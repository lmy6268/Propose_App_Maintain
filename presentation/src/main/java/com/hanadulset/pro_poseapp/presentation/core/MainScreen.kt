package com.hanadulset.pro_poseapp.presentation.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.hanadulset.pro_poseapp.domain.usecase.camera.UnbindCameraUseCase
import com.hanadulset.pro_poseapp.presentation.component.UIComponents
import com.hanadulset.pro_poseapp.presentation.core.permission.PermScreen
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.feature.camera.Screen
import com.hanadulset.pro_poseapp.presentation.feature.download.ModelDownloadScreen
import com.hanadulset.pro_poseapp.presentation.feature.gallery.GalleryScreen
import com.hanadulset.pro_poseapp.presentation.feature.gallery.GalleryViewModel
import com.hanadulset.pro_poseapp.presentation.feature.setting.SettingScreen
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceScreens
import com.hanadulset.pro_poseapp.presentation.feature.splash.PrepareServiceViewModel
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object MainScreen {
    enum class Page {
        ModelDownloadProgress,//모델 다운로드 화면
        ModelDownloadRequest, //모델 다운로드 요청 화면
        Perm, //권한 화면
        TermOfUse,//약관 동의 화면
        Cam, //카메라 화면
        Setting,//설정화면
        Splash, //스플래시 화면
        AppLoading, //앱 로딩화면
        Images,//촬영된 이미지 목록 보여주기
    }

    enum class Graph {
        NotPermissionAllowed,
        PermissionAllowed,
        UsingCamera,
        DownloadProcess
    }


    //요청 받을 권한들
    private val PERMISSIONS_REQUIRED =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) else arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )


    @Composable
    fun MainScreen(
        modifier: Modifier = Modifier,
        navHostController: NavHostController,
        cameraViewModel: CameraViewModel,
        prepareServiceViewModel: PrepareServiceViewModel,
        galleryViewModel: GalleryViewModel
    ) {
        Surface(
            modifier = modifier
        ) {
            ContainerView(
                navController = navHostController,
                cameraViewModel = cameraViewModel,
                prepareServiceViewModel = prepareServiceViewModel,
                galleryViewModel = galleryViewModel
            )
        }
    }

    //https://sonseungha.tistory.com/662
    //프레그먼트가 이동되는 뷰
    @SuppressLint("HardwareIds")
    @OptIn(ExperimentalPermissionsApi::class)
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    private fun ContainerView(
        navController: NavHostController,
        cameraViewModel: CameraViewModel,
        prepareServiceViewModel: PrepareServiceViewModel,
        galleryViewModel: GalleryViewModel
    ) {

        val multiplePermissionsState =
            rememberMultiplePermissionsState(permissions = PERMISSIONS_REQUIRED.toList()) {}
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
            cameraViewModel.bindCameraToLifeCycle(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.surfaceProvider,
                previewRotation = previewView.rotation.toInt()
            )
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { dest ->
                Firebase.analytics.logEvent("EVENT_USER_DESTINATION") {
                    param("timeStamp", System.currentTimeMillis())
                    param("destination", dest)
                    param(
                        "deviceID",
                        Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                    )
                }
            }

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
                previewState = previewState,
            )
            permissionAllowedGraph(
                routeName = Graph.PermissionAllowed.name,
                navHostController = navController,
                cameraInit = cameraInit,
                previewState = previewState,
            )
            usingCameraGraph(
                routeName = Graph.UsingCamera.name,
                navHostController = navController,
                galleryViewModel = galleryViewModel,
                previewView = previewView,
                cameraInit = cameraInit,
                cameraViewModel = cameraViewModel
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
        previewState: State<CameraState>,
    ) {
        navigation(startDestination = Page.Splash.name, route = routeName) {
            runSplashScreen(
                navHostController = navHostController,
                moveToNext = {
                    if (it.not()) {
                        navHostController.navigate(route = Page.TermOfUse.name) {
                            popUpTo(Page.Splash.name) { inclusive = true }
                        }
                    } else
                    //약관 동의 여부 확인 하고, 그 다음에 권한 받기 -> 만약 동의가 없는 경우, 동의 받고 권한을 체크함.
                        navHostController.navigate(route = Page.Perm.name) {
                            //백스택에서 스플래시 화면을 제거한다.
                            popUpTo(Page.Splash.name) { inclusive = true }
                        }

                }
            )

            //약관 동의화면 추가
            composable(route = Page.TermOfUse.name) {
                TermOfUseScreen.TermOfUseScreen {
                    prepareServiceViewModel.successToUse()
                    //권한 설정으로 넘어감.
                    navHostController.navigate(route = Page.Perm.name) {
                        popUpTo(Page.Splash.name) { inclusive = true }
                    }
                }
            }


            composable(route = Page.Perm.name) {
                //여기서부터는 Composable 영역
                PermScreen.PermScreen(
                    multiplePermissionsState = multiplePermissionsState,
                    permissionAllowed = {
                        navHostController.navigate(route = Graph.UsingCamera.name)
                    })
            }
            runAppLoadingScreen(navHostController = navHostController,
                cameraInit = cameraInit,
                previewState = previewState,
                onMoveToDownload = {
                    navHostController.navigate(Graph.DownloadProcess.name) {
                        popUpTo(it) { inclusive = true }
                    }
                })
            relatedWithDownload(routeName = Graph.DownloadProcess.name,
                navHostController = navHostController,
                onDoneDownload = {
                    navHostController.navigate(Page.AppLoading.name + "?afterDownload=${true}") {
                        popUpTo(Page.AppLoading.name) {}
                    }
                })

        }
    }

    private fun NavGraphBuilder.relatedWithDownload(
        routeName: String,
        navHostController: NavHostController,
        onDoneDownload: () -> Unit
    ) {
        navigation(startDestination = Page.ModelDownloadRequest.name, route = routeName) {
            //다운로드 요청 페이지
            composable(
                route = Page.ModelDownloadRequest.name,
//                enterTransition = {
//                fadeIn(
//                    animationSpec = tween(
//                        300, easing = LinearEasing
//                    )
//                ) + slideIntoContainer(
//                    animationSpec = tween(300, easing = EaseOut),
//                    towards = AnimatedContentTransitionScope.SlideDirection.Start
//                )
//            }, exitTransition = {
//                fadeOut(
//                    animationSpec = tween(
//                        300, easing = LinearEasing
//                    )
//                )
//            }
            ) {
                val prepareServiceViewModel =
                    it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
                LaunchedEffect(key1 = Unit) {
                    prepareServiceViewModel.startToTrackNetWorkState()
                    prepareServiceViewModel.requestForCheckDownload()
                }
                val checkState by prepareServiceViewModel.checkDownloadState.collectAsStateWithLifecycle()
                UIComponents.AnimatedSlideToLeft(
                    isVisible = checkState != null,
                ) {
                    ModelDownloadScreen.ModelDownloadRequestScreen(
                        isCheck = checkState,
                        moveToLoading = onDoneDownload,
                        moveToDownloadProgress = { type ->
                            val isDownload = type == CheckResponse.TYPE_MUST_DOWNLOAD
                            navHostController.navigate(
                                "${Page.ModelDownloadProgress.name}?isDownload={$isDownload}"
                            ) {
                                popUpTo(Page.ModelDownloadRequest.name) { inclusive = true }
                            }
                        },
                        requestCheckDownload = {
                            prepareServiceViewModel.requestForCheckDownload()
                        }
                    )
                }
                DisposableEffect(Unit) {
                    onDispose {
                        prepareServiceViewModel.clearStates()
                    }
                }
            }
            //다운로드 진행 페이지
            composable(route = "${Page.ModelDownloadProgress.name}?isDownload={isDownload}",
                arguments = listOf(navArgument("isDownload") {
                    type = NavType.BoolType
                    defaultValue = true
                }),
//                enterTransition = {
//                    fadeIn()
//                },
//                exitTransition = {
//                    fadeOut(
//                        animationSpec = tween(300, easing = LinearEasing)
//                    ) + slideOutOfContainer(
//                        animationSpec = tween(300, easing = EaseOut),
//                        towards = AnimatedContentTransitionScope.SlideDirection.End
//                    )
//                }
            ) {
                val prepareServiceViewModel =
                    it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
                val isDownload = it.arguments?.getBoolean("isDownload")
                val downloadState by prepareServiceViewModel.downloadState.collectAsStateWithLifecycle()
                val networkState by prepareServiceViewModel.networkState.collectAsStateWithLifecycle()
                LaunchedEffect(key1 = networkState) {
                    if (networkState) prepareServiceViewModel.requestForDownload()
                    else Log.e("네트워크 없음 알림.", "없습니다.")
                }
                LaunchedEffect(downloadState) {
                    downloadState?.run {
                        if (currentFileIndex + 1 == totalFileCnt && currentBytes == totalBytes) onDoneDownload()//다끝냄을 알림
                    }
                }
                UIComponents.AnimatedSlideToLeft(
                    isVisible = (downloadState != null),
                ) {
                    ModelDownloadScreen.ModelDownloadProgressScreen(
                        isDownload = isDownload,
                        downloadedInfo = downloadState!!,
                        onDismissEvent = { context ->
                            if (isDownload != null && isDownload.not()) onDoneDownload()
                            else (context as Activity).finish()
                        })
                }


            }


        }
    }

    private fun NavGraphBuilder.permissionAllowedGraph(
        routeName: String,
        navHostController: NavHostController,
        previewState: State<CameraState>,
        cameraInit: () -> Unit,
    ) {
        navigation(startDestination = Page.Splash.name, route = routeName) {
            runSplashScreen(
                navHostController = navHostController,
                moveToNext = {
                    navHostController.navigate(route = Page.AppLoading.name) {
                        //백스택에서 스플래시 화면을 제거한다.
                        popUpTo(Page.Splash.name) { inclusive = true }
                    }
                })
            runAppLoadingScreen(navHostController = navHostController,
                previewState = previewState,
                cameraInit = cameraInit,
                onMoveToDownload = {
                    navHostController.navigate(Graph.DownloadProcess.name) {
                        popUpTo(it) { inclusive = true }
                    }
                })
            relatedWithDownload(routeName = Graph.DownloadProcess.name,
                navHostController = navHostController,
                onDoneDownload = {
                    navHostController.navigate(Page.AppLoading.name + "?afterDownload=${true}") {}
                })
        }


    }

    //카메라를 사용할 때 사용되는 그래프
    @SuppressLint("HardwareIds")
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun NavGraphBuilder.usingCameraGraph(
        routeName: String,
        navHostController: NavHostController,
        previewView: PreviewView,
        cameraViewModel: CameraViewModel,
        galleryViewModel: GalleryViewModel,
        cameraInit: () -> Unit,
    ) {
        navigation(startDestination = Page.Cam.name, route = routeName) {
            //카메라 화면
            composable(route = Page.Cam.name,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }) {
                val isOnClose = remember { mutableStateOf(false) }
                val localActivity = LocalContext.current as Activity
                val userSet by cameraViewModel.userSetState.collectAsStateWithLifecycle()
                LaunchedEffect(key1 = userSet) {
                    cameraViewModel.loadUserSet()
                }
                if (isOnClose.value.not()) {
                    UIComponents.AnimatedSlideToRight(isVisible = userSet != null) {
                        Screen(
                            cameraViewModel,
                            previewView = previewView,
                            onClickSettingBtnEvent = {
                                navHostController.navigate(route = Page.Setting.name) {}
                            },
                            onClickGalleryBtn = {
                                navHostController.navigate(route = Page.Images.name) {}
                            },
                            cameraInit = cameraInit,
                            onFinishEvent = {
                                isOnClose.value = true
                                localActivity.finish()
                            },
                            userSet = userSet!!
                        )
                    }

                }


            }

            //최근 촬영된 이미지들 보여주는 함수
            composable(route = Page.Images.name, enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }) {

                val imageList = galleryViewModel.capturedImageState.collectAsState()
                val context = LocalContext.current
                val deleteTargetIndex = remember {
                    mutableStateOf<Int?>(null)
                }
                LaunchedEffect(Unit) {
                    galleryViewModel.loadImages()
                }
                val coroutineScope = rememberCoroutineScope()
                //R 버전 이상 부터는 이미지 삭제시, 파일에 대한 권한이 필요.
                val deleteLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = {
                        if (it.resultCode == Activity.RESULT_OK) {
                            galleryViewModel.deleteImage(deleteTargetIndex.value!!, true)
                            deleteTargetIndex.value = null
                        }
                    })

                if (imageList.value != null) {
                    GalleryScreen.GalleryScreen(imageList = imageList.value!!,
                        onLoadImages = { galleryViewModel.loadImages() },
                        onDeleteImage = { index, func ->
                            coroutineScope.launch {
                                deleteTargetIndex.value = index
                                deleteImage(
                                    context.contentResolver,
                                    deleteLauncher,
                                    galleryViewModel,
                                    index = index,
                                    uri = imageList.value!![index].dataUri!!,
                                )
                                galleryViewModel.deleteCompleteState.collectLatest {
                                    it?.run { func() }
                                }
                            }
                        },
                        onBackPressed = {
                            navHostController.navigateUp()
                        })


                }
            }
            //설정 화면
            composable(route = Page.Setting.name,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(150)
                    )
                }, exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(150)
                    )
                }) {
                val userSet by cameraViewModel.userSetState.collectAsStateWithLifecycle()
                LaunchedEffect(key1 = Unit, key2 = userSet) {
                    cameraViewModel.loadUserSet()
                }
                UIComponents.AnimatedSlideToRight(isVisible = userSet != null) {
                    SettingScreen.Screen(userSet = userSet!!, onSaveUserSet = { setting ->
                        cameraViewModel.saveUserSet(setting)
                    },
                        onBackPressed = {
                            navHostController.navigateUp()
                        }
                    )
                }
            }


        }
    }

    private fun NavGraphBuilder.runSplashScreen(
        navHostController: NavHostController,
        moveToNext: (Boolean) -> Unit
    ) {
        val splashPage = Page.Splash.name

        composable(route = splashPage, enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            )
        }, exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            )
        }) {
            //여기서부터는 Composable 영역
            val prepareServiceViewModel =
                it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
            val checkState = prepareServiceViewModel.checkUserSuccess.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                prepareServiceViewModel.checkToUse()
            }
            PrepareServiceScreens.SplashScreen()
            LaunchedEffect(checkState.value) {
                //1초 뒤에 앱 로딩 화면으로 넘어감.
                if (checkState.value != null) {
                    delay(1000)
                    moveToNext(checkState.value!!)
                }
            }
        }
    }

    private fun NavGraphBuilder.runAppLoadingScreen(
        navHostController: NavHostController,
        cameraInit: () -> Unit,
        previewState: State<CameraState>,
        onMoveToDownload: (String) -> Unit,
    ) {
        val appLoadingPage = Page.AppLoading.name
        composable(route = "$appLoadingPage?afterDownload={afterDownload}",
            arguments = listOf(navArgument("afterDownload") {
                type = NavType.BoolType
                defaultValue = false
            }),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) {
            val afterDownload = it.arguments?.getBoolean("afterDownload")
            val prepareServiceViewModel =
                it.sharedViewModel<PrepareServiceViewModel>(navHostController = navHostController)
            //앱로딩이 끝나면, 카메라화면을 보여주도록 한다.
            PrepareServiceScreens.AppLoadingScreen(
                previewState = previewState,
                cameraInit = cameraInit,
                prepareServiceViewModel = prepareServiceViewModel,
                onAfterLoadedEvent = {
                    navHostController.navigate(Graph.UsingCamera.name) {
                        //앱 로딩 페이지는 뒤로가기 해도 보여주지 않음 .
                        popUpTo(route = appLoadingPage) { inclusive = true }
                    }
                },
                onMoveToDownload = {
                    onMoveToDownload(appLoadingPage)
                },
                isAfterDownload = afterDownload!!,
            )

        }
    }

    //네비게이션 간 뷰모델
    @Composable
    inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navHostController: NavHostController): T {
        val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
        val parentEntry = remember(this) {
            navHostController.getBackStackEntry(navGraphRoute)
        }
        return hiltViewModel(parentEntry)
    }


    private fun deleteImage(
        contentResolver: ContentResolver,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        galleryViewModel: GalleryViewModel,
        index: Int,
        uri: Uri,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createDeleteRequest(
                contentResolver,
                arrayListOf(uri)
            ).intentSender.run {
                launcher.launch(IntentSenderRequest.Builder(this).build())
            }
            false
        } else {
            galleryViewModel.deleteImage(index, false)
            true
        }
    }
}
