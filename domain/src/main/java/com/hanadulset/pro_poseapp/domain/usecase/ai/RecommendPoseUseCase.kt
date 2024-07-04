package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.model.pose.RecommendPoseResult
import javax.inject.Inject

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(backgroundBitmap: Bitmap): RecommendPoseResult = repository.getRecommendPose(backgroundBitmap)
    


}