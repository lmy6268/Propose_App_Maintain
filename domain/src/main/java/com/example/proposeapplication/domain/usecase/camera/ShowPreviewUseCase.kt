package com.example.proposeapplication.domain.usecase.camera


import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner
import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class ShowPreviewUseCase @Inject constructor(private val repository: CameraRepository) {
    operator fun invoke(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer
    ) =
        repository.initPreview(lifecycleOwner, surfaceProvider, ratio, analyzer)
}