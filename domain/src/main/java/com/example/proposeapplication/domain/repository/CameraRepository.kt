package com.example.proposeapplication.domain.repository

import android.graphics.Bitmap
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner


//카메라 기능을 담당하는 레포지토리
interface CameraRepository {
    fun initCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,
    )

    suspend fun takePhoto(): Bitmap
    fun setZoomRatio(zoomLevel: Float)

}