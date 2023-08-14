package com.example.proposeapplication.presentation

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proposeapplication.presentation.ui.MainTheme
import com.example.proposeapplication.presentation.view.PermScreen
import com.example.proposeapplication.presentation.view.SettingScreen
import com.example.proposeapplication.presentation.view.camera.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setFullScreen(this)

        setContent {
            MainTheme {
                MainScreen()
            }
        }

    }

    @Composable
    @Preview
    private fun MainScreen() {
        //화면에 가득차게 컨텐츠를 채운다.
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ContainerView()
        }
    }


    //https://sonseungha.tistory.com/662
    //프레그먼트가 이동되는 뷰
    @OptIn(ExperimentalPermissionsApi::class)
    @Preview
    @Composable
    private fun ContainerView() {
        val navController = rememberNavController()

        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
        NavHost(
            navController = navController,
            startDestination = if (multiplePermissionsState.allPermissionsGranted.not()) page.Perm.name
            else page.Cam.name
        ) {
            composable(route = page.Cam.name) {
                Screen(navController, mainViewModel)
            }
            composable(route = page.Perm.name) {
                PermScreen.PermScreen(navController, multiplePermissionsState)
            }
            composable(route = page.Setting.name) {
                SettingScreen.Screen()
            }
        }
    }

    enum class page {
        Splash, //로딩화면
        Perm, //권한 화면
        Cam, //카메라 화면
        Setting,//설정화면
    }


    companion object {

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

        //필요한 권한
        val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) else arrayOf(
            Manifest.permission.CAMERA
        )
    }

}