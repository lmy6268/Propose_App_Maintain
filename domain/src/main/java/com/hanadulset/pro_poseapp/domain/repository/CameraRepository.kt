package com.hanadulset.pro_poseapp.domain.repository

import androidx.camera.core.ImageAnalysis
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

    suspend fun takePhoto(isFixedRequest: Boolean): Any
    fun setZoomRatio(zoomLevel: Float)

}