package com.hanadulset.pro_poseapp.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amazonaws.AmazonClientException
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.hanadulset.pro_poseapp.data.datasource.interfaces.DownloadResourcesDataSource
import com.hanadulset.pro_poseapp.utils.BuildConfig
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadResourcesDataSourceImpl(private val applicationContext: Context) :
    DownloadResourcesDataSource {

    private lateinit var s3Client: AmazonS3Client
    private val transferUtility by lazy {
        TransferNetworkLossHandler.getInstance(applicationContext)
        TransferUtility.builder().context(applicationContext)
            .defaultBucket(BuildConfig.BUCKET_ID) //버킷 이름
            .s3Client(s3Client).build()
    }
    private val downloadList = mutableListOf<Pair<String, String>>()
    private val downloadState = MutableStateFlow<Boolean?>(false)


    private val downloadFlow = callbackFlow {
        downloadState.value.let { state ->
            var downloadId: Int = -1

            downloadList.forEachIndexed { index, pair ->
                val fileName = pair.first
                val versionID = pair.second
                val targetFile = File(applicationContext.cacheDir.absolutePath, fileName)
                val listener = object : TransferListener {
                    override fun onStateChanged(id: Int, transferState: TransferState?) {
                        when (transferState) {
                            TransferState.COMPLETED -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val savedPath =
                                        File(
                                            applicationContext.dataDir.absolutePath,
                                            "/$fileName"
                                        )
                                    targetFile.copyTo(
                                        savedPath,
                                        overwrite = true
                                    ).run {
                                        targetFile.delete()
                                        modifyVersionIDLocal(fileName, versionID)
                                    }
                                }
                            }

                            else -> {

                            }
                        }

                    }

                    override fun onProgressChanged(
                        id: Int,
                        bytesCurrent: Long,
                        bytesTotal: Long
                    ) {
                        downloadId = id
                        trySend(
                            DownloadState(
                                currentFileIndex = index,
                                totalFileCnt = downloadList.size,
                                currentBytes = bytesCurrent,
                                totalBytes = bytesTotal,
                                currentFileName = fileName
                            )
                        )
                    }

                    override fun onError(id: Int, ex: Exception?) {
                    }

                }
                transferUtility.run {
                    when (state) {
                        //다운로드 진행중
                        true -> {
                            if (downloadId != -1) resume(downloadId).setTransferListener(listener)
                            else download(fileName, targetFile, listener)
                        }
                        //다운로드 일시정지
                        false -> {
                            this.pause(downloadId)
                        }
                        //다운로드 정지
                        else -> {
                            this.cancel(downloadId)
                        }
                    }
                }
            }
            awaitClose { close() }
        }
    }

    private fun checkNetworkState(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(ConnectivityManager::class.java)
        val network: Network =
            connectivityManager.activeNetwork ?: return false
        val actNetwork: NetworkCapabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return when {
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    }
//체크에 관련된 메소드

    //다운로드 여부를 체크하기 위한 메소드 -> 현재 인터넷 연결이 없을 때, 체크하는 방법이 따로 없음.
    @SuppressLint("HardwareIds")
    override suspend fun checkForDownload(): CheckResponse =
        withContext(Dispatchers.IO) {
            CognitoCachingCredentialsProvider(
                applicationContext, BuildConfig.CREDENTIAL_POOL_ID, //자격증명 pool ID
                LOCAL_REGION
            ).run {
                s3Client =
                    AmazonS3Client(
                        this,
                        Region.getRegion(
                            LOCAL_REGION
                        ),
                        ClientConfiguration()
                            .withSocketTimeout(300)
                    )

                val checkedList = mutableListOf<String>()
                val needToDownload = mutableListOf<Pair<String, String>>()
                var mustDownloadFlag = false
                var onErrorFlag = false
                var isOkayToSkip = true
                var totalSize = 0L

                //검증 대상 파일 대상 목록을 추가함.
                checkedList.addAll(needToCheckFileList)

                //검증 시간
                checkedList.forEach { fileName ->
                    try {
                        val fileMetadata =
                            s3Client.getObjectMetadata(BuildConfig.BUCKET_ID, fileName)

                        //실제 존재하는 지 확인
                        if (isExistInData(fileName)) {
                            //서버의 해당 데이터 버전을 조회한다.
                            if (checkSameVersionLocal(fileName, fileMetadata.versionId).not()) {
                                needToDownload.add(Pair(fileName, fileMetadata.versionId))
                                totalSize += fileMetadata.contentLength
                            }
                        } else {
                            //무조건 필요한 경우 -> 해당 파일이 현재 기기에 없음.
                            needToDownload.add(Pair(fileName, fileMetadata.versionId))
                            totalSize += fileMetadata.contentLength
                            mustDownloadFlag = true
                        } //아니라면 무조건 다운로드 진행
                    } catch (ex: AmazonClientException) {
                        onErrorFlag = true
                        isOkayToSkip = isExistInData(fileName)
                        val deviceID = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                        Firebase.analytics.apply {
                            setUserId(deviceID)
                        }.logEvent("EVENT_AWS_S3_ERROR") {
                            param(
                                "networkConnection",
                                checkNetworkState(applicationContext).toString()
                            )
                            param("timestamp", System.currentTimeMillis())
                            param("errorMessage", "${
                                ex.message?.replace(")", "")?.split(";")
                                    ?.let {
                                        if (it.size >= 4) it[1] + "," + it[3]
                                        else it
                                    }
                            }")
                            param(
                                "deviceID",
                                deviceID
                            )
                        }
//                        Log.e(
//                            "Amazon Error: ",
//                            "${
//                                ex.message?.replace(")", "")?.split(";")
//                                    ?.let {
//                                        if (it.size >= 4) it[1] + "," + it[3]
//                                        else it
//                                    }
//                            }"
//                        )
                    }
                }

                downloadList.clear()
                downloadList.addAll(needToDownload.toList())
                //검증 결과를 응답으로 전달
                return@run CheckResponse(
                    needToDownload = isOkayToSkip.not() || needToDownload.isNotEmpty(),
                    downloadType = if (mustDownloadFlag) CheckResponse.TYPE_MUST_DOWNLOAD
                    else if (onErrorFlag) CheckResponse.TYPE_ERROR
                    else CheckResponse.TYPE_ADDITIONAL_DOWNLOAD,
                    totalSize = totalSize,
                    hasRemainStorage = checkFreeSpaceInDataDir(totalSize)
                )
            }
        }


    //여유 공간 확인
    private fun checkFreeSpaceInDataDir(need: Long): Boolean {
        val dataDir = applicationContext.dataDir
        return dataDir.freeSpace >= need
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
    override suspend fun startToDownload(): Flow<DownloadState> {
        downloadState.value = true
        return downloadFlow
    }

    fun pauseDownload() {
        downloadState.value = false
    }

    fun restartDownload() {
        downloadState.value = true
    }


    suspend fun checkInternetConnection(): Flow<Boolean> =
        withContext(Dispatchers.IO) {
            val manager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            flow {
                manager.activeNetwork?.run {
                    val actNetwork = manager.getNetworkCapabilities(this)
                    val res = if (actNetwork != null) {
                        actNetwork.hasTransport(
                            NetworkCapabilities.TRANSPORT_CELLULAR
                        ) || actNetwork.hasTransport(
                            NetworkCapabilities.TRANSPORT_WIFI
                        )
                    } else false
                    emit(res)
                }
            }
        }

    private suspend fun modifyVersionIDLocal(fileName: String, versionID: String) =
        withContext(Dispatchers.IO) {
            applicationContext.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(fileName)] = versionID
            }
        }

    companion object {
        private val needToCheckFileList = listOf("vapnet.ptl")
        private const val PREF_NAME = "userPreference"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)
        private val LOCAL_REGION = Regions.AP_NORTHEAST_2 //리전
    }
}