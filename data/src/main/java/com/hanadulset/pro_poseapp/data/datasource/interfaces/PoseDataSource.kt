package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import org.opencv.core.Mat

interface PoseDataSource {


    /**포즈를 추천해준다.*/
    suspend fun recommendPose(backgroundBitmap: Bitmap): PoseDataResult

    fun preparePoseData()
    //그외, 위를 구현하기 위한 메소드들 정리
    // 포즈 추천 관련

    /**Hog 뽑기 위한 사전 작업*/
    suspend fun preProcessing(image: Bitmap): Mat

    /**HOG 값과 Resnet 결과값 간의 유클리드 거리를 구한다. */
    fun getDistance(angle: List<Double>, centroidIdx: Int): Double

    fun distanceAngle(aAngle: List<Double>, bAngle: List<Double>): Double


    /**HOG 값의 유클리드 거리를 구한다. */
    fun distanceHog(aHog: List<Double>, bHog: List<Double>): Double

    /**차원을 증가 시킨다.*/
    fun addZDimension(arr: MutableList<List<Double>>)

    /**각 결과값 간의 거리를 계산한다. */
    fun calculateDistance(a: List<List<Double>>, b: List<List<Double>>): Double

    /**리스트의 모양을 변형시킨다. */
    fun reshapeList(inputList: List<Double>, newShape: List<Int>): List<List<Double>>

    /**그레디언트를 뽑아낸다.*/
    suspend fun getGradient(targetImage: Mat): Pair<Mat, Mat>

    /**히스토그램을 뽑아낸다.*/
    suspend fun getHistogram(magnitude: Mat, orientation: Mat): DoubleArray

    /**히스토그램 맵을 뽑아낸다. */
    suspend fun getHistogramMap(backgroundBitmap: Bitmap): Mat


    fun getAngleFromHog(histogramMap: Mat): List<Double>

    //포즈 위치 선정 관련
//    fun makeLayoutImage(yoloResult: ArrayList<YoloPredictResult>): Bitmap
//    fun outputsToNMSPredictions(
//        scaleSize: Size,
//        outputs: FloatArray
//    ): ArrayList<YoloPredictResult>
//
//    fun IOU(a: Rect, b: Rect): Float
//    fun nonMaxSuppression(
//        boxes: ArrayList<YoloPredictResult>
//    ): ArrayList<YoloPredictResult>
    /**포즈를 취하며 있을 위치를 추천해준다.*/
//    fun recommendPosePosition(backgroundBitmap: Bitmap): DoubleArray

}
