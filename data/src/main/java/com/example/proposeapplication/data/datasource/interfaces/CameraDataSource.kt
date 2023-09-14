package com.example.proposeapplication.data.datasource.interfaces

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner

/**
 * 카메라를 조작하는 데이터 소스
 * */
interface CameraDataSource {
    /** 카메라 미리보기 조작
     * */
    fun initCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer
    )

    suspend fun takePhoto(isFixedRequest: Boolean): Any
    fun setZoomLevel(zoomLevel: Float)

}