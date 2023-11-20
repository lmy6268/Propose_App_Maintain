package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import android.util.Log
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(backgroundBitmap: Bitmap): PoseDataResult =
        measureTimedValue { repository.getRecommendPose(backgroundBitmap) }.apply {
            Log.d("Elapsed Time for Recommending Pose: ", duration.toString())
        }.value


}