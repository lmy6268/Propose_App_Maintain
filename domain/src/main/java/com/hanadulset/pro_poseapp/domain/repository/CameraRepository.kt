package com.hanadulset.pro_poseapp.domain.repository

import android.media.Image
import android.net.Uri
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog


//카메라 기능을 담당하는 레포지토리
interface CameraRepository {
    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,
    ): CameraState

    suspend fun takePhoto(): Uri
    fun setZoomRatio(zoomLevel: Float)
    fun sendCameraSound()
    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long)


    fun sendUserFeedBackData(eventLogs: ArrayList<EventLog>)
    fun unbindCameraResource(): Boolean
}