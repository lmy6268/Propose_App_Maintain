package com.example.proposeapplication.utils

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner

interface CameraControllerInterface {
    fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer
    )

   suspend fun takePhoto(): Bitmap?
    fun getLatestImage(): Bitmap?

}