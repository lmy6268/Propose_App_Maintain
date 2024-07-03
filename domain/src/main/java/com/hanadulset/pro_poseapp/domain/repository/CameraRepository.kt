package com.hanadulset.pro_poseapp.domain.repository

import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventData
import com.hanadulset.pro_poseapp.utils.model.camera.ProPoseCameraState
import com.hanadulset.pro_poseapp.utils.model.camera.PreviewResolutionData


//카메라 기능을 담당하는 레포지토리
interface CameraRepository {
    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,
    ): ProPoseCameraState<PreviewResolutionData>

    suspend fun takePhoto(eventData: CaptureEventData): Uri
    fun setZoomRatio(zoomLevel: Float)
    fun sendCameraSound()
    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long)
    fun unbindCameraResource(): Boolean
}