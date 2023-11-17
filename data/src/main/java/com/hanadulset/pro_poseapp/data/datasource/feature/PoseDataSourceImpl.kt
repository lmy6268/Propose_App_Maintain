package com.hanadulset.pro_poseapp.data.datasource.feature

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.SizeF
import androidx.core.net.toUri
import com.hanadulset.pro_poseapp.data.datasource.interfaces.PoseDataSource
import com.hanadulset.pro_poseapp.utils.ImageUtils
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

class PoseDataSourceImpl(private val context: Context) : PoseDataSource {


    //centroid 값
    private val centroid by lazy {
        context.assets.open("centroids.csv").use { stream ->
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
        val poseDataList = mutableListOf<List<PoseData>>()
        val rankList = context.assets.open("pose_ranks.csv").use { stream ->
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

        //저장된 이미지와 데이터를 매핑시킨다.
        val imageDataList = context.assets.open("image_datas.csv").use { stream ->
            val resultList = mutableListOf<PoseData>()
            val imageRes = loadPoseImages()
            CSVReaderBuilder(InputStreamReader(stream)).withCSVParser(
                CSVParserBuilder().withSeparator(',').build()
            ).build().readAll().run {
                //첫 줄은 생략하고 시작함.
                this.subList(1, this.size).forEach { strings ->
                    val center =
                        strings[1].replace("[", "").replace("]", "").split(",").map {
                            it.toFloat()
                        }
                    val size = strings[2].replace("[", "").replace("]", "").split(",")
                        .map { it.toFloat() }

                    resultList.add(
                        PoseData(
                            poseId = strings[0].toInt(),
                            bottomCenterRate = SizeF(center[0], center[1]),
                            sizeRate = SizeF(size[0], size[1])
                        )
                    )
                }
                resultList.sortBy { it.poseId }
                for (i in resultList.indices) {
                    resultList[i] = resultList[i].copy(imageUri = imageRes[i])
                }
            }
            resultList
        }


        for (idx in rankList.indices) {
            val tmp = mutableListOf<PoseData>()
            rankList[idx].forEach { poseId ->
                tmp.add(imageDataList[poseId.toInt()].copy(poseCat = idx))
            }
            poseDataList.add(tmp)
        }
        poseDataList.toList()
    }


    override suspend fun recommendPose(backgroundBitmap: Bitmap): PoseDataResult =
        withContext(Dispatchers.IO) {
            val angle = getAngleFromHog(getHistogramMap(backgroundBitmap))
            var res = Pair(-1, java.lang.Double.POSITIVE_INFINITY)
            for (i in 0 until centroid.size - 2) {
                val calculatedDistance = getDistance(angle, i)
                if (res.second > calculatedDistance) res =
                    res.copy(first = i, second = calculatedDistance)
            }
            val backgroundId = res.first //백그라운드 클러스터 ID
            val poseDataResult = PoseDataResult(
                poseDataList = poseRanks[backgroundId].toMutableList(),
                backgroundId = backgroundId,
                backgroundAngleList = angle
            )
            poseDataResult
        }


    override fun preProcessing(image: Bitmap): Mat {

        val resizedImageMat = Mat(image.width, image.height, CvType.CV_8UC3)
        Utils.bitmapToMat(image, resizedImageMat)
        Imgproc.cvtColor(resizedImageMat, resizedImageMat, Imgproc.COLOR_RGBA2RGB) //알파값을 빼고 저장
//        Imgproc.cvtColor(resizedImageMat, resizedImageMat, HogConfig.imageConvert)
        Imgproc.resize(resizedImageMat, resizedImageMat, HogConfig.imageResize)
        //10.01 추가
        Imgproc.medianBlur(resizedImageMat, resizedImageMat, HogConfig.blurSize.width.toInt())
        return resizedImageMat
    }

    override fun getDistance(angle: List<Double>, centroidIdx: Int): Double {
        val weight = 50.0
        val centroidGHog = centroid[centroidIdx]
        val distanceHog = distanceAngle(angle, centroidGHog)
        return weight * distanceHog
    }


    override fun distanceAngle(aAngle: List<Double>, bAngle: List<Double>): Double {
        var distance = 0.0
        for (idx in aAngle.indices) {
            distance += if ((aAngle[idx] == -1.0 && bAngle[idx] != -1.0) || (aAngle[idx] != -1.0 && bAngle[idx] == -1.0)) 1.0
            else 1 - abs(cos(aAngle[idx] - bAngle[idx]))
        }
        return distance
    }

    override fun distanceHog(aHog: List<Double>, bHog: List<Double>): Double {
        val imageResizeConfig = 128.0
        val cellSizeConfig = 16.0
        val histCnt = (imageResizeConfig / cellSizeConfig).pow(2).toInt()
        val binCnt = 9
        val reshapedAHog =
            reshapeList(aHog, listOf(histCnt, binCnt)).toMutableList().apply { addZDimension(this) }
        val reshapedBHog = reshapeList(bHog, listOf(histCnt, binCnt)).toMutableList().apply {
            addZDimension(this)
        }
        return calculateDistance(reshapedAHog, reshapedBHog)
    }

    override fun addZDimension(arr: MutableList<List<Double>>) {
        val arrShape = arr.size
        val zeroArr = List(arrShape) { 0.0 }
        val normArr = MutableList(arrShape) { 0.0 }
        normArr[0] = 1.0
        normArr[1] = 1.0

        val arrTrue = arr.map { sublist ->
            sublist.all { value -> abs(value) < 1e-6 } // Adjust tolerance as needed
        }
        val arrZDim = arrTrue.mapIndexed { index, value ->
            if (value) normArr else zeroArr
        }

        val arrZDimReshaped = arrZDim.flatten()
        val arrConcat = arr.mapIndexed { index, sublist ->
            sublist + arrZDimReshaped[index]
        }
        arr.clear()
        arr.addAll(arrConcat)
    }

    override fun calculateDistance(a: List<List<Double>>, b: List<List<Double>>): Double {
        require(a.size == b.size && a.isNotEmpty()) { "Input lists must have the same non-empty size." }

        val numElements = a.size * a[0].size

        val diffSquaredSum = a.zip(b).sumOf { (aSublist, bSublist) ->
            require(aSublist.size == bSublist.size) { "Sublists must have the same size." }

            aSublist.zip(bSublist).sumOf { (aVal, bVal) ->
                val diff = aVal - bVal
                diff * diff
            }
        }

        return sqrt(diffSquaredSum) / numElements
    }

    override fun reshapeList(inputList: List<Double>, newShape: List<Int>): List<List<Double>> {
        val totalElements = inputList.size
        val newTotalElements = newShape.reduce { acc, i -> acc * i }

        require(totalElements == newTotalElements) { "Total elements in input list must match new shape, total: $totalElements / new:$newTotalElements" }

        val result = mutableListOf<List<Double>>()
        var currentIndex = 0

        for (dimension in newShape) {
            val sublist = inputList.subList(currentIndex, currentIndex + dimension)
            result.add(sublist)
            currentIndex += dimension
        }
        return result
    }

    override fun getGradient(targetImage: Mat): Pair<Mat, Mat> {
        var gradientX = Mat(targetImage.size(), targetImage.type())
        var gradientY = Mat(targetImage.size(), targetImage.type())

        Imgproc.Sobel(targetImage, gradientX, CvType.CV_64F, 1, 0, 3)
        Imgproc.Sobel(targetImage, gradientY, CvType.CV_64F, 0, 1, 3)

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
//                if (adjustedPhase >= 180.0) {
//                    adjustedPhase -= 180.0
//                }
                adjustedPhase %= 180
                gradientOrientation.put(row, col, adjustedPhase)
            }
        }


//        for (x in 0 until gradientMagnitude.rows()) {
//            for (y in 0 until gradientMagnitude.cols()) {
//                if (gradientMagnitude.get(
//                        x, y
//                    )[0] < HogConfig.magnitudeThreshold
//                ) {
//                    gradientMagnitude.put(x, y, 0.0)
//                }
//            }
//        }
        return Pair(gradientMagnitude, gradientOrientation)
    }

