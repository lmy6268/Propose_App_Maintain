package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import android.media.Image
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(backgroundBitmap: Bitmap) =
        repository.getRecommendPose(backgroundBitmap)
}