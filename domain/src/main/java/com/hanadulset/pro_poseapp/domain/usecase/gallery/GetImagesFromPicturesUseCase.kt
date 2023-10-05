package com.hanadulset.pro_poseapp.domain.usecase.gallery

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class GetImagesFromPicturesUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke() = imageRepository.loadAllCapturedImages()
}