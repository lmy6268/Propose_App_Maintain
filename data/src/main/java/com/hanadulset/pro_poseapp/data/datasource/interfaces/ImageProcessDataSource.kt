package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import org.opencv.core.Size


//이미지 저장 등과 같은 처리 담당
interface ImageProcessDataSource {
    fun getFixedImage(bitmap: Bitmap): Bitmap
    fun resizeBitmap(
        bitmap: Bitmap,
        width: Double = 5.0,
        height: Double = 5.0,
        isScaledResize: Boolean
    ): Bitmap

    fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap

    fun imageToBitmap(image: Image, rotation: Int): Bitmap

    suspend fun trackingXYPoint(
        inputFrame: ImageProxy,
        inputOffset: Pair<Float, Float>,
        radius: Int
    ): Pair<Float, Float>
    fun stopTracking()
}