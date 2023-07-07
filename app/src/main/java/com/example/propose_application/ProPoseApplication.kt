package com.example.propose_application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader

@HiltAndroidApp
class ProPoseApplication : Application() {
    init {
        val isIntialized = OpenCVLoader.initDebug() //OpenCV를 프로젝트에 적용하기 위한 시작점.
    }

    override fun onCreate() {
        super.onCreate()
    }
}