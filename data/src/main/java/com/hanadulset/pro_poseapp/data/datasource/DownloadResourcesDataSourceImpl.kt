package com.hanadulset.pro_poseapp.data.datasource

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amazonaws.AmazonClientException
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.hanadulset.pro_poseapp.data.datasource.interfaces.DownloadResourcesDataSource
import com.hanadulset.pro_poseapp.data.mapper.UserConfig
import com.hanadulset.pro_poseapp.utils.BuildConfig
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadResponse
import com.hanadulset.pro_poseapp.utils.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

class DownloadResourcesDataSourceImpl(private val applicationContext: Context) :
    DownloadResourcesDataSource {

    private lateinit var s3Client: AmazonS3Client
    private lateinit var transferUtility: TransferUtility
    private val downloadList = mutableListOf<String>()
    private val config by lazy {
        val file = File(applicationContext.dataDir, ASSET_CONFIG)
        if (file.exists().not()) applicationContext.assets.open(ASSET_CONFIG).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
        }
        val jsonString = file.readText()
        Json.decodeFromString<UserConfig>(jsonString)

    }


    //체크에 관련된 메소드

    //다운로드 여부를 체크하기 위한 메소드
    override suspend fun checkForDownload(): CheckResponse = withContext(Dispatchers.IO) {
        if (this@DownloadResourcesDataSourceImpl::s3Client.isInitialized.not()) {
            s3Client = AmazonS3Client(
                CognitoCachingCredentialsProvider(
                    applicationContext,
                    BuildConfig.CREDENTIAL_POOL_ID, //자격증명 pool ID
                    LOCAL_REGION
                ), Region.getRegion(
                    LOCAL_REGION
                )
            )
        }

        val checkedList = mutableListOf<String>()
        val needToDownload = mutableListOf<String>()
        var mustDownloadFlag = false
        var totalSize = 0L

        //검증 대상 파일 대상 목록을 추가함.
        checkedList.addAll(config.prepareMaterials.models)


        //검증 시간
        checkedList.forEach { fileName ->
            val fileMetadata = getMetaDataFromS3(fileName)
            //실제 존재하는 지 확인
            if (isExistInData(fileName)) {
                //서버의 해당 데이터 버전을 조회한다.
                try {
                    if (checkSameVersionLocal(fileName, fileMetadata.versionId).not()) {
                        needToDownload.add(fileName)
                        totalSize += fileMetadata.contentLength
                    }
                } catch (ex: AmazonClientException) {
                    Log.e("Amazon Error: ", ex.message!!)
                }

            } else {
                //무조건 필요한 경우 -> 해당 파일이 현재 기기에 없음.
                needToDownload.add(fileName)
                totalSize += fileMetadata.contentLength
                mustDownloadFlag = true
            } //아니라면 무조건 다운로드 진행
        }


        downloadList.clear()
        downloadList.addAll(needToDownload.toList())


        //검증 결과를 응답으로 전달
        return@withContext CheckResponse(
            needToDownload = needToDownload.isNotEmpty(),
            downloadType = if (mustDownloadFlag) CheckResponse.TYPE_MUST_DOWNLOAD else CheckResponse.TYPE_ADDITIONAL_DOWNLOAD,
            totalSize = totalSize,
            hasRemainStorage = checkFreeSpaceInDataDir(totalSize)
        )
    }


    //여유 공간 확인
    private fun checkFreeSpaceInDataDir(need: Long): Boolean {
        val dataDir = applicationContext.dataDir
        return dataDir.freeSpace >= need
    }

    //서버에서 메타데이터 가져옴.
    private suspend fun getMetaDataFromS3(fileName: String): ObjectMetadata =
        withContext(Dispatchers.IO) {
            return@withContext s3Client.getObject(
                BuildConfig.BUCKET_ID,
                fileName
            ).objectMetadata
        }

    //로컬에 저장된 버전 정보를 가져옴.
    private suspend fun checkSameVersionLocal(fileName: String, versionID: String): Boolean =
        withContext(Dispatchers.IO) {
            val localID = applicationContext.dataStore.data.map {
                it[stringPreferencesKey(fileName)]
            }.first()


            return@withContext localID == versionID
        }


    // 데이터 폴더에 존재하는지
    private fun isExistInData(fileName: String): Boolean {
        val dataPath = applicationContext.dataDir.absolutePath
        val fileForCheck = File(dataPath, fileName)
        return fileForCheck.exists()
    }

    //다운로드를 책임지는 메소드
    override suspend fun startToDownload(): DownloadResponse =
        withContext(Dispatchers.IO) {
            if (this@DownloadResourcesDataSourceImpl::transferUtility.isInitialized.not()) transferUtility =
                TransferUtility.builder().context(applicationContext)
                    .defaultBucket(BuildConfig.BUCKET_ID) //버킷 이름
                    .s3Client(s3Client).build()

            val dataFlow = callbackFlow {
                downloadList.forEachIndexed { index, fileName ->
                    val targetFile = File(applicationContext.dataDir.absolutePath, fileName)
                    transferUtility.download(fileName, targetFile, object : TransferListener {
                        val downloadState =
                            MutableStateFlow(DownloadState(state = DownloadState.STATE_ON_PROGRESS))

                        override fun onStateChanged(id: Int, state: TransferState?) {
                            if (state == TransferState.COMPLETED) {
                                downloadState.value
                                trySend(
                                    downloadState.value.copy(state = DownloadState.STATE_COMPLETE)
                                ).isSuccess
                            }
                        }

                        override fun onProgressChanged(
                            id: Int,
                            bytesCurrent: Long,
                            bytesTotal: Long
                        ) {
                            val downloaded = DownloadState(
                                state = DownloadState.STATE_ON_PROGRESS,
                                currentFileName = fileName,
                                currentFileIndex = index,
                                totalFileCnt = downloadList.size,
                                currentBytes = bytesCurrent,
                                totalBytes = bytesTotal
                            )
                            downloadState.value = downloaded
                            trySend(
                                downloadState.value
                            ).isSuccess
                        }

                        override fun onError(id: Int, ex: Exception?) {
                            cancel(CancellationException(ex))
                        }
                    })

                }
                awaitClose {

                }

            }



            return@withContext DownloadResponse(
                data = dataFlow,
                state = DownloadResponse.STATE_GOOD_TO_DOWNLOAD
            )
        }

    private suspend fun modifyVersionIDLocal(fileName: String, versionID: String) =
        withContext(Dispatchers.IO) {
            applicationContext.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(fileName)] = versionID
            }
        }

    companion object {
        private const val ASSET_CONFIG = "config.json"
        private const val PREF_NAME = "userPreference"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)
        private val LOCAL_REGION = Regions.AP_NORTHEAST_2 //리전
    }
}