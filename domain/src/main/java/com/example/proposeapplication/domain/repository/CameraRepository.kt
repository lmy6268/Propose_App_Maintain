package com.example.proposeapplication.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import android.view.Display
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner


interface CameraRepository {
    fun initPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer
    )

    suspend fun takePhoto(): Bitmap
    suspend fun getFixedScreen(rawBitmap: Bitmap): Bitmap?
    fun getLatestImage(): Bitmap?
    fun setZoomRatio(zoomLevel: Float)

    suspend fun compositionData(bitmap: Bitmap): String
}