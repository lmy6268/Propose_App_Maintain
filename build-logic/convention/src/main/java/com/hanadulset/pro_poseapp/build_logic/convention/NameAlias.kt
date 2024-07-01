package com.hanadulset.pro_poseapp.build_logic.convention


//Matching with libs.versions.toml and conventionPlugin call id.
internal object NameAlias {


    object Plugins {
        const val ANDROID_APP = "android.application"
        const val ANDROID_LIB = "android.library"
        const val KOTLIN_ANDROID = "jetbrains.kotlin.android"
        const val HILT = "dagger.hilt"
        const val KSP = "google.devtool.ksp"
        const val ROOM = "androidx.room"
    }

    object Library {

        object HILT {
            const val ANDROID = "hilt.android"
            const val COMPILER = "hilt.compiler"
        }

        object ROOM {
            const val RUNTIME = "room.runtime"
            const val KTX = "room.ktx"
            const val COMPILER = "room.compiler"
        }

        object COMPOSE {
            const val HILT_NAVIGATION_COMPOSE = "androidx.hilt.navigation.compose"
            const val RUNTIME_COMPOSE = "androidx.lifecycle.runtimeCompose"
            const val VIEWMODEL_COMPOSE = "androidx.lifecycle.viewModelCompose"
            const val BOM = "androidx.compose.composeBom"
        }
    }


}