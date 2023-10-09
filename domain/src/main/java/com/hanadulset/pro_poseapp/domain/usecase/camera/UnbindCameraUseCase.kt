package com.hanadulset.pro_poseapp.domain.usecase.camera

import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject

class UnbindCameraUseCase @Inject constructor(private val repository: CameraRepository) {
    operator fun invoke() = repository.unbindCameraResource()
}