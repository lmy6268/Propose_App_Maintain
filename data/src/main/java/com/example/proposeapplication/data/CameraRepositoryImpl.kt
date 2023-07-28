package com.example.proposeapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Display
import android.view.Surface
import com.example.proposeapplication.domain.repository.CameraRepository
import com.example.proposeapplication.utils.camera.CameraController

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {
    //변수 초기화
    private val controller by lazy {
        CameraController(applicationContext)
    }

    override fun getPreviewSize(context: Context,display: Display) = controller.getPreviewSize(context,display)


    override suspend fun initPreview(surface: Surface) {
        controller.setPreview(surface)
    }

    override suspend fun takePhoto(orientationData: Int): Bitmap = controller.takePhoto(orientationData)
}
