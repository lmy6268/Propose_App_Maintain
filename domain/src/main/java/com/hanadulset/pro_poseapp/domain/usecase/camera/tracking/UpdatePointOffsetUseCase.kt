package com.hanadulset.pro_poseapp.domain.usecase.camera.tracking

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class UpdatePointOffsetUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        targetOffset: SizeF, backgroundBitmap: Bitmap
    ): SizeF? {
        val duration =
            measureTimedValue { imageRepository.updateOffsetPoint(backgroundBitmap, targetOffset) }
        return duration.value
    }


}