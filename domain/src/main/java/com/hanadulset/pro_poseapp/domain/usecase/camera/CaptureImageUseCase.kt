package com.hanadulset.pro_poseapp.domain.usecase.camera

import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CaptureImageUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    suspend operator fun invoke(captureEventData: CaptureEventData) =
        suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                cont.resume(
                    cameraRepository.takePhoto(
                        captureEventData
                    )
                )
            }

        }

}