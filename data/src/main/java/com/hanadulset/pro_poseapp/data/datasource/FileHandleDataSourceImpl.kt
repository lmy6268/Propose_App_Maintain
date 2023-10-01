package com.hanadulset.pro_poseapp.data.datasource

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import com.hanadulset.pro_poseapp.data.BuildConfig
import com.hanadulset.pro_poseapp.data.UserPreference
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.R
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.eventlog.FeedBackData
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FileHandleDataSourceImpl(private val context: Context) : FileHandleDataSource {
    private lateinit var s3Client: AmazonS3Client
    private lateinit var transferUtility: TransferUtility
    private val downloadQueue: ArrayList<String> = ArrayList()
    private val versionIDQueue: ArrayList<String> = ArrayList()
    private val fileSpecList by lazy {
        context.resources.getStringArray(R.array.need_to_download_list).toList()
    }

    //버전정보를 가지고 있는 사용자 정보
    private val userPreference by lazy {
        UserPreference().apply {
            context.dataStore.data.map {
                fileSpecList.forEach { key ->
                    modelVersionId[key] = it[stringPreferencesKey(key)]
                }
            }
        }
    }

    //S3 데이터를 가져오기 위한 변수들
    private val bucketId by lazy {
       BuildConfig.BUCKET_ID
    }


    override suspend fun saveImageToGallery(bitmap: Bitmap): Uri =
        withContext(Dispatchers.IO) {
            val sdf = SimpleDateFormat(FILE_NAME, Locale.KOREA).format(System.currentTimeMillis())
            val filename = "IMG_${sdf}.jpg"
            var uri: Uri? = null
            val fos =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.DATE_TAKEN, sdf)
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ProPose")
                    })?.let {
                    uri = it
                    context.contentResolver.openOutputStream(it)
                }
                else {
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_PICTURES}/ProPose")
                    File(imagesDir.absolutePath).let {
                        if (it.exists().not()) it.mkdir()
                        val image = File(imagesDir, filename)
                        uri = image.toUri()
                        image.outputStream()
                    }
                }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos!!) // 이 부분에서 이슈가 있는 것 같다.
            uri!!.apply {
                fos.close()
            }
        }

    //최근 이미지 불러오기
    override fun getLatestImage(): Uri? {
        val resolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
//            MediaStore.Images.Media
        )
        resolver.query(
            uri, arrayOf(
                "Pictures"
            ), null, null, null
        ).use {

        }
        return null
    }

