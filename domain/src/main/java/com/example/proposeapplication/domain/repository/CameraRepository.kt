package com.example.proposeapplication.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import android.view.Display
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner


//카메라 기능을 담당하는 레포지토리
interface CameraRepository {
    fun initPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer,
    )

    suspend fun takePhoto(): Bitmap
    fun setZoomRatio(zoomLevel: Float)

}