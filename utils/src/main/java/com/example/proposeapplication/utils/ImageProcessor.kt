package com.example.proposeapplication.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//이미지를 처리하는 프로세서
class ImageProcessor(private val context: Context) {



    //이미지를 갤러리에 저장하는 함수
    fun saveImageToGallery(bitmap: Bitmap) {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.KOREA)
        val title = "IMG_${sdf.format(Date())}.jpg"

        var fos: OutputStream? = null


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            context.contentResolver?.also { resolver ->
                // 5
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, title)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // 6
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // 7
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, title)
            fos = FileOutputStream(image)
//            Log.d("${this.javaClass.simpleName} : ", "사진이 저장되었습니다. / ${image.toURI()}")
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Log.d("${this.javaClass.simpleName} : ", "사진이 저장되었습니다. ")
        }


    }

    //필요한 권한이 있는지 체크하기
    private fun checkPermission() {

    }

    fun getLatestImage(): Bitmap? {
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.MIME_TYPE
        )
        var res: Bitmap? = null
        context.contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC"
            ).use { cursor ->
                if (cursor!!.moveToLast()) {
                    val latestImageUri = cursor.getString(1)
                    val imageFile = File(latestImageUri)
                    if (imageFile.exists()) {
                        res = BitmapFactory.decodeFile(latestImageUri)
                    }
                }
            }
        return res
    }

    fun edgeDetection(bitmap: Bitmap): Bitmap {
        // No implementation found ~ 에러 해결
        OpenCVLoader.initDebug()
        val input = Mat()
        Utils.bitmapToMat(bitmap, input) // bitmap을 매트릭스로 변환
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY) //흑백으로 변경

        //Convert to detected picture
        Imgproc.Canny(input, input, 80.0, 150.0)

        return bitmap.copy(bitmap.config, true).apply {
            Utils.matToBitmap(input, this)
        }
    }
}