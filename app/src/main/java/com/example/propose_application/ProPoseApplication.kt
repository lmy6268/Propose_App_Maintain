package com.example.propose_application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProPoseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}