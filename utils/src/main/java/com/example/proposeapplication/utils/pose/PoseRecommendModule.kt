package com.example.proposeapplication.utils.pose

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.cvtColor
import kotlin.math.abs
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

    private fun preProcessing(image: Bitmap): Mat {
        OpenCVLoader.initDebug() //초기화
        val resizedImage =
            Bitmap.createScaledBitmap(
                image,
                HogConfig.imageResize.width.toInt(),
                HogConfig.imageResize.height.toInt(),
                true
            )
        return Mat().apply {
            Utils.bitmapToMat(resizedImage, this)
        }.apply {
            cvtColor(this, this, HogConfig.imageConvert) //흑백으로 변환
        }

    }


    private fun getGradient(image: Mat): Pair<Mat, Mat> {
        var gradientX = Mat()
        var gradientY = Mat()

        Imgproc.Sobel(image, gradientX, CvType.CV_64F, 1, 0, 3)
        Imgproc.Sobel(image, gradientY, CvType.CV_64F, 0, 1, 3)
        gradientX = gradientX.apply {
            val std = Core.minMaxLoc(this)
            val dv = if (abs(std.maxVal) > abs(std.minVal)) abs(std.maxVal) else abs(std.minVal)
            Core.divide(dv, this, this)
        }
        gradientY = gradientY.apply {
            val std = Core.minMaxLoc(this)
            val dv = if (abs(std.maxVal) > abs(std.minVal)) abs(std.maxVal) else abs(std.minVal)
            Core.divide(dv, this, this)
        }

//
//        //0~255값으로 정규화


        val gradientMagnitude = Mat().apply {
            val tmpX = Mat()
            val tmpY = Mat()
            Core.pow(gradientX, 2.0, tmpX)
            Core.pow(gradientY, 2.0, tmpY)
            Core.add(tmpX, tmpY, tmpX)
            Core.sqrt(tmpX, this)
        }
        val gradientOrientation = Mat().apply {
            //https://docs.opencv.org/3.4/d2/de8/group__core__array.html#ga9db9ca9b4d81c3bde5677b8f64dc0137
            Core.phase(gradientX, gradientY, this, true)
        }


        for (x in 0 until gradientMagnitude.rows()) {
            for (y in 0 until gradientMagnitude.cols()) {
                if (gradientMagnitude.get(x, y)[0] < HogConfig.magnitudeThreshold) {
                    gradientMagnitude.put(x, y, *doubleArrayOf(0.0))
                }
            }
        }

        return Pair(gradientMagnitude, gradientOrientation)
    }

    private fun getHistogram(
        magnitude: Mat,
        orientation: Mat
    ): DoubleArray {
        val maxDegree = 180.0
        val diff = maxDegree / HogConfig.nBins

//        val degreeAxis = DoubleArray(nBins) { it * diff }
        val histogram = DoubleArray(HogConfig.nBins)

        val cellSize = magnitude.size()

        for (x in 0 until cellSize.height.toInt()) {
            for (y in 0 until cellSize.width.toInt()) {
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

        for (x in 0 until resizedImage.height() step HogConfig.cellSize.height.toInt()) {
            for (y in 0 until resizedImage.width() step HogConfig.cellSize.width.toInt()) {
                val xEnd = x + HogConfig.cellSize.height.toInt()
                val yEnd = y + HogConfig.cellSize.width.toInt()

                val cellMagnitude = magnitude.submat(x, xEnd, y, yEnd)
                val cellOrientation = orientation.submat(x, xEnd, y, yEnd)

                val histogram = getHistogram(
                    cellMagnitude,
                    cellOrientation
                )
                histogramMap.put(
                    x / HogConfig.cellSize.height.toInt(),
                    y / HogConfig.cellSize.width.toInt(),
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
                        sqrt(res)
                    } //정규화 요소
                    val normVector = histogramVector.map { it / norm } //정규화된 벡터값
                    hog.addAll(normVector)
                }
            }
        }
        return hog

    }
}