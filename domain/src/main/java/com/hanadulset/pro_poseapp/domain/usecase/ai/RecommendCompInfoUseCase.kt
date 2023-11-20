package com.hanadulset.pro_poseapp.domain.usecase.ai

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendCompInfoUseCase @Inject constructor(private val repository: ImageRepository) {

    suspend operator fun invoke(backgroundBitmap: Bitmap): Pair<Float, Float> =
        repository.getRecommendCompInfo(backgroundBitmap)


}
