package com.example.proposeapplication.domain.usecase.camera

import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class GetLatestImageUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    operator fun invoke() = imageRepository.getLatestImage()
}