// 모델을 다운받는 부분

    override suspend fun downloadModel(downloadStateFlow: MutableStateFlow<DownloadInfo>) {
        val filePaths = ArrayList<Pair<String, String>>()
        downloadQueue.zip(versionIDQueue).forEachIndexed { idx, item ->
            filePaths.add(
                Pair(
                    item.first, onDownload(
                        downloadStateFlow,
                        item.first,
                        idx,
                        downloadQueue.size,
                        fileSpecList.size == downloadQueue.size,
                        item.second
                    )
                )
            )
        }
        Log.d("downloaded Done:", filePaths.toString())
    }




    private suspend fun checkToDownload() = suspendCoroutine { cont ->
        CoroutineScope(Dispatchers.IO).launch {
            var updateFlag = false
            fileSpecList.forEach {
                val serverID = s3Client.getObject(bucketId, it).objectMetadata.versionId
                val localID = userPreference.modelVersionId[it]
                if (serverID != localID) downloadQueue.add(it)
                    .apply {
                        if (localID != null) updateFlag = true
                        versionIDQueue.add(serverID)
                    } // 업데이트 파일
            }
            if (updateFlag) cont.resume(NEED_UPDATE)
            else if (downloadQueue.isNotEmpty()) cont.resume(NEED_DOWNLOAD)
            else cont.resume(NEED_NOTHING)
        }

    }

    override suspend fun checkForDownloadModel(downloadInfo: DownloadInfo): DownloadInfo {
        //현재 버전과 서버의 버전을 확인함.
        val res = when (checkToDownload()) {
            //만약 현재 버전과 서버의 버전이 같다면, value 값의 state를 SKIP으로 전환
            NEED_NOTHING -> downloadInfo.copy(state = DownloadInfo.ON_SKIP)
            NEED_DOWNLOAD -> downloadInfo.copy(state = DownloadInfo.ON_DOWNLOAD)
            NEED_UPDATE -> downloadInfo.copy(state = DownloadInfo.ON_UPDATE)
            else ->
                downloadInfo
        }
        return res

    }


    override suspend fun sendFeedBackData(feedBackData: FeedBackData) {
        val url = BuildConfig.FEEDBACK_URL
        val feedback = Json.encodeToString(feedBackData)
        val contentType = ContentType.Application.Json
        val response = suspendCoroutine {
            CoroutineScope(Dispatchers.IO).launch {
                val client = HttpClient(CIO)
                it.resume(client.post(url) {
                    contentType(contentType)
                    setBody(feedback)
                })
            }
        }
        Log.d("response for feedback: ", response.toString())
    }


    private suspend fun onDownload(
        downloadStateFlow: MutableStateFlow<DownloadInfo>,
        fileName: String,
        nowIdx: Int,
        totalLength: Int,
        isDownload: Boolean,
        versionID: String
    ): String = suspendCoroutine { cont ->
        val filePath = "${context.dataDir.absolutePath}/$fileName"
        transferUtility.download(fileName, File(filePath), object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                //만약 다운로드가 완료된 경우
                if (state == TransferState.COMPLETED) {
                    CoroutineScope(Dispatchers.IO).launch {
                        context.dataStore.edit { preferences ->
                            preferences[stringPreferencesKey(fileName)] = versionID
                        }
                    }
                    cont.resume(filePath) //파일 위치를 반환함.
                }
            }

            override fun onProgressChanged(
                id: Int, bytesCurrent: Long, bytesTotal: Long
            ) {
                downloadStateFlow.value = downloadStateFlow.value.copy(
                    state = if (isDownload) DownloadInfo.ON_DOWNLOAD else DownloadInfo.ON_UPDATE,
                    totalLength = totalLength,
                    byteCurrent = bytesCurrent,
                    byteTotal = bytesTotal,
                    nowIndex = nowIdx
                )
            }

            override fun onError(id: Int, ex: Exception?) {
                downloadStateFlow.value = downloadStateFlow.value.copy(
                    state = DownloadInfo.ON_ERROR, errorException = ex
                )
            }
        })
    }


    private suspend fun initAWSObj() {
        if (this::s3Client.isInitialized.not()) s3Client = suspendCoroutine {
            CoroutineScope(Dispatchers.IO).launch {
                it.resume(
                    AmazonS3Client(
                        CognitoCachingCredentialsProvider(
                            context,
                            BuildConfig.CREDENTIAL_POOL_ID, //자격증명 pool ID
                            Regions.AP_NORTHEAST_2 //리전
                        ), Region.getRegion(
                            Regions.AP_NORTHEAST_2 //리전)
                        )
                    )
                )
            }
        }
        if (this::transferUtility.isInitialized.not()) transferUtility =
            suspendCoroutine { cont ->
                //Client 생성
                CoroutineScope(Dispatchers.IO).launch {
                    cont.resume(
                        TransferUtility.builder().context(context)
                            .defaultBucket(bucketId) //버킷 이름
                            .s3Client(s3Client).build()
                    )
                }
            }
    }

    companion object {
        private const val PHOTO_TYPE = "image/jpeg"
        private const val FILE_NAME = "yyyy_MM_dd_HH_mm_ss_SSS"
        private const val PREF_NAME = "userPreference"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)
        private const val NEED_NOTHING = 0
        private const val NEED_DOWNLOAD = 1
        private const val NEED_UPDATE = 2
    }

}
