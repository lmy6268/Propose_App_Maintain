package com.hanadulset.pro_poseapp.presentation.core

import android.Manifest
import android.app.Activity
import android.os.Build
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hanadulset.pro_poseapp.presentation.core.permission.PermScreen
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.camera.Screen
import com.hanadulset.pro_poseapp.presentation.feature.download.ModelDownloadScreen
import com.hanadulset.pro_poseapp.presentation.feature.setting.SettingScreen
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceViewModel
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.delay

object MainScreen {
    enum class Page {
        ModelDownloadProgress,//모델 다운로드 화면
        ModelDownloadRequest, //모델 다운로드 요청 화면
        Perm, //권한 화면
        Cam, //카메라 화면
        Setting,//설정화면
        Splash, //스플래시 화면
        CloseAsk, //종료 요청
        AppLoading //앱 로딩화면
    }

    enum class Graph {
        NotPermissionAllowed,
        PermissionAllowed,
        UsingCamera,
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
        prepareServiceViewModel: PrepareServiceViewModel
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ContainerView(
                navController = navHostController,
                cameraViewModel = cameraViewModel,
                prepareServiceViewModel = prepareServiceViewModel
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
        cameraViewModel: CameraViewModel,
        prepareServiceViewModel: PrepareServiceViewModel
    ) {

        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}

        val activity = LocalContext.current as Activity
        val lifecycleOwner = LocalLifecycleOwner.current
        val isPermissionAllowed = multiplePermissionsState.allPermissionsGranted
        val context = LocalContext.current
        val previewView = remember {
            val preview = PreviewView(context)
            preview.scaleType = PreviewView.ScaleType.FILL_CENTER
            preview
        }
        val previewState = cameraViewModel.previewState.collectAsState()
        val cameraInit = {
            cameraViewModel.showPreview(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.surfaceProvider,
                aspectRatio = AspectRatio.RATIO_4_3,
                previewRotation = previewView.rotation.toInt()
            )
        }



        NavHost(
            navController = navController, //전환을 담당
            //시작 지점
            startDestination = if (isPermissionAllowed) Graph.PermissionAllowed.name
            else Graph.NotPermissionAllowed.name
        ) {
            notPermissionAllowGraph(
                Graph.NotPermissionAllowed.name,
                navController,
                prepareServiceViewModel,
                multiplePermissionsState,
                cameraInit = cameraInit,
                previewState = previewState
            )
            permissionAllowedGraph(
                routeName = Graph.PermissionAllowed.name,
                prepareServiceViewModel = prepareServiceViewModel,
                navHostController = navController,
                cameraInit = cameraInit,
                previewState = previewState
            )
            usingCameraGraph(
                routeName = Graph.UsingCamera.name,
                navHostController = navController,
                cameraViewModel = cameraViewModel,
                previewView = previewView,
                activeActivity = activity,
                cameraInit = cameraInit,
            )
        }
    }

    //권한이 허가되지 않은 경우
    @OptIn(ExperimentalPermissionsApi::class)
    private fun NavGraphBuilder.notPermissionAllowGraph(
        routeName: String,
        navHostController: NavHostController,
        prepareServiceViewModel: PrepareServiceViewModel,
        multiplePermissionsState: MultiplePermissionsState,
        cameraInit: () -> Unit,
        previewState: State<CameraState>
    ) {
        navigation(startDestination = Page.Splash.name, route = routeName) {
            runSplashScreen(
                navHostController,
                Page.Perm.name
            )
            composable(route = Page.Perm.name) {
                //여기서부터는 Composable 영역
                PermScreen.PermScreen(
                    multiplePermissionsState = multiplePermissionsState,
                    permissionAllowed = {
                        navHostController.navigate(route = Graph.UsingCamera.name)
                    })
            }
            runAppLoadingScreen(
                navHostController = navHostController,
                prepareServiceViewModel = prepareServiceViewModel,
                nextPage = Graph.UsingCamera.name,
                cameraInit = cameraInit,
                previewState = previewState
            )
        }
    }


    private fun NavGraphBuilder.permissionAllowedGraph(
        routeName: String,
        navHostController: NavHostController,
        prepareServiceViewModel: PrepareServiceViewModel,
        previewState: State<CameraState>,
        cameraInit: () -> Unit
    ) {
        navigation(startDestination = Page.Splash.name, route = routeName) {
            runSplashScreen(
                navHostController,
                Page.AppLoading.name
            )
            runAppLoadingScreen(
                navHostController = navHostController,
                prepareServiceViewModel = prepareServiceViewModel,
                nextPage = Graph.UsingCamera.name,
                previewState = previewState,
                cameraInit = cameraInit
            )
        }


    }

