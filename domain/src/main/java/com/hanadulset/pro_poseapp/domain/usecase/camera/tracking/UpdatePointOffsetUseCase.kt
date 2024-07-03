package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import android.graphics.Bitmap
import android.util.SizeF
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject
import kotlin.time.measureTimedValue

class UpdatePointOffsetUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        targetOffset: SizeF, backgroundBitmap: Bitmap
    ): SizeF? {
        val duration =
            measureTimedValue { imageRepository.updateOffsetPoint(backgroundBitmap, targetOffset) }
        return duration.value
    }


}