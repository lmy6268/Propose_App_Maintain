package com.example.proposeapplication.domain.usecase.camera

import android.graphics.Bitmap
import com.example.proposeapplication.domain.repository.CameraRepository
import javax.inject.Inject

class GetCompInfoUseCase @Inject constructor(private val repository: CameraRepository) {
    suspend operator fun invoke(bitmap: Bitmap) = repository.compositionData(bitmap)
}