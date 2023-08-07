package com.example.proposeapplication.utils.camera

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraDevice
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.SurfaceView


interface CameraControllerInterface {
    suspend fun takePhoto(orientation: Int): Bitmap
    suspend fun provideFixedScreen(viewFinder: SurfaceView): Bitmap?
    suspend fun getCapturedImage(orientationData: Int): CameraController.Companion.CombinedCaptureResult
    suspend fun setPreview(surface: Surface)
    fun getPreviewSize(actContext: Context, display: Display): Size
    suspend fun openCamera(cameraId: String): CameraDevice
    fun getLatestImage(): Bitmap?

}