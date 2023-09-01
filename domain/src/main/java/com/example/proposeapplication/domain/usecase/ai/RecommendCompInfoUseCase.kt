package com.example.proposeapplication.domain.usecase.ai

import android.graphics.Bitmap
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendCompInfoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(bitmap: Bitmap) = repository.getRecommendCompInfo(bitmap)
}
