package com.hanadulset.pro_poseapp.domain.usecase.camera

import androidx.camera.core.MeteringPoint
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class SetFocusUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraRepository.setFocus(meteringPoint, durationMilliSeconds)
    }
}