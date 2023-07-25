package com.example.proposeapplication.domain.repository

import android.graphics.Bitmap
import android.util.Size
import android.view.Display
import android.view.Surface

interface CameraRepository {
    fun getPreviewSize(display: Display): Size
    suspend fun initPreview(surface: Surface)
    fun saveResult()
    fun takePhoto(): Bitmap
}