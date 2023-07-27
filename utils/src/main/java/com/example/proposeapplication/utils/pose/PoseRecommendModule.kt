package com.example.proposeapplication.utils.pose

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Core.add
import org.opencv.core.Core.pow
import org.opencv.core.Core.sqrt
import org.opencv.core.CvType.CV_64F
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.Sobel
import kotlin.math.atan2

object PoseRecommendModule {
    //결과값으로 배열을 반환
    fun getResult(bitmap: Bitmap) {
        val inputImage = Mat().apply {
            Utils.bitmapToMat(bitmap, this)
        }
        val gradientX = Mat().apply {
            Sobel(inputImage, this, CV_64F, 1, 0, 3)
        }
        val gradientY = Mat().apply {
            Sobel(inputImage, this, CV_64F, 0, 1, 3)
        }


        val gradientMagnitude = Mat().apply {
            val tmp = Mat()
            add(
                gradientX.apply { pow(this, 2.0, this) },
                gradientY.apply { pow(this, 2.0, this) },
                tmp
            )
            sqrt(tmp, this)
        }

        val gradientOrientation = Mat(gradientX.size(), CV_64F).apply {
            for (r in 0..gradientX.rows()) {
                for (c in 0 until gradientX.cols()) {
                    val gX = gradientX.at(Double::class.java, intArrayOf(r, c))
                    val gY = gradientY.at(Double::class.java, intArrayOf(r, c))
                    this.at(Double::class.java, intArrayOf(r, c)).v =
                        Math.toDegrees(atan2(gY.v, gX.v))
                }
            }
        }
        Log.d("resultInModule",gradientMagnitude.dump()+"  "+gradientOrientation.dump())


    }

    private fun getGradient(bitmap: Bitmap) {

    }


    private fun showMagnitude(path: String) {

    }

}