package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import android.media.AudioManager
import android.media.MediaActionSound
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.data.datasource.CameraDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.UserDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import kotlinx.serialization.builtins.serializer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {

    private val cameraDataSource by lazy {
        CameraDataSourceImpl(applicationContext)
    }
    private val imageProcessDataSourceImpl by lazy {
        ImageProcessDataSourceImpl()
    }
    private val shutterSoundManager by lazy {
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val fileHandleDataSourceImpl by lazy {
        FileHandleDataSourceImpl(context = applicationContext)
    }


    override suspend fun initCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,

        ) = cameraDataSource.initCamera(
        lifecycleOwner, surfaceProvider, aspectRatio, previewRotation, analyzer
    )

    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun takePhoto() =
        cameraDataSource.takePhoto()
            .let { data ->
                //만약 셔터 소리를 설정해놓은 경우, 셔터음을 켠다.
                val res: Uri
                val elapseTime = measureTimeMillis {
                    res = data.use {
                        fileHandleDataSourceImpl.saveImageToGallery(
                            imageProcessDataSourceImpl.convertCaptureImageToBitmap(
                                it.image!!,
                                it.imageInfo.rotationDegrees
                            )
                        )
                    }
                }
                Log.d(
                    "Time Elapse to image: ", "$elapseTime ms"
                )
                Log.d(
                    "현재 저장된 이미지 목록: ",
                    fileHandleDataSourceImpl.loadCapturedImages(false).toString()
                )
                res
            }


    override fun setZoomRatio(zoomLevel: Float) =
        cameraDataSource.setZoomLevel(zoomLevel)

    //카메라 소리
    override fun sendCameraSound() {
        shutterSoundManager.setStreamVolume(
            AudioManager.STREAM_SYSTEM,
            1,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        MediaActionSound().apply {
            play(MediaActionSound.SHUTTER_CLICK)
        }


    }

    override fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraDataSource.setFocus(meteringPoint, durationMilliSeconds)
    }

    override fun sendUserFeedBackData(eventLogs: ArrayList<EventLog>) {
        TODO("Not yet implemented")
    }

    override suspend fun trackingXYPoint(
        inputFrame: ImageProxy,
        inputOffset: Pair<Float, Float>,
        radius: Int
    ): Pair<Float, Float> = imageProcessDataSourceImpl.trackingXYPoint(
        inputFrame, inputOffset, radius
    )

    override fun stopTracking() = imageProcessDataSourceImpl.stopTracking()


}
