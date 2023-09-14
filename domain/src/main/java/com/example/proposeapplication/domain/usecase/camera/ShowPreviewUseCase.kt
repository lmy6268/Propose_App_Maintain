package com.example.proposeapplication.domain.usecase.camera


import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class ShowPreviewUseCase @Inject constructor(
    private val repository: CameraRepository,
    private val imageRepository: ImageRepository
) {
    operator fun invoke(
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
    ).apply {
        imageRepository.preRunModel()
    }
}