    //카메라를 사용할 때 사용되는 그래프
    private fun NavGraphBuilder.usingCameraGraph(
        routeName: String,
        navHostController: NavHostController,
        previewView: PreviewView,
        cameraViewModel: CameraViewModel,
        activeActivity: Activity,
        cameraInit: () -> Unit
    ) {
        navigation(startDestination = Page.Cam.name, route = routeName) {
            //카메라 화면
            composable(
                route = Page.Cam.name
            ) {
                Screen(
                    cameraViewModel,
                    showBackContinueDialog = {
                        activeActivity.finish() //앱 종료
//                        navHostController.navigate(route = page.CloseAsk.name) //종료 여부 파악 화면으로 이동
                    }, previewView = previewView,
                    onClickSettingBtnEvent = {
                        navHostController.navigate(route = Page.Setting.name) {
                        }
                    },
                    cameraInit = cameraInit
                )
            }

            //앱 종료 여부 파악 화면
            composable(route = Page.CloseAsk.name) {

            }
            //설정 화면
            composable(route = Page.Setting.name) {
                SettingScreen.Screen()
            }


        }
    }

    private fun NavGraphBuilder.runSplashScreen(
        navHostController: NavHostController,
        nextPage: String
    ) {
        val splashPage = Page.Splash.name
        composable(route = splashPage) {
            //여기서부터는 Composable 영역
            PrepareServiceScreens.SplashScreen()
            LaunchedEffect(Unit) {
                //1초 뒤에 앱 로딩 화면으로 넘어감.
                delay(500)
                navHostController.navigate(route = nextPage) {
                    //백스택에서 스플래시 화면을 제거한다.
                    popUpTo(splashPage) { inclusive = true }
                }
            }
        }
    }

    private fun NavGraphBuilder.runAppLoadingScreen(
        navHostController: NavHostController,
        prepareServiceViewModel: PrepareServiceViewModel,
        nextPage: String,
        cameraInit: () -> Unit,
        previewState: State<CameraState>
    ) {
        val appLoadingPage = Page.AppLoading.name
        composable(route = appLoadingPage) {
            //앱로딩이 끝나면, 카메라화면을 보여주도록 한다.
            PrepareServiceScreens.AppLoadingScreen(
                previewState = previewState,
                cameraInit = cameraInit,
                prepareServiceViewModel = prepareServiceViewModel,
                onAfterLoadedEvent = {
                    navHostController.navigate(nextPage)
                    {
                        //앱 로딩 페이지는 뒤로가기 해도 보여주지 않음 .
                        popUpTo(route = appLoadingPage) { inclusive = true }

                    }

                })
        }
    }

    private fun NavGraphBuilder.checkResourceDownloadGraph(
        navHostController: NavHostController,
        prepareServiceViewModel: PrepareServiceViewModel,
        routeName: String = "modelDownload",
        permissionAllowed: Boolean
    ) {
        val modelDownloadRequestPage = Page.ModelDownloadRequest.name
        val modelDownloadProgressPage = Page.ModelDownloadProgress.name

        navigation(startDestination = modelDownloadRequestPage, route = routeName) {
            runSplashScreen(
                navHostController,
                modelDownloadRequestPage
            )
            composable(modelDownloadRequestPage) {
                ModelDownloadScreen.ModelDownloadRequestScreen(
                    prepareServiceViewModel = prepareServiceViewModel,
                    moveToPerm = {
                        navHostController.navigate(
                            if (permissionAllowed) Graph.PermissionAllowed.name
                            else Graph.NotPermissionAllowed.name
                        )
                    },
                    moveToDownloadProgress = {
                        navHostController.navigate(
                            modelDownloadProgressPage
                        ) { popUpTo(route = modelDownloadRequestPage) { inclusive = true } }
                    })
            }
            composable(modelDownloadProgressPage) {
                val downloadProgress by prepareServiceViewModel.downloadState.collectAsState()
                if (downloadProgress.state in listOf(
                        DownloadInfo.ON_UPDATE,
                        DownloadInfo.ON_DOWNLOAD
                    )
                ) ModelDownloadScreen.ModelDownloadProgressScreen(downloadProcess = downloadProgress) {
                    //앱 종료
                }

            }
        }


    }


}
