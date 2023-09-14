package com.hanadulset.pro_poseapp.presentation.view

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanadulset.pro_poseapp.presentation.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.view.camera.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

object MainScreen {
    enum class page {
        ModelDownloadProgress,//모델 다운로드 화면
        ModelDownloadRequest, //모델 다운로드 요청 화면
        Perm, //권한 화면
        Cam, //카메라 화면
        Setting,//설정화면
    }

    private val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) else arrayOf(
        Manifest.permission.CAMERA
    )


    @Composable
    fun MainScreen(cameraViewModel: CameraViewModel = hiltViewModel()) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ContainerView(cameraViewModel = cameraViewModel)
        }
    }

    //https://sonseungha.tistory.com/662
    //프레그먼트가 이동되는 뷰
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun ContainerView(cameraViewModel: CameraViewModel) {
        val navController = rememberNavController()
        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
        NavHost(
            navController = navController,
            startDestination = if (multiplePermissionsState.allPermissionsGranted.not()) page.Perm.name else page.Cam.name
        ) {
            composable(route = page.Cam.name) {
                Screen(navController, cameraViewModel)
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