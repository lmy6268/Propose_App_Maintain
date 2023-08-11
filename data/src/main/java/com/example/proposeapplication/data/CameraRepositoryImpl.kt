package com.example.proposeapplication.data

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.utils.CameraControllerImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {

    private val controllerImpl by lazy {
        CameraControllerImpl(applicationContext)
    }


    override fun initPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer
    ) {
//        controller.setPreview(surface)
        controllerImpl.showPreview(
            lifecycleOwner, surfaceProvider, ratio, analyzer
        )
    }

    override suspend fun takePhoto(): Bitmap =
        controllerImpl.takePhoto()

    override suspend fun getFixedScreen(rawBitmap: Bitmap): Bitmap =
        controllerImpl.getFixedScreen(rawBitmap)

    override fun getLatestImage(): Bitmap? = controllerImpl.getLatestImage()


}
