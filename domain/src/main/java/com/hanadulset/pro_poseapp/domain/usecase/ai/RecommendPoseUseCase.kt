package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class RecommendPoseUseCase @Inject constructor(private val repository: ImageRepository) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(backgroundBitmap: Bitmap): PoseDataResult {
        val testResult = measureTimedValue { repository.getRecommendPose(backgroundBitmap) }
        Log.d("Pose Inference Time: ", "${testResult.duration.inWholeMilliseconds}ms")
        return testResult.value
    }

}