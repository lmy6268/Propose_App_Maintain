package com.hanadulset.pro_poseapp.presentation

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.hanadulset.pro_poseapp.presentation.ui_components.MainTheme
import com.hanadulset.pro_poseapp.presentation.core.MainScreen
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val cameraViewModel: CameraViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    //전체화면 적용
    private fun setFullScreen(context: Context) {
        (context as AppCompatActivity).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                supportActionBar?.hide()
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars())
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            //R버전 이하
            else {
                supportActionBar?.hide()
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        // 컨텐츠를 시스템바 밑에 보이도록한다.
                        // 시스템바가 숨겨지거나 보여질 때 컨텐츠 부분이 리사이징 되는 것을 막기 위함
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // 상태바를 사라지게하기
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setFullScreen(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED //회전 고정


        setContent {
            MainTheme {
                MainScreen.MainScreen(cameraViewModel, splashViewModel = splashViewModel)
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로 버튼 이벤트 처리
            finish()
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}