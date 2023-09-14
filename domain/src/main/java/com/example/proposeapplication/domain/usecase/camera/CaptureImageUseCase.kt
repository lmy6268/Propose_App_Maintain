package com.example.proposeapplication.domain.usecase.camera

import android.graphics.Bitmap
import android.net.Uri
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CaptureImageUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke() =
        suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
//                val uri =
//                val resizedImage = imageRepository.saveImageToGallery(tmp) // 원본 저장하면, 썸네일 뽑아주기
//                imageRepository.testS3()
                cont.resume( cameraRepository.takePhoto(isFixedRequest = false) as Uri)
            }

        }

}