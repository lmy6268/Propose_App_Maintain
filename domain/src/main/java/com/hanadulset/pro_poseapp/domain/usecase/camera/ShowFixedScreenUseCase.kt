package com.hanadulset.pro_poseapp.domain.usecase.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class ShowFixedScreenUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
//    private val cameraRepository: CameraRepository
) {
    operator fun invoke(
        imageProxy: ImageProxy
    ) =
        imageRepository.getFixedScreen(
//            cameraRepository.takePhoto(isFixedRequest = true) as Bitmap
            imageProxy
        )


}