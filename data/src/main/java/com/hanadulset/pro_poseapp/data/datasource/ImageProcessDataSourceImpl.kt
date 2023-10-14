package com.hanadulset.pro_poseapp.data.datasource

//import org.opencv.core.Size
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.video.TrackerMIL
import org.opencv.video.Video
import java.io.ByteArrayOutputStream
import kotlin.math.pow
import kotlin.math.sqrt


//이미지 처리
class ImageProcessDataSourceImpl() : ImageProcessDataSource {

    private var tracker: TrackerMIL? = null
    private var startOffset: Pair<Float, Float>? = null
    private var roi: org.opencv.core.Rect? = null
    private var afterFirstFrame = false
    private var prevFrame: Mat? = null
    private var prevPoint: SizeF? = null
    private var prevCornerPoint: MatOfPoint2f? = null

    override fun getFixedImage(bitmap: Bitmap): Bitmap {
        // No implementation found ~ 에러 해결
        OpenCVLoader.initDebug()
        val input = Mat()
        Utils.bitmapToMat(bitmap, input) // bitmap을 매트릭스로 변환
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY) //흑백으로 변경
        //Convert to detected picture
//        Imgproc.adaptiveThreshold(input, input, 255.0,  Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,9,4.5)
        Imgproc.Canny(input, input, 50.0, 150.0)
        return bitmap.copy(bitmap.config, true).apply {
            Utils.matToBitmap(input, this)
        }
    }


    private fun bitmapToMatWithOpenCV(bitmap: Bitmap): Mat {
        val resMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        Utils.bitmapToMat(bitmap, resMat)
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_RGB2GRAY)
        return resMat
    }

    override fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: org.opencv.core.Size): Bitmap {
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat, inputImageMat, Imgproc.COLOR_RGBA2RGB) //알파값을 빼고 저장
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        return outputResizeBitmap
    }


    fun convertCaptureImageToBitmap(image: Image, rotation: Int): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        var res = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (rotation != 0) {
            res = Bitmap.createBitmap(
                res,
                0,
                0,
                res.width,
                res.height,
                Matrix().apply { postRotate(rotation.toFloat()) },
                true
            )
        }
        return res


    }

    override fun imageToBitmap(image: Image, rotation: Int): Bitmap {
        var res: Bitmap
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage =
            YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        res = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        //만약 회전이 필요한 경우
        if (rotation != 0) {
            res = Bitmap.createBitmap(
                res,
                0,
                0,
                res.width,
                res.height,
                Matrix().apply { postRotate(rotation.toFloat()) },
                true
            )
        }
        return res
    }

    //이미지를 원하는 사이즈로 리사이즈 하는 함수
    override fun resizeBitmap(
        bitmap: Bitmap,
        width: Double,
        height: Double,
        isScaledResize: Boolean
    ): Bitmap {
        val size = org.opencv.core.Size(width, height)

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
    
    override suspend fun useOpticalFlow(image: Image, targetOffset: SizeF, rotation: Int): SizeF? {

        //이전 프레임이 없는 경우, 트래킹을 하지 않는다.

        if (prevFrame == null) {
            val prevBitmap = imageToBitmap(image, rotation)

            prevFrame =
                bitmapToMatWithOpenCV(prevBitmap)
            prevPoint = targetOffset

            prevCornerPoint = MatOfPoint().apply {
                Imgproc.goodFeaturesToTrack(prevFrame, this, 1000, 0.01, 10.0)
            }.let { goodCorner -> MatOfPoint2f().apply { fromList(goodCorner.toList()) } }

            return targetOffset
        } //흑백이미지 Matrix

        else {
            val outputBitmap = imageToBitmap(image, rotation)
            val outputFrame =
                bitmapToMatWithOpenCV(outputBitmap) //흑백이미지 Matrix

            val outputState = MatOfByte()
            val outputErr = MatOfFloat()
            val outputCornerPoint = MatOfPoint().apply {
                Imgproc.goodFeaturesToTrack(outputFrame, this, 1000, 0.01, 10.0)
            }.let { goodCorner -> MatOfPoint2f().apply { fromList(goodCorner.toList()) } }

            Video.calcOpticalFlowPyrLK(
                prevFrame,
                outputFrame,
                prevCornerPoint,
                outputCornerPoint,
                outputState,
                outputErr,
//                lkParams["winSize"] as org.opencv.core.Size,
//                lkParams["maxLevel"] as Int,
//                lkParams["criteria"] as TermCriteria
            )
            val outputPoint =
                calculateOffsetDiff(prevCornerPoint!!, outputCornerPoint, targetOffset)

            Log.d(
                "state array: ",
                "state : ${outputState.toList()} , output: $outputPoint"
            )

            prevFrame = outputFrame //업데이트
            prevPoint = outputPoint
            prevCornerPoint = outputCornerPoint


            return outputPoint
        }
    }


    private fun calculateOffsetDiff(
        prevCornerPoint: MatOfPoint2f,
        outputCornerPoint: MatOfPoint2f,
        targetOffset: SizeF
    ): SizeF {
        val distanceArr = FloatArray(outputCornerPoint.toList().size)

        for (i in 0 until outputCornerPoint.toList().size) {
            val outputPoint = outputCornerPoint.toList()[i]
            distanceArr[i] = sqrt(
                (outputPoint.x - targetOffset.width).pow(2) + (outputPoint.y - targetOffset.height).pow(
                    2
                )
            ).toFloat()
        }
        val index = distanceArr.indices.minBy { distanceArr[it] }

        val prevX = prevCornerPoint.toList()[index].x
        val prevY = prevCornerPoint.toList()[index].y

        val outPutX = outputCornerPoint.toList()[index].x
        val outPutY = outputCornerPoint.toList()[index].y

        return targetOffset.let {
            SizeF(
                it.width + (outPutX - prevX).toFloat(),
                it.height + (outPutY - prevY).toFloat()
            )
        }
    }


    override fun stopToUseOpticalFlow() {
        prevFrame = null
        prevPoint = null
        prevCornerPoint = null
    }


}