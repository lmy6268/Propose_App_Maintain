package com.example.proposeapplication.data.datasource

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.proposeapplication.data.datasource.interfaces.ImageProcessDataSource
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//이미지 처리
class ImageProcessDataSourceImpl() : ImageProcessDataSource {

    override fun getFixedImage(bitmap: Bitmap): Bitmap {
        // No implementation found ~ 에러 해결
        OpenCVLoader.initDebug()
        val input = Mat()
        Utils.bitmapToMat(bitmap, input) // bitmap을 매트릭스로 변환
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY) //흑백으로 변경
//        Imgproc.GaussianBlur(input, input, Size(0.0, 0.0), 1.0)

        //Convert to detected picture
        Imgproc.Canny(input, input, 100.0, 200.0)

        return bitmap.copy(bitmap.config, true).apply {
            Utils.matToBitmap(input, this)
        }
    }


    override fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap {
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB) //알파값을 빼고 저장
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        return outputResizeBitmap
    }

    //이미지를 원하는 사이즈로 리사이즈 하는 함수
    override fun resizeBitmap(
        bitmap: Bitmap,
        width: Double,
        height: Double,
        isScaledResize: Boolean
    ): Bitmap {
        val size = Size(width, height)

        return when (isScaledResize) {
            //스케일 줄이기
            true -> {
                Bitmap.createScaledBitmap(
                    bitmap,
                    bitmap.width / size.width.toInt(),
                    bitmap.height / size.height.toInt(),
                    true
                )
            }

            //리사이징
            else -> {
                Bitmap.createBitmap(
                    bitmap, 0, 0, size.width.toInt(), size.height.toInt()
                )
            }
        }

    }


}