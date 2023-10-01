package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class StopTrackingDataUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke() = cameraRepository.stopTracking()

}