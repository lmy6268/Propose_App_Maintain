package com.hanadulset.pro_poseapp.data.datasource

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.hanadulset.pro_poseapp.data.datasource.interfaces.FileHandleDataSource
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class FileHandleDataSourceImpl(private val context: Context) : FileHandleDataSource {


    override suspend fun saveImageToGallery(bitmap: Bitmap): Uri =
        withContext(Dispatchers.IO) {
            val sdf = System.currentTimeMillis()
            val filename = "IMG_${sdf}.jpg"
            var uri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                context.contentResolver.insert(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    ContentValues().apply {
                        put(MediaStore.Images.ImageColumns.DATE_ADDED, sdf)
                        put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename)
                        put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.Images.ImageColumns.RELATIVE_PATH, PROPOSE_PATH)
                    })?.let {
                    uri = it
                    context.contentResolver.openOutputStream(it)
                }?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            //이하 버전에서는 문제가 없음
            else {
                Environment.getExternalStoragePublicDirectory(PROPOSE_PATH).run {
                    File(absolutePath).let {
                        if (it.exists().not()) it.mkdir()
                        val image = File(this, filename)
                        uri = image.toUri()
                        image.outputStream()
                    }.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                        context.sendBroadcast(
                            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
                        )
                    }
                }
            }
            uri!!
        }


    override suspend fun loadCapturedImages(isReadAllImage: Boolean): List<ImageResult> {

        val resList = mutableListOf<ImageResult>()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val externalUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val selection = "${MediaStore.Images.ImageColumns.RELATIVE_PATH} like ?"
            val projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.DATA
            )
            val selectionArgs = arrayOf("%$PROPOSE_PATH%")
            context.contentResolver.query(
                externalUri,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.ImageColumns.DATE_ADDED + " DESC "
            ).use { cursor ->
                if (cursor == null) return emptyList()
                else if (cursor.moveToFirst()) {
                    do {
                        if (cursor.isNull(1).not()
                        ) { //MediaStore.Images.Media.DATE_ADDED 컬럼 값이 Null이 아닌 경우
                            val idColNum =
                                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                            val imageId = cursor.getLong(idColNum)
                            val imageTakenDate = cursor.getString(1).let { timestamp ->
                                val sdf = SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN)
                                sdf.format(timestamp.toLong())
                            }
                            val imageUri = ContentUris.withAppendedId(externalUri, imageId)
                            val imageResult =
                                ImageResult(imageUri, takenDate = imageTakenDate)
                            resList.add(imageResult)
                            if (isReadAllImage.not()) break //한개의 이미지 데이터만 가져오는 경우
                        }
                    } while (cursor.moveToNext())
                }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(PROPOSE_PATH)
            if (imagesDir.exists()) {
                imagesDir.listFiles().apply { this?.sortByDescending { it.lastModified() } }
                    ?.forEach {
                        resList.add(ImageResult(it.toUri(), it.lastModified().toString()))
                        if (isReadAllImage.not()) return resList.toList() //한개의 이미지만 반환
                    }
            }
        }
        return resList.toList()
    }

    override suspend fun deleteCapturedImage(uri: Uri): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver.query(uri, proj, null, null, null)
                if (cursor != null) {
                    File(cursor.use {
                        val index = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                        it.getString(index)
                    }).delete()

                } else context.contentResolver.delete(uri, null, null)
                true
            } else {
                uri.path?.run { File(this).delete() }
                true
            }
        } catch (ex: Exception) {
            false
        }
    }

    companion object {
        private const val FILE_NAME = "yyyy_MM_dd_HH_mm_ss_SSS"
        private val PROPOSE_PATH = "${Environment.DIRECTORY_PICTURES}/ProPose"
    }

}
