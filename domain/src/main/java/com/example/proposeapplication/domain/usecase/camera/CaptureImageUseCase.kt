package com.example.proposeapplication.domain.usecase.camera

import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class CaptureImageUseCase @Inject constructor(private val repository: CameraRepository) {
    suspend operator fun invoke(orientationData: Int) =
        repository.takePhoto(orientationData)

}