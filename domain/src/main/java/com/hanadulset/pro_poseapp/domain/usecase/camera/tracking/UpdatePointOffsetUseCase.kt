package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import android.media.Image
import android.util.Size
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject

class UpdatePointOffsetUseCase @Inject constructor(
//    private val cameraRepository: CameraRepository,
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        image: Image, targetOffset: SizeF, rotation: Int
    ) = imageRepository.updateOffsetPoint(image, targetOffset, rotation)


}