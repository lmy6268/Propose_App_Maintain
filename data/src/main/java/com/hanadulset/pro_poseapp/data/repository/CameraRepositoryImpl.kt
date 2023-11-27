package com.hanadulset.pro_poseapp.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaActionSound
import android.provider.Settings
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.hanadulset.pro_poseapp.data.datasource.CameraDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import com.hanadulset.pro_poseapp.utils.ImageUtils
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventLog
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {

    private val cameraDataSource by lazy {
        CameraDataSourceImpl(applicationContext)
    }
    private val shutterSoundManager by lazy {
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val fileHandleDataSourceImpl by lazy {
        FileHandleDataSourceImpl(context = applicationContext)
    }


    override suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int,
        analyzer: ImageAnalysis.Analyzer,
    ) = cameraDataSource.initCamera(
        lifecycleOwner, surfaceProvider, aspectRatio, previewRotation, analyzer
    )

    @SuppressLint("HardwareIds")
    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun takePhoto(eventLog: CaptureEventLog) =
        cameraDataSource.takePhoto()
            .let { data ->
                val capturedImageBitmap = data.use {
                    ImageUtils.imageToBitmap(
                        it.image!!,
                        it.imageInfo.rotationDegrees
                    )
                }
                val res = fileHandleDataSourceImpl.saveImageToGallery(capturedImageBitmap)
                val poseEstimationResult = estimatePose(capturedImageBitmap).toString()
                val deviceID = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
                Firebase.analytics.apply {
                    setUserId(deviceID)
                }.logEvent(eventLog.eventId) {
                    val hog = eventLog.backgroundHog.toString()
                    val prev = eventLog.prevRecommendPoses.toString()
                    param(
                        "deviceID", deviceID
                    )
                    param(
                        "poseID", eventLog.poseID.toDouble()
                    )
                    param(
                        "timeStamp", eventLog.timestamp
                    )
                    param(
                        "backgroundId", eventLog.backgroundId?.toDouble() ?: -1.0
                    )
                    if (prev.length > 99) {
                        prev.chunked(99).forEachIndexed { index, s ->
                            param(
                                "prevRecommendPoses_$index", s
                            )
                        }
                    } else {
                        param(
                            "prevRecommendPoses", prev
                        )
                    }
                    if (hog.length > 99) {
                        hog.chunked(99).forEachIndexed { index, s ->
                            param(
                                "backgroundHog_$index", s
                            )
                        }
                    } else {
                        param(
                            "backgroundHog", hog
                        )
                    }
                    if (poseEstimationResult.length > 99) {
                        poseEstimationResult.chunked(99).forEachIndexed { index, s ->
                            param(
                                "human_pose_estimation_$index", s
                            )
                        }
                    } else {
                        param(
                            "human_pose_estimation", poseEstimationResult
                        )
                    }


                }
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

    override fun sendUserFeedBackData(eventLogs: ArrayList<CaptureEventLog>) {

    }

    override fun unbindCameraResource() =
        cameraDataSource.unbindCameraResources()

    private suspend fun estimatePose(bitmap: Bitmap): List<Triple<Float, Float, Float>?> {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        val poseDetector = PoseDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        val processTask = poseDetector.process(image)
        val resultPoseData = processTask.await()
        val returnList = MutableList<Triple<Float, Float, Float>?>(33) { null }
        resultPoseData.allPoseLandmarks.forEach {
            returnList[it.landmarkType] = it.position3D.run { Triple(x, y, z) }
        }
        return returnList.toList()
    }
}
