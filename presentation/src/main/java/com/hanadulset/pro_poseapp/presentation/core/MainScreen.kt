package com.hanadulset.pro_poseapp.presentation.core

import android.Manifest
import android.os.Build
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.camera.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hanadulset.pro_poseapp.presentation.core.permission.PermScreen
import com.hanadulset.pro_poseapp.presentation.feature.setting.SettingScreen
import com.hanadulset.pro_poseapp.presentation.feature.splash.SplashScreen
import com.hanadulset.pro_poseapp.presentation.feature.splash.SplashViewModel

object MainScreen {
    enum class page {
        ModelDownloadProgress,//모델 다운로드 화면
        ModelDownloadRequest, //모델 다운로드 요청 화면
        Perm, //권한 화면
        Cam, //카메라 화면
        Setting,//설정화면
        Splash, //스플래시 화면
    }

    private val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) else arrayOf(
        Manifest.permission.CAMERA
    )


    @Composable
    fun MainScreen(
        cameraViewModel: CameraViewModel = hiltViewModel(),
        splashViewModel: SplashViewModel = hiltViewModel()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ContainerView(cameraViewModel = cameraViewModel, splashViewModel = splashViewModel)
        }
    }

    //https://sonseungha.tistory.com/662
    //프레그먼트가 이동되는 뷰
    @OptIn(ExperimentalPermissionsApi::class)
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    private fun ContainerView(cameraViewModel: CameraViewModel, splashViewModel: SplashViewModel) {
        val navController = rememberNavController()
        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val previewView = PreviewView(context)

        NavHost(
            navController = navController,
            startDestination = page.Splash.name
//            if (multiplePermissionsState.allPermissionsGranted.not()) page.Perm.name else page.Cam.name
        ) {
            //스플래시 화면
            composable(route = page.Splash.name) {
                SplashScreen.Screen(splashViewModel) {
                    previewView.apply {
                        cameraViewModel.showPreview(
                            lifecycleOwner = lifecycleOwner,
                            surfaceProvider = this.surfaceProvider,
                            aspectRatio = cameraViewModel.viewRateState.value,
                            previewRotation = this.rotation.toInt()
                        )
                    }
                    navController.navigate(route = page.Cam.name)
                    {
                        popUpTo(page.Splash.name) { inclusive = true }
                    }
                }
            }
            composable(route = page.Cam.name) {
                Screen(previewView, navController, cameraViewModel) {
                    navController.navigate(route = page.Setting.name) {
//                        popUpTo(page.Cam.name)
                    }
                }

            }
            composable(route = page.Perm.name) {
                PermScreen.PermScreen(multiplePermissionsState) {
                    navController.navigate(page.Cam.name) {
                        popUpTo(page.Perm.name) { inclusive = true }
                    }
                }
            }
            composable(route = page.Setting.name) {
                SettingScreen.Screen()
            }
            composable(route = page.ModelDownloadRequest.name) {

            }
            composable(route = page.ModelDownloadProgress.name) {

            }
        }
    }


}

@Preview
@Composable
fun Test() {
    MainScreen.MainScreen()
}