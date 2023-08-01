package com.example.proposeapplication.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.SurfaceView

interface CameraRepository {
    fun getPreviewSize(context: Context, display: Display): Size
    suspend fun initPreview(surface: Surface)
    suspend fun takePhoto(orientationData: Int): Bitmap
    suspend fun getFixedScreen(surfaceView: SurfaceView): Bitmap?
}