package com.hanadulset.pro_poseapp.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

fun imageToBitmap(image: Image, imageRotation: Int): Bitmap {
    var res: Bitmap? = null
    when (image.format) {
        ImageFormat.YUV_420_888 -> {
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
        }

        ImageFormat.JPEG -> {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
            res = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        else -> {}
    }
    //만약 회전이 필요한 경우, 회전 적용
    if (imageRotation != 0) {
        res = Bitmap.createBitmap(
            res!!,
            0,
            0,
            res.width,
            res.height,
            Matrix().apply { postRotate(imageRotation.toFloat()) },
            true
        )
    }
    return res!!
}