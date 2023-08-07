package com.example.proposeapplication.domain.usecase.camera

import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class GetLatestImageUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke() = cameraRepository.getLatestImage()
}