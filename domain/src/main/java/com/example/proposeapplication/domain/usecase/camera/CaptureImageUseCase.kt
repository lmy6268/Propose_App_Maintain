package com.example.proposeapplication.domain.usecase.camera

import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class CaptureImageUseCase @Inject constructor(
    private val repository: CameraRepository,
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke() =
        repository.takePhoto()
            .apply { //원본을 전달 받고
            imageRepository.saveImageToGallery(this) // 원본 저장하면, 썸네일 뽑아주기
        }

}