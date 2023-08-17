package com.example.proposeapplication.utils.pose

import android.graphics.Bitmap
import org.opencv.core.Mat

interface PoseRecommendController {
    fun preProcessing(image: Bitmap): Mat
    fun getGradient(image: Mat): Pair<Mat, Mat>
    fun getHistogram(magnitude: Mat, orientation: Mat): DoubleArray
    fun getHistogramMap(image: Bitmap): Mat
    fun getHOG(image: Bitmap): List<Double>//HoG 값 구하기
    suspend fun getRecommendPose(backgroundImage: Bitmap): String
}