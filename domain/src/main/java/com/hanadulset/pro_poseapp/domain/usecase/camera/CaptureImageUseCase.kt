package com.hanadulset.pro_poseapp.domain.usecase.camera

import android.net.Uri
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CaptureImageUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    suspend operator fun invoke() =
        suspendCoroutine { cont ->
//            cameraRepository.sendCameraSound()
            CoroutineScope(Dispatchers.IO).launch {
                cont.resume(cameraRepository.takePhoto())
            }

        }

}