package com.hanadulset.pro_poseapp.domain.usecase.camera

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class ShowFixedScreenUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
) {
    operator fun invoke(
        backgroundBitmap: Bitmap
    ) = imageRepository.getFixedScreen(backgroundBitmap)


}