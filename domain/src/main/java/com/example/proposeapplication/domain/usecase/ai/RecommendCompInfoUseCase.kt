package com.example.proposeapplication.domain.usecase.ai

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class RecommendCompInfoUseCase @Inject constructor(private val repository: ImageRepository) {
    suspend operator fun invoke(image: Image, rotation: Int) =
        repository.getRecommendCompInfo(image, rotation)
}
