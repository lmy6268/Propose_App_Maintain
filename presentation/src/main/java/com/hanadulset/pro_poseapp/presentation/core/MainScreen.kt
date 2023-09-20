package com.hanadulset.pro_poseapp.presentation.core

import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.camera.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
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
        navHostController: NavHostController,
        cameraViewModel: CameraViewModel,
        splashViewModel: SplashViewModel
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ContainerView(
                navController = navHostController,
                cameraViewModel = cameraViewModel, splashViewModel = splashViewModel
            )
        }
    }

    //https://sonseungha.tistory.com/662
    //프레그먼트가 이동되는 뷰
    @OptIn(ExperimentalPermissionsApi::class)
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    private fun ContainerView(
        navController: NavHostController,
        cameraViewModel: CameraViewModel, splashViewModel: SplashViewModel
    ) {

        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}

        NavHost(
            navController = navController, //전환을 담당
            startDestination = page.Splash.name //시작 지점
        ) {
            prepareToUseGraph(
                routeName = "prepare",
                navHostController = navController,
                splashViewModel = splashViewModel,
                multiplePermissionsState = multiplePermissionsState
            )
            usingCameraGraph(
                routeName = "usingCamera",
                navHostController = navController,
                cameraViewModel = cameraViewModel
            )


            //스플래시 화면
            composable(route = page.Splash.name) {
                SplashScreen.Screen(splashViewModel) {
                    navController.navigate(
                        route = if (multiplePermissionsState.allPermissionsGranted.not()) page.Perm.name
                        else page.Cam.name
                    ) {
                    }
                }
            }
            composable(route = page.Cam.name) {
                Screen(cameraViewModel) {
                    navController.navigate(route = page.Setting.name) {
                        popUpTo(page.Cam.name)
                    }
                }
            }
            // 권한 설정 화면
            composable(route = page.Perm.name) {

                PermScreen.PermScreen(multiplePermissionsState) {
                    navController.navigate(page.Splash.name) {
                        popUpTo(page.Perm.name) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    //앱을 사용하기 전에 동작하는 그래프
    @OptIn(ExperimentalPermissionsApi::class)
    fun NavGraphBuilder.prepareToUseGraph(
        routeName: String,
        navHostController: NavHostController,
        splashViewModel: SplashViewModel,
        multiplePermissionsState: MultiplePermissionsState
    ) {
        navigation(startDestination = page.Splash.name, route = routeName) {
            composable(route = page.Splash.name) {
                //여기서부터는 Composable 영역

                //Splash
                // 1. 허가 여부 확인
                // 2. 허가된 경우 진행 절차
                SplashScreen.Screen(splashViewModel = splashViewModel,
                    onAfterLoadAllData = {

                    },
                    onNeedToCheckPermission = {
                        navHostController.navigate(page.Perm.name) {

                        }
                    }
                )
            }
            composable(route = page.Perm.name) {
                //여기서부터는 Composable 영역

            }
        }


    }

    //카메라를 사용할 때 사용되는 그래프
    private fun NavGraphBuilder.usingCameraGraph(
        routeName: String,
        navHostController: NavHostController,
        cameraViewModel: CameraViewModel
    ) {
        navigation(startDestination = page.Cam.name, route = routeName) {
            //카메라 화면
            composable(route = page.Cam.name) {
                Screen(cameraViewModel) {
                    navHostController.navigate(route = page.Setting.name) {
                        popUpTo(page.Cam.name)
                    }
                }
            }

            //설정 화면
            composable(route = page.Setting.name) {
                SettingScreen.Screen()
            }
        }
    }
}

//@Preview
//@Composable
//fun Test() {
//    MainScreen.MainScreen()
//}