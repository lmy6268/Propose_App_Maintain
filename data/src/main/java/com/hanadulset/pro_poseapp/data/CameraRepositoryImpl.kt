package com.hanadulset.pro_poseapp.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaActionSound
import android.media.MediaPlayer
import android.provider.MediaStore.Video.Media
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.hanadulset.pro_poseapp.data.datasource.CameraDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.ImageProcessDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.CameraRepository
import javax.inject.Inject
import javax.inject.Singleton

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
    override suspend fun takePhoto(isFixedRequest: Boolean) =
        cameraDataSource.takePhoto(isFixedRequest)
            .let { data ->
                if (data is ImageProxy)
                    data.use {
                        imageProcessDataSourceImpl.imageToBitmap(
                            data.image!!,
                            data.imageInfo.rotationDegrees
                        )
                    }
                else data
            }


    override fun setZoomRatio(zoomLevel: Float) =
        cameraDataSource.setZoomLevel(zoomLevel)

    override fun sendCameraSound() {

        shutterSoundManager.play(MediaActionSound.SHUTTER_CLICK)
    }

}
