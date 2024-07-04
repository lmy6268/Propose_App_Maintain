package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSizeF
import javax.inject.Inject
import kotlin.time.measureTimedValue

class UpdatePointOffsetUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        targetOffset: ProPoseSizeF, backgroundBitmap: Bitmap
    ): ProPoseSizeF? {
        val duration =
            measureTimedValue { imageRepository.updateOffsetPoint(backgroundBitmap, targetOffset) }
        return duration.value
    }


}