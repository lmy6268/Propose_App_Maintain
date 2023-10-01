package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class GetTrackingDataUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    suspend operator fun invoke(
        inputFrame: ImageProxy,
        inputOffset: Pair<Float, Float>,
        radius: Int
    ) = cameraRepository.trackingXYPoint(
        inputFrame = inputFrame,
        inputOffset = inputOffset,
        radius = radius
    )
}