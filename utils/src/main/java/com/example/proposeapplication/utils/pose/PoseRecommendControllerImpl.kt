package com.example.proposeapplication.utils.pose

import android.content.Context
import android.graphics.Bitmap
import com.example.proposeapplication.utils.TorchController
import com.opencsv.CSVReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow
import kotlin.math.sqrt


class PoseRecommendControllerImpl(private val applicationContext: Context) :
    PoseRecommendController {

    internal object HogConfig {
        val imageResize: Size = Size(128.0, 128.0)
        const val imageConvert: Int =
            Imgproc.COLOR_BGR2GRAY  //cv2.COLOR_BGR2GRAY | cv2.COLOR_BGR2RGB | cv2.COLOR_BGR2HSV
        val cellSize: Size = Size(16.0, 16.0)
        val blockSize: Size = Size(1.0, 1.0)
        const val magnitudeThreshold: Int = 151
        const val nBins: Int = 9
    }


    private val torchController by lazy {
        TorchController(applicationContext)
    }


    //centroid 값
    private val centroid by lazy {
        applicationContext.assets.open("centroids.csv").use { stream ->
            val resMutableList = mutableListOf<List<Double>>()
            CSVReader(InputStreamReader(stream)).forEach {
                //앞에 라벨 번호가 있는 것들만 데이터를 가져와보자
                if (!it.contains("label")) {
                    val tmpList = mutableListOf<Double>()
                    val length = it.size
                    tmpList.add(it[1].substring(1).toDouble())
                    for (i in 2 until length - 1) tmpList.add(it[i].toDouble())
                    tmpList.add(
                        it[length - 1].substring(0 until it[length - 1].length - 1).toDouble()
                    )
                    resMutableList.add(tmpList)
                }
            }
            resMutableList
        }
    }

    private val poseRanks by lazy {
        applicationContext.assets.open("pose_ranks.csv").use { stream ->
            val resMutableList = mutableListOf<List<Double>>()
            CSVReader(InputStreamReader(stream)).forEach { strings ->
                if (strings[1].equals("pose_ids").not()) {
                    val listStrData = strings[1].substring(1, strings[1].length - 1)
                    listStrData.split(',').map { it.toDouble() }.let {
                        resMutableList.add(it)
                    }
                }
            }
            resMutableList
        }
    }

    //포즈 추천 메소드
    override suspend fun getRecommendPose(backgroundImage: Bitmap): String =
        suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                val hogResult = async { getHOG(backgroundImage) }.await() //HoG 결과
                val resFeature = async {
                    torchController.runResNet(backgroundImage).map { it.toDouble() } //ResNet

                }.await() //ResNet 결과
//                var res = Pair(-1, POSITIVE_INFINITY)
//                for (i in centroid.indices) {
//                    val calculatedDistance = getDistance(hogResult, resFeature, i)
//                    if (res.first > calculatedDistance)
//                        res = res.copy(first = i, second = calculatedDistance)
//                }

                cont.resume(resFeature.toString())
            }
        }

    private fun getDistance(hog: List<Double>, resnet50: List<Double>, centroidIdx: Int): Double {
        val weight = 50.0
        val (centroidResNet50, centroidGHog) = Pair(
            centroid[centroidIdx].subList(0, 2048),
            centroid[centroidIdx].subList(2048, centroid[centroidIdx].size)
        )
        val distanceResNet50 = resnet50.zip(centroidResNet50).map {
            sqrt(it.first.pow(2) - it.second.pow(2)) //np.linalg.norm(A - B, axis = 0)
        }.let {
            it.sum() / it.size // np.mean()
        }
//        val distanceHog =


        return weight
    }


    private fun distanceHog(aHog: List<Double>, bHog: List<Double>) {
        val imageResizeConfig = 128.0
        val cellSizeConfig = 16.0

        val histCnt = (imageResizeConfig / cellSizeConfig).pow(2).toInt()
        val binCnt = 9
        val reshapedAHog = addZDimension(reshapeList(aHog, listOf(histCnt, binCnt)))
        val reshapedBHog = addZDimension(reshapeList(bHog, listOf(histCnt, binCnt)))


    }

    private fun reshapeList(inputList: List<Double>, newShape: List<Int>): List<List<Double>> {
        val totalElements = inputList.size
        val newTotalElements = newShape.reduce { acc, i -> acc * i }

        require(totalElements == newTotalElements) { "Total elements in input list must match new shape" }

        val result = mutableListOf<List<Double>>()
        var currentIndex = 0

        for (dimension in newShape) {
            val sublist = inputList.subList(currentIndex, currentIndex + dimension)
            result.add(sublist)
            currentIndex += dimension
        }
        return result
    }

    private fun addZDimension(list: List<List<Double>>): List<List<List<Double>>> {
        val arrShape = list.size to list[0].size
        val zeroArr = List(arrShape.second) { List(arrShape.second) { 0.0 } }
        val normArr = List(zeroArr.size) { _ ->
            List(arrShape.second) { columnIndex ->
                if (columnIndex == 0 || columnIndex == 1) 1.0 else 0.0
            }
        }

        val arrTrue = list.map { row -> row.all { it == 0.0 } }
        val arrZDim = arrTrue.map { isTrue ->
            if (isTrue) normArr else zeroArr
        }

        val arrExpanded = listOf(list)

        return arrExpanded.plus(arrZDim)
    }


    //Hog 뽑기 위한 사전 작업
    override fun preProcessing(image: Bitmap): Mat {
        OpenCVLoader.initDebug() //초기화
        val resizedImageMat = Mat(image.width, image.height, CvType.CV_8UC3)
        Utils.bitmapToMat(image, resizedImageMat)
        Imgproc.cvtColor(resizedImageMat, resizedImageMat, Imgproc.COLOR_RGBA2RGB) //알파값을 빼고 저장
        Imgproc.cvtColor(resizedImageMat, resizedImageMat, HogConfig.imageConvert)
        Imgproc.resize(resizedImageMat, resizedImageMat, HogConfig.imageResize)

        return resizedImageMat
    }


    override fun getGradient(image: Mat): Pair<Mat, Mat> {
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
                if (gradientMagnitude.get(
                        x, y
                    )[0] < HogConfig.magnitudeThreshold
                ) {
                    gradientMagnitude.put(x, y, 0.0)
                }
            }
        }

        //테스트 결과 여기까진 잘나옴
        return Pair(gradientMagnitude, gradientOrientation)
    }

    override fun getHistogram(magnitude: Mat, orientation: Mat): DoubleArray {
        val maxDegree = 180.0
        val diff = maxDegree / HogConfig.nBins

        val histogram = DoubleArray(HogConfig.nBins)

        val cellSize = magnitude.size()

        for (x in 0 until cellSize.width.toInt()) {
            for (y in 0 until cellSize.height.toInt()) {
                val magValue = magnitude[x, y][0]
                val orientationValue = orientation[x, y][0]

                if (magValue < HogConfig.magnitudeThreshold) continue

                val index = (orientationValue / diff).toInt()
                val deg = index * diff
                histogram[index] += magValue * (1 - (orientationValue - deg) / diff)

                val nextIndex = (index + 1) % HogConfig.nBins
                histogram[nextIndex] += magValue * ((orientationValue - deg) / diff)
            }
        }

        return histogram
    }

    override fun getHistogramMap(image: Bitmap): Mat {
        val resizedImage = preProcessing(image)
        val (magnitude, orientation) = getGradient(resizedImage)

        val histogramMap = Mat.zeros(
            Size(
                resizedImage.width() / HogConfig.cellSize.width,
                resizedImage.height() / HogConfig.cellSize.height
            ), CvType.CV_64FC(HogConfig.nBins)
        )

        for (x in 0 until resizedImage.width() step HogConfig.cellSize.height.toInt()) {
            for (y in 0 until resizedImage.height() step HogConfig.cellSize.width.toInt()) {
                val xEnd = x + HogConfig.cellSize.width.toInt()
                val yEnd = y + HogConfig.cellSize.height.toInt()
                val cellMagnitude = magnitude.submat(x, xEnd, y, yEnd)
                val cellOrientation = orientation.submat(x, xEnd, y, yEnd)

                val histogram = getHistogram(
                    cellMagnitude, cellOrientation
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


    override fun getHOG(image: Bitmap): List<Double> {
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
}