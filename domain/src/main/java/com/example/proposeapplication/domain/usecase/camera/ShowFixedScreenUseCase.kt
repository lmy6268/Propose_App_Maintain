package com.example.proposeapplication.domain.usecase.camera

import android.graphics.Bitmap
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import javax.inject.Inject

class ShowFixedScreenUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val cameraRepository: CameraRepository
) {
    suspend operator fun invoke() =
        imageRepository.getFixedScreen(
            cameraRepository.takePhoto(isFixedRequest = true) as Bitmap
        )


}