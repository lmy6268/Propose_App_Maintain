package com.hanadulset.pro_poseapp.domain.usecase.camera


import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class ShowPreviewUseCase @Inject constructor(
    private val repository: CameraRepository,
) {
    suspend operator fun invoke(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: Analyzer,

        ) = repository.initCamera(
        lifecycleOwner,
        surfaceProvider,
        aspectRatio,
        previewRotation,
        analyzer
    )
}