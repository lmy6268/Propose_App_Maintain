package com.hanadulset.pro_poseapp.data.datasource

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.protobuf.FieldType
import androidx.datastore.preferences.protobuf.Type
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.hanadulset.pro_poseapp.data.BuildConfig
import com.hanadulset.pro_poseapp.data.UserPreference
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.R
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import com.hanadulset.pro_poseapp.utils.eventlog.FeedBackData
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.DateFormat.getTimeInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FileHandleDataSourceImpl(private val context: Context) : FileHandleDataSource {


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
                        put(MediaStore.MediaColumns.RELATIVE_PATH, PROPOSE_PATH)
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

    override suspend fun loadCapturedImages(isReadAllImage: Boolean): List<ImageResult> {
        val resList = mutableListOf<ImageResult>()
        when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true -> {
                val externalUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = MediaStore.Images.ImageColumns.RELATIVE_PATH + " like ? "
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.DATA
                )
                val selectionArgs = arrayOf("%ProPose%")
                context.contentResolver.query(
                    externalUri,
                    projection,
                    selection,
                    selectionArgs,
                    MediaStore.MediaColumns.DATE_MODIFIED + " DESC "
                ).use { cursor ->
                    if (cursor == null) return emptyList()
                    when (cursor.count) {
                        0 -> return emptyList()
                        else -> {
                            while (cursor.moveToNext()) {
                                if (cursor.isNull(1).not()) {
                                    val idColNum =
                                        cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                                    val imageId = cursor.getLong(idColNum)
                                    val imageTakenDate = cursor.getString(1).let { timestamp ->
                                        val sdf =
                                            SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
                                        sdf.format(timestamp.toLong())
                                    }
                                    val imageUri = ContentUris.withAppendedId(externalUri, imageId)
                                    val imageResult =
                                        ImageResult(imageUri, takenDate = imageTakenDate)
                                    resList.add(imageResult)
                                    if (isReadAllImage.not()) break //한개의 이미지 데이터만 가져오는 경우
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_PICTURES}/ProPose")
                if (imagesDir.exists()) {
                    imagesDir.listFiles().apply { this?.sortByDescending { it.lastModified() } }
                        ?.forEach {
                            resList.add(ImageResult(it.toUri(), it.lastModified().toString()))
                            if (isReadAllImage.not()) return resList.toList() //한개의 이미지만 반환
                        }
                }
            }
        }
        return resList.toList()
    }

    override fun deleteCapturedImage(uri: Uri): Boolean {
        val targetFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val path = context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                cursor!!.moveToNext()
                val index = cursor.getColumnIndex("_data")
                cursor.getString(index)
            }
            File(path)
        } else {
            uri.toFile()
        }


        return targetFile.delete()
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
                    headers {
                        append("x-api-key", BuildConfig.API_KEY)
                    }
                    setBody(feedback)
                })
            }
        }
        Log.d("response for feedback: ", response.toString())
    }


    companion object {
        private const val FILE_NAME = "yyyy_MM_dd_HH_mm_ss_SSS"
        private const val PROPOSE_PATH = "Pictures/ProPose"
    }

}
