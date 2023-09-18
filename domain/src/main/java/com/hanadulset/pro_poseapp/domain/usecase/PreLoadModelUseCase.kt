package com.hanadulset.pro_poseapp.domain.usecase

import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class PreLoadModelUseCase @Inject constructor(private val imageRepository: ImageRepository) {
    suspend operator fun invoke() = imageRepository.preRunModel()

}