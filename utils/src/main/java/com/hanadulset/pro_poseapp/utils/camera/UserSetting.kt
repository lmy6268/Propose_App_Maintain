package com.hanadulset.pro_poseapp.utils.camera

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
class UserSetting private constructor() {
    private var soundOn = false
    fun activateSound() {
        soundOn = true
    }

    companion object {
        private lateinit var context: Context
        private var instance: UserSetting? = null
        fun getInstance(_context: Context): UserSetting {
            return instance ?: synchronized(this) {
                instance ?: UserSetting().also {
                    context = _context
                    instance = it
                }
            }
        }

    }
}