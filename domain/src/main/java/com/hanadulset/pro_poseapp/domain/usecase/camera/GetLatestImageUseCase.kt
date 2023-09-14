package com.hanadulset.pro_poseapp.domain.usecase.camera

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class GetLatestImageUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    operator fun invoke() = imageRepository.getLatestImage()
}