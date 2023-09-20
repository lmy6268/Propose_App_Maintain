package com.hanadulset.pro_poseapp.data.datasource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ImageProcessDataSource
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream


//이미지 처리
class ImageProcessDataSourceImpl() : ImageProcessDataSource {

    override fun getFixedImage(bitmap: Bitmap): Bitmap {
        // No implementation found ~ 에러 해결
        OpenCVLoader.initDebug()
        val input = Mat()
        Utils.bitmapToMat(bitmap, input) // bitmap을 매트릭스로 변환
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY) //흑백으로 변경
        //Convert to detected picture
        Imgproc.Canny(input, input, 50.0, 150.0)

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


    override fun imageToBitmap(image: Image, rotation: Int): Bitmap {
        var res: Bitmap
        if (image.format == ImageFormat.YUV_420_888) {
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
        } else {
            val buffer = image.planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            res = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
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
//            res = rotatedBitmap(res, rotation)
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

    private fun rotatedBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        val mat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, mat)
        val centerPoint = Point(mat.width() / 2.0, mat.height() / 2.0)
        val M = Imgproc.getRotationMatrix2D(
            centerPoint,
            rotation.toDouble(),
            1.0
        )
        Imgproc.warpAffine(mat, mat, M, Size(0.0, 0.0))
        Utils.matToBitmap(
            mat,
            Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        )
        return bitmap
    }

}