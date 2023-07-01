package com.example.propose_application

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.propose_application.databinding.ActivityMainBinding
import com.example.propose_application.ui.theme.ProPose_ApplicationTheme

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
    }

    override fun onResume() {
        super.onResume()
        activityMainBinding.fragmentContainer.postDelayed(
            {
                setFullScreen(this)
            }, 500L
        )
    }

    companion object {
        //전체화면 적용
        private fun setFullScreen(context: Context) {
            (context as AppCompatActivity).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    actionBar?.hide()
                    window.setDecorFitsSystemWindows(false)
                    window.insetsController?.apply {
                        hide(WindowInsets.Type.statusBars())
                        systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
                //R버전 이하
                else {
                    actionBar?.hide()
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
    }
}
