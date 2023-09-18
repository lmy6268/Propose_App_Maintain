package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.media.Image
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendCompInfoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(image: Image, rotation: Int) =
        repository.getRecommendCompInfo(image, rotation)
}