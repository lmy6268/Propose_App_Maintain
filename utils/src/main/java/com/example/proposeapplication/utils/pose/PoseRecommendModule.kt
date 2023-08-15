package com.example.proposeapplication.utils.pose

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.COLOR_RGB2BGR
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.imgproc.Imgproc.resize
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


//포즈 분류 및 제공시 사용하는 모듈
object PoseRecommendModule {
    internal object HogConfig {
        val imageResize: Size = Size(128.0, 128.0)
        const val imageConvert: Int =
            COLOR_BGR2GRAY  //cv2.COLOR_BGR2GRAY | cv2.COLOR_BGR2RGB | cv2.COLOR_BGR2HSV
        val cellSize: Size = Size(16.0, 16.0)
        val blockSize: Size = Size(1.0, 1.0)
        const val magnitudeThreshold: Int = 151
        const val nBins: Int = 9
    }

    val centroid by lazy {

    }


    private fun preProcessing(image: Bitmap): Mat {
        OpenCVLoader.initDebug() //초기화
        return Mat(image.width, image.height, CvType.CV_8UC3).apply {
            Utils.bitmapToMat(image, this)
            cvtColor(this, this, COLOR_RGB2BGR)


            resize(this, this, HogConfig.imageResize)
            cvtColor(this, this, HogConfig.imageConvert)
        }
    }


    private fun getGradient(image: Mat): Pair<Mat, Mat> {
        var gradientX = Mat(image.size(), image.type())
        var gradientY = Mat(image.size(), image.type())

        Imgproc.Sobel(image, gradientX, CvType.CV_64F, 1, 0, 3)
        Imgproc.Sobel(image, gradientY, CvType.CV_64F, 0, 1, 3)

        //gradient_x = gradient_x / int(np.max(np.abs(gradient_x)) if np.max(np.abs(gradient_x)) != 0 else 1) * 255
        gradientX = gradientX.apply {
            val zeroMat = Mat.zeros(this.size(), this.type())
            val absMat = Mat(this.size(), this.type())
            Core.absdiff(this, zeroMat, absMat)//abs값으로 변환
            val std = Core.minMaxLoc(absMat)
            val dv = if (std.maxVal != 0.0) std.maxVal else 1.0
            Core.divide(this, Scalar(dv), this)
            Core.multiply(this, Scalar(255.0), this)
        }
//        gradient_y = gradient_y / int(np.max(np.abs(gradient_y)) if np.max(np.abs(gradient_y)) != 0 else 1) * 255
        gradientY = gradientY.apply {
            val zeroMat = Mat.zeros(this.size(), this.type())
            val absMat = Mat(this.size(), this.type())
            Core.absdiff(this, zeroMat, absMat)//abs값으로 변환
            val std = Core.minMaxLoc(absMat)
            val dv = if (std.maxVal != 0.0) std.maxVal else 1.0
            Core.divide(this, Scalar(dv), this)
            Core.multiply(this, Scalar(255.0), this)
        }


        val gradientMagnitude = Mat(gradientX.size(), gradientX.type()).apply {
            val tmpY = Mat(gradientY.size(), gradientY.type())
            Core.pow(gradientX, 2.0, this)
            Core.pow(gradientY, 2.0, tmpY)
            Core.add(this, tmpY, this)
            Core.pow(this, 0.5, this)
        }

        // Calculate gradient orientation using OpenCV
        val gradientOrientation = Mat.zeros(gradientX.size(), gradientX.type()).apply {
            Core.phase(gradientX, gradientY, this, true)
        }

        // Adjust gradient orientation to [-π, π] range -> 이부분만 살짝 수정하면 Hog값은 100퍼센트 나옴.
        for (row in 0 until gradientOrientation.rows()) {
            for (col in 0 until gradientOrientation.cols()) {
                var adjustedPhase = gradientOrientation[row, col][0]
                if (adjustedPhase >= 180.0) {
                    adjustedPhase -= 180.0
                }
                gradientOrientation.put(row, col, adjustedPhase)
            }
        }


        for (x in 0 until gradientMagnitude.rows()) {
            for (y in 0 until gradientMagnitude.cols()) {
                if (gradientMagnitude.get(x, y)[0] < HogConfig.magnitudeThreshold) {
                    gradientMagnitude.put(x, y, 0.0)
                }
            }
        }

        //테스트 결과 여기까진 잘나옴
        return Pair(gradientMagnitude, gradientOrientation)
    }