    override fun getHistogram(magnitude: Mat, orientation: Mat): DoubleArray {
        val maxDegree = 180.0
        val diff = maxDegree / HogConfig.nBins

        var histogram = DoubleArray(HogConfig.nBins)

        val cellSize = magnitude.size()

        for (x in 0 until cellSize.width.toInt()) {
            for (y in 0 until cellSize.height.toInt()) {
                val magValue = magnitude[x, y][0]
                val orientationValue = orientation[x, y][0]

//                if (magValue < HogConfig.magnitudeThreshold) continue

                val index = (orientationValue / diff).toInt()
                val deg = index * diff
                histogram[index] += magValue * (1 - (orientationValue - deg) / diff)

                val nextIndex = (index + 1) % HogConfig.nBins
                histogram[nextIndex] += magValue * ((orientationValue - deg) / diff)
            }
        }

        val maxVal = histogram.max()

        histogram = histogram.map {
            if (it == maxVal && maxVal != 0.0) 1.0
            else 0.0
        }.toDoubleArray()

        return histogram
    }

    override fun getHistogramMap(backgroundBitmap: Bitmap): Mat {
        val resizedImage = preProcessing(backgroundBitmap) //이미지

        val resizedImageMats = arrayListOf(
            Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_8UC1),
            Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_8UC1),
            Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_8UC1)
        )
        Core.split(resizedImage, resizedImageMats)
        val resList = ArrayList<Pair<Mat, Mat>>()
        resizedImageMats.forEach {
            resList.add(getGradient(it)) //여기서 값이 들어감
        }
        val resListMagnitudeDump = mutableListOf<List<List<Double>>>()
        val resListOrientationDump = mutableListOf<List<List<Double>>>()
        val resDump = mutableListOf<List<List<List<Double>>>>()
        for (pair in resList) {
            resListMagnitudeDump.add(pair.first.to3dList()[0])
            resListOrientationDump.add(pair.second.to3dList()[0])
        }
        for (i in resListMagnitudeDump.indices) {
            resDump.add(listOf(resListMagnitudeDump[i], resListOrientationDump[i]))
        }


        //변수 초기화 -> 모든 칸의 값을 0으로 초기화하여 진행한다.
        val resMagnitude = Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_64FC1)
        val resOrientation = Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_64FC1)
        val cnt = Mat.zeros(resizedImage.width(), resizedImage.height(), CvType.CV_8UC1)


        resList.forEachIndexed { idx, it ->
            val magnitude = it.first
            val orientation = it.second
            for (row in 0 until magnitude.rows()) {
                for (col in 0 until magnitude.cols()) {
                    resMagnitude.put(row, col, resMagnitude[row, col][0] + magnitude[row, col][0])
                    if (magnitude[row, col][0] != 0.0) {
                        resOrientation.put(
                            row, col, resOrientation[row, col][0] + orientation[row, col][0]
                        )
                        cnt.put(
                            row, col, cnt[row, col][0] + 1
                        )
                    }
                }
            }
        }

        for (row in 0 until cnt.rows()) {
            for (col in 0 until cnt.cols()) {
                val currentCnt = cnt[row, col][0]
                if (currentCnt != 0.0) {
                    resMagnitude.put(
                        row, col, resMagnitude[row, col][0] / currentCnt
                    )
                    resOrientation.put(
                        row, col, resOrientation[row, col][0] / currentCnt
                    )
                }
            }
        }


        val ave =
            Core.sumElems(resMagnitude).`val`[0] / (resMagnitude.width() * resMagnitude.height())
        for (row in 0 until cnt.rows()) {
            for (col in 0 until cnt.cols()) {
                resMagnitude.put(
                    row,
                    col,
                    if (ave * HogConfig.magnitudeThreshold / 10 > resMagnitude[row, col][0]) 0.0
                    else 1.0
                )
            }
        }
        val arrayList = ArrayList<Double>()
        for (row in 0 until cnt.rows()) {
            for (col in 0 until cnt.cols()) {
                arrayList.add(resMagnitude[row, col][0])
                continue
            }
        }


        val histogramMap = Mat.zeros(
            Size(
                resizedImage.width() / HogConfig.cellSize.width,
                resizedImage.height() / HogConfig.cellSize.height
            ), CvType.CV_64FC(HogConfig.nBins)
        )

        Mat.zeros(
            Size(
                resizedImage.width() / HogConfig.cellSize.width,
                resizedImage.height() / HogConfig.cellSize.height
            ), CvType.CV_64FC(HogConfig.nBins)
        )

        for (x in 0 until resizedImage.width() step HogConfig.cellSize.height.toInt()) {
            for (y in 0 until resizedImage.height() step HogConfig.cellSize.width.toInt()) {
                val xEnd = x + HogConfig.cellSize.width.toInt()
                val yEnd = y + HogConfig.cellSize.height.toInt()
                val cellMagnitude = resMagnitude.submat(x, xEnd, y, yEnd)
                val cellOrientation = resOrientation.submat(x, xEnd, y, yEnd)

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

    override fun getHOG(backgroundBitmap: Bitmap): List<Double> {
        val histogramMap = getHistogramMap(backgroundBitmap)
        val hog = mutableListOf<Double>()
        val mapSize = histogramMap.size()
        for (x in 0 until mapSize.width.toInt() - HogConfig.blockSize.width.toInt() + 1) {
            for (y in 0 until mapSize.height.toInt() - HogConfig.blockSize.height.toInt() + 1) {
                val histogramVector = mutableListOf<Double>()
                for (bx in x until x + HogConfig.blockSize.width.toInt()) {
                    for (by in y until y + HogConfig.blockSize.height.toInt()) {
                        histogramVector.addAll(histogramMap[bx, by].toList()) //값을 추가
                    }
//                    //정규화 요소 구하는 공식 -> 각 값의 제곱을 더한 후 그것에 루트를 씌움 (Norm_2)
//                    val norm = histogramVector.toDoubleArray().let { hist ->
//                        var res = 0.0
//                        hist.forEach {
//                            res += it.pow(2)
//                        }
//                        if (sqrt(res) == 0.0) 1.0
//                        else sqrt(res)
//
//                    } //정규화 요소
//                    val normVector = histogramVector.map { it / norm } //정규화된 벡터값
                    hog.addAll(histogramVector)
                }
            }
        }
        return hog
    }

    override fun getAngleFromHog(histogramMap: Mat): List<Double> {
        val angleMap = MutableList(histogramMap.rows() * histogramMap.cols()) { -1.0 }

        for (row in 0 until histogramMap.rows()) {
            for (col in 0 until histogramMap.cols()) {
                if (histogramMap[row, col].all { it == 0.0 }
                        .not()) for (index in histogramMap[row, col].indices) {
                    val value = histogramMap[row, col][index]
                    if (value == 1.0) {
                        //angleMap에 데이터가 저장됨
                        angleMap[row * histogramMap.rows() + col] = index * 180.0 / HogConfig.nBins
                        break
                    }
                }
            }
        }

        return angleMap.toList()
    }

    private fun loadPoseImages(): List<Uri> {
        val file = File(context.dataDir, "/silhouettes")
//        if (file.exists().not()) {
        val tmpFile = File(context.dataDir, SILHOUETTE_IMAGE_ZIP)
        //데이터를 옮김
        context.assets.open(SILHOUETTE_IMAGE_ZIP).use { `is` ->
            FileOutputStream(tmpFile).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
        }
        unzip(tmpFile, file.parentFile!!.absolutePath)
//        }

        return mutableListOf<Uri>().let { uriList ->
            mutableListOf<Pair<Int, Uri>>().run {
                file.listFiles()?.forEach { image ->
                    val name = image.name.replace(".png", "").toInt()
                    val uri = image.toUri()
                    add(Pair(name, uri))
                }
                sortBy { imageData -> imageData.first }
                forEach {
                    uriList.add(it.first, it.second)
                }
            }
            uriList
        }

    }

    private fun unzip(sourceZip: File, targetDir: String) {
        val dir = File(targetDir)
        if (dir.exists().not()) dir.mkdir()
        ImageUtils.unZip(sourceZip, targetDir)
    }


    companion object {

        object HogConfig {
            val imageResize: Size = Size(128.0, 128.0)
            const val imageConvert: Int =
                Imgproc.COLOR_BGR2GRAY  //cv2.COLOR_BGR2GRAY | cv2.COLOR_BGR2RGB | cv2.COLOR_BGR2HSV
            val cellSize: Size = Size(16.0, 16.0)
            val blurSize = Size(31.0, 31.0)
            val blockSize: Size = Size(1.0, 1.0)
            const val magnitudeThreshold: Int = 25 //10.01 수정
            const val nBins: Int = 18 //10.01 수정

        }

        const val SILHOUETTE_IMAGE_ZIP = "silhouette_image.zip"

    }


    private fun Mat.to3dList(): List<List<List<Double>>> {
        val channels = this.channels()
        val height = this.height()
        val width = this.width()

        return List(channels) { channel ->
            List(height) { y ->
                List(width) { x ->
                    this.get(x, y)[channel]
                }
            }
        }
    }
}