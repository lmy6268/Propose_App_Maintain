package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import android.util.Log
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(backgroundBitmap: Bitmap): PoseDataResult = repository.getRecommendPose(backgroundBitmap)
    


}