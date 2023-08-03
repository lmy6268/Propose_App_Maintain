package com.example.proposeapplication.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowCompat
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proposeapplication.presentation.view.CameraScreen
import com.example.proposeapplication.presentation.view.Screen
import com.example.proposeapplication.utils.PermissionDialog
import com.example.proposeapplication.utils.camera.AutoFitSurfaceView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
//    private lateinit var activityMainBinding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
//        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(activityMainBinding.root)
        setFullScreen(this)
        setContent {
            MaterialTheme {
//                setFullScreen(this)
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
//        val systemUiController = rememberSystemUiController()
//        LaunchedEffect(Unit) {
//            systemUiController.apply {
//                isStatusBarVisible = false
//            }
//        }
        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
        NavHost(
            navController = navController,
            startDestination = if (multiplePermissionsState.allPermissionsGranted.not()) page.Perm.name
            else page.Cam.name
        ) {
            composable(route = page.Cam.name) {
                Screen(mainViewModel = mainViewModel, context = this@MainActivity)
            }
            composable(route = page.Perm.name) {
                PermScreen(navController, multiplePermissionsState)
            }
        }
    }

    enum class page {
        Splash, //로딩화면
        Perm, //권한 화면
        Cam //카메라 화면
    }


//    @Preview
//    @Composable
//    fun CamScreen() {
//        Surface(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(Modifier.fillMaxSize()) {
//                //미리보기 화면
//                AndroidView(
//                    factory = {
//                        AutoFitSurfaceView(it)
//                    },
//                    modifier = Modifier.fillMaxSize(),
//                )
//                Row(
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .fillMaxWidth()
//                        .heightIn(50.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                ) {
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                }
//
//                //하단 부분
//                Row(
//                    modifier = Modifier.align(Alignment.BottomStart)
//                        .fillMaxWidth()
//                        .heightIn(50.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                    Button(onClick = {}) {
//                        Text(text = "화면비율")
//                    }
//                }
//
//
//            }
//        }
//    }


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    //권한 설정화면
    fun PermScreen(
        navController: NavHostController,
        multiplePermissionsState: MultiplePermissionsState
    ) {
        val needToCheck = remember { mutableStateOf(false) }
        //https://hanyeop.tistory.com/452 참고함.
        LaunchedEffect(Unit) {
            if (needToCheck.value) {
                if (multiplePermissionsState.allPermissionsGranted) navController.navigate(page.Cam.name) {
                    popUpTo(page.Perm.name) { inclusive = true }
                }
                else needToCheck.value = false
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "애플리케이션을 이용하기 위해서는  권한을 허용하셔야 합니다.",
                    modifier = Modifier.fillMaxWidth(0.5F)
                )
                Button(onClick = {
                    if (multiplePermissionsState.allPermissionsGranted.not()) {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                        needToCheck.value = true
                    }
                }) {
                    Text(text = "권한 설정하기")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

//        activityMainBinding.fragmentContainer.postDelayed(
//            {
//                setFullScreen(this)
//            }, 50L
//        )
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

        val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) else arrayOf(
            Manifest.permission.CAMERA
        )
    }

}