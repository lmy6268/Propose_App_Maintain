package com.example.proposeapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProPoseApplication :Application() {
    override fun onCreate() {
        super.onCreate()
    }
}