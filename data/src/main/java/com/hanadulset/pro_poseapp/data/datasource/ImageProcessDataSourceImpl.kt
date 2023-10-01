package com.hanadulset.pro_poseapp.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.video.TrackerMIL
import org.opencv.video.TrackerMIL_Params
import java.io.ByteArrayOutputStream


//이미지 처리
class ImageProcessDataSourceImpl() : ImageProcessDataSource {

    private var tracker: TrackerMIL? = null
    private var startOffset: Pair<Float, Float>? = null
    private var roi: org.opencv.core.Rect? = null
    private var afterFirstFrame = false


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


    fun bitmapToMatWithOpenCV(bitmap: Bitmap): Mat {
        val resMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        Utils.bitmapToMat(bitmap, resMat)
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_RGB2GRAY)
        return resMat
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

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun trackingXYPoint(
        inputFrame: ImageProxy, //입력 프레임
        inputOffset: Pair<Float, Float>, //구도추천 포인트의 Centroid
        radius: Int //
    ): Pair<Float, Float> {
        var isTrackingAvailable = false
        //트래커 초기화
        if (tracker == null) tracker = TrackerMIL.create()
        val input = bitmapToMatWithOpenCV(
            imageToBitmap(
                image = inputFrame.image!!,
                rotation = inputFrame.imageInfo.rotationDegrees
            )
        )//입력 프레임
        if (startOffset == null) startOffset = inputOffset
        if (roi == null) {
            roi = org.opencv.core.Rect(
                (startOffset!!.first - radius.toFloat()).toInt(), //사각형의 왼쪽 상단의 x좌표
                (startOffset!!.second - radius.toFloat()).toInt(), //사각형의 왼쪽 상단의 y좌표
                2 * radius, 2 * radius //사각형의 가로, 세로
            )
            tracker!!.init(input, roi!!) //트래커 초기화
            isTrackingAvailable = true
            afterFirstFrame = true //첫프레임 끝남을 알림
        } else if (afterFirstFrame) {
            isTrackingAvailable = tracker!!.update(input, roi!!) //값 업데이트
        }
        Log.d("트래킹 활성화: ", isTrackingAvailable.toString())


        return Pair(
            (roi!!.x + radius).toFloat(), (roi!!.y + radius).toFloat() //Centroid 값으로 변경
        )
    }

    override fun stopTracking() {
        startOffset = null
        tracker = null
        roi = null
        afterFirstFrame = false
    }


}