    private fun getHistogram(
        magnitude: Mat,
        orientation: Mat
    ): DoubleArray {
        val maxDegree = 180.0
        val diff = maxDegree / HogConfig.nBins

        val histogram = DoubleArray(HogConfig.nBins)

        val cellSize = magnitude.size()

        for (x in 0 until cellSize.width.toInt()) {
            for (y in 0 until cellSize.height.toInt()) {
                val magValue = magnitude[x, y][0]
                val orientationValue = orientation[x, y][0]

                if (magValue < HogConfig.magnitudeThreshold)
                    continue

                val index = (orientationValue / diff).toInt()
                val deg = index * diff
                histogram[index] += magValue * (1 - (orientationValue - deg) / diff)

                val nextIndex = (index + 1) % HogConfig.nBins
                histogram[nextIndex] += magValue * ((orientationValue - deg) / diff)
            }
        }

        return histogram
    }

    private fun getHistogramMap(image: Bitmap): Mat {
        val resizedImage = preProcessing(image)
        val (magnitude, orientation) = getGradient(resizedImage)

        val histogramMap = Mat.zeros(
            Size(
                resizedImage.width() / HogConfig.cellSize.width,
                resizedImage.height() / HogConfig.cellSize.height
            ),
            CvType.CV_64FC(HogConfig.nBins)
        )

        for (x in 0 until resizedImage.width() step HogConfig.cellSize.height.toInt()) {
            for (y in 0 until resizedImage.height() step HogConfig.cellSize.width.toInt()) {
                val xEnd = x + HogConfig.cellSize.width.toInt()
                val yEnd = y + HogConfig.cellSize.height.toInt()
                val cellMagnitude = magnitude.submat(x, xEnd, y, yEnd)
                val cellOrientation = orientation.submat(x, xEnd, y, yEnd)

                val histogram = getHistogram(
                    cellMagnitude,
                    cellOrientation
                )
                histogramMap.put(
                    x / HogConfig.cellSize.width.toInt(),
                    y / HogConfig.cellSize.height.toInt(),
                    *histogram
                )
            }
        }

        return histogramMap
    }

    //Hog 구하는 메소드
    fun getHOG(image: Bitmap): List<Double> {
        val histogramMap = getHistogramMap(image)
        val hog = mutableListOf<Double>()
        val mapSize = histogramMap.size()
        for (x in 0 until mapSize.width.toInt() - HogConfig.blockSize.width.toInt() + 1) {
            for (y in 0 until mapSize.height.toInt() - HogConfig.blockSize.height.toInt() + 1) {
                val histogramVector = mutableListOf<Double>()
                for (bx in x until x + HogConfig.blockSize.width.toInt()) {
                    for (by in y until y + HogConfig.blockSize.height.toInt()) {
                        histogramVector.addAll(histogramMap[bx, by].toList()) //값을 추가
                    }
                    //정규화 요소 구하는 공식 -> 각 값의 제곱을 더한 후 그것에 루트를 씌움 (Norm_2)
                    val norm = histogramVector.toDoubleArray().let { hist ->
                        var res = 0.0
                        hist.forEach {
                            res += it.pow(2)
                        }
                        if (sqrt(res) == 0.0) 1.0
                        else sqrt(res)

                    } //정규화 요소
                    val normVector = histogramVector.map { it / norm } //정규화된 벡터값
                    hog.addAll(normVector)
                }
            }
        }
        return hog

    }


    fun showRecommendPose(targetBackground: Bitmap, context: Context) {
        val hog = getHOG(targetBackground) //HoG 값 가져오기
        //
        val centroid = context.assets.open("centroids.csv").use {

        }

    }

}