package com.hanadulset.pro_poseapp.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaActionSound
import android.media.MediaPlayer
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore.Video.Media
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
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import java.io.File
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
        MediaActionSound()
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
                res
            }
//    data ->
//    return if (isFixedRequest) {
//        (data as ImageProxy).use {
//            imageProcessDataSourceImpl.imageToBitmap(
//                it.image!!,
//                it.imageInfo.rotationDegrees
//            )
//        }
//    }
//    else {
//        (data as ImageProxy).use {
//            fileHandleDataSourceImpl.saveImageToGallery(
//                imageProcessDataSourceImpl.imageToBitmap(
//                    it.image!!,
//                    it.imageInfo.rotationDegrees
//                )
//            )
//        }
//    }
//}


    override fun setZoomRatio(zoomLevel: Float) =
        cameraDataSource.setZoomLevel(zoomLevel)

    override fun sendCameraSound() {

        shutterSoundManager.play(MediaActionSound.SHUTTER_CLICK)
    }

    override fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        cameraDataSource.setFocus(meteringPoint, durationMilliSeconds)
    }

}
