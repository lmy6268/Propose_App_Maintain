package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import android.graphics.Bitmap
import android.media.Image
import android.util.Size
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class UpdatePointOffsetUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        targetOffset: SizeF, backgroundBitmap: Bitmap
    ) = imageRepository.updateOffsetPoint(backgroundBitmap, targetOffset)


}