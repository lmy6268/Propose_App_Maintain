package com.hanadulset.pro_poseapp.presentation.view.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.hanadulset.pro_poseapp.presentation.CameraViewModel
import com.hanadulset.pro_poseapp.presentation.view.camera.CameraModules.CompositionArrow
import com.hanadulset.pro_poseapp.presentation.view.camera.CameraModules.LowerButtons
import com.hanadulset.pro_poseapp.presentation.view.camera.CameraModules.UpperButtons
import com.hanadulset.pro_poseapp.presentation.view.camera.PoseScreen.PoseResultScreen
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import com.mutualmobile.composesensors.rememberAccelerometerSensorState
import com.mutualmobile.composesensors.rememberRotationVectorSensorState
import kotlinx.coroutines.flow.Flow


@Composable
fun <T> Flow<T>.collectAsStateWithLifecycleRemember(
    initial: T,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val flowLifecycleAware = remember(this, lifecycleOwner) {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }
    return flowLifecycleAware.collectAsState(initial)
}

@SuppressLint("SuspiciousIndentation")
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun Screen(
    navController: NavHostController, cameraViewModel: CameraViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val localDensity = LocalDensity.current
    val accelerationState = rememberAccelerometerSensorState()
    val rotationState = rememberRotationVectorSensorState()

    //요청된 고정 화면에 대한 결과값을 가지고 있는 State 변수
    val resFixedScreenState by cameraViewModel.fixedScreenState.collectAsState()

    val isUpdated = remember {
        mutableStateOf(false)
    }
    val isPressedFixedBtn = remember {
        mutableStateOf(false)
    }
    val selectedModeIdxState = remember {
        mutableIntStateOf(1)
    }
    val poseScreenVisibleState = remember {
        mutableStateOf(false)
    }

    val capturedThumbnailBitmap: Uri? by cameraViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
    val compResultDirection: String by cameraViewModel.compResultState.collectAsState()
    val viewRateIdxState by cameraViewModel.viewRateIdxState.collectAsStateWithLifecycleRemember(
        initial = 0
    )
    val aspectRatioState by cameraViewModel.aspectRatioState.collectAsStateWithLifecycleRemember(
        initial = cameraViewModel.aspectRatioState.replayCache[0]
    )
    val viewRateState by cameraViewModel.viewRateState.collectAsStateWithLifecycleRemember(
        initial = cameraViewModel.viewRateState.replayCache[0]
    )
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val poseRecPair: Pair<DoubleArray?, List<PoseData>?>? by cameraViewModel.poseResultState.collectAsState()

//        뷰를 계속 업데이트 하면서 생겼던 오류
    if (isUpdated.value.not()) {
        isUpdated.value = true
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val upperButtonsRowSize = remember {
            mutableStateOf(IntSize(0, 0))
        }
        val cameraDisplaySize = remember {
            mutableStateOf(IntSize(0, 0))
        }
        val cameraDisplayPxSize = remember {
            mutableStateOf(IntSize(0, 0))
        }
        val lowerBarDisplaySize = remember {
            mutableStateOf(IntSize(0, 0))
        }
        val lowerBarDisplayPxSize = remember {
            mutableStateOf(IntSize(0, 0))
        }

        LaunchedEffect(viewRateState) {
            cameraViewModel.showPreview(
                lifecycleOwner,
                previewView.surfaceProvider,
                viewRateState,
                previewView.rotation.toInt()
            )
        }
        AndroidView(factory = {
            previewView
        },
            modifier = Modifier
//                .fillMaxWidth()
                .aspectRatio(aspectRatioState)
                .animateContentSize { initialValue, targetValue -> }
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        cameraDisplaySize.value = coordinates.size.let {
                            IntSize(
                                it.width.toDp().value.toInt(), it.height.toDp().value.toInt()
                            )
                        }
                        cameraDisplayPxSize.value = coordinates.size.let {
                            IntSize(it.width, it.height)
                        }
                    }
                }
                .align(Alignment.TopCenter)
        ) {

        }

        //엣지 화면
        ShowEdgeImage(
            capturedEdgesBitmap = resFixedScreenState,
            modifier = Modifier
                .align(Alignment.TopCenter)
//                .aspectRatio(viewRateState.second)
                .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
        )

        if (poseScreenVisibleState.value) {
            PoseResultScreen(
                cameraDisplaySize = cameraDisplaySize,
                cameraDisplayPxSize = cameraDisplayPxSize,
                lowerBarDisplayPxSize = lowerBarDisplayPxSize,
                upperButtonsRowSize = upperButtonsRowSize,
                modifier = Modifier
//                    .size(
//                        cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp
//                    )
                    .aspectRatio(aspectRatioState)
                    .align(Alignment.TopCenter),
                poseResultData = poseRecPair,
                onVisibilityEvent = {
                    poseScreenVisibleState.value = false
                }
            )
        }


        //상단 버튼들
        UpperButtons(modifier = Modifier
            .align(Alignment.TopCenter)
            .heightIn(80.dp)
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                with(localDensity) {
                    upperButtonsRowSize.value = coordinates.size.let {
                        IntSize(
                            it.width.toDp().value.toInt(), it.height.toDp().value.toInt()
                        )
                    }
                }
            },
            navController = navController,
            viewRateIdx = viewRateIdxState,
            mainColor = MaterialTheme.colors.primary,
            selectedModeIdxState = selectedModeIdxState,
            viewRateClickEvent = { idx ->
                cameraViewModel.changeViewRate(idx)
            })

        CompositionArrow(
            arrowDirection = compResultDirection,
            modifier = Modifier
                .aspectRatio(aspectRatioState)
                .heightIn(150.dp)
                .align(Alignment.Center)
                .offset(y = (-100).dp)
        )

        LowerButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(150.dp)
                .padding(bottom = 100.dp)
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        lowerBarDisplaySize.value = coordinates.size.let {
                            IntSize(
                                it.width.toDp().value.toInt(), it.height.toDp().value.toInt()
                            )
                        }
                        lowerBarDisplayPxSize.value = coordinates.size.let {
                            IntSize(it.width, it.height)
                        }
                    }
                },
            selectedModeIdxState = selectedModeIdxState,
            capturedImageBitmap = capturedThumbnailBitmap,
            captureImageEvent = {
                cameraViewModel.getPhoto()
            },
            poseBtnClickEvent = {
                cameraViewModel.reqPoseRecommend()
                poseScreenVisibleState.value = true
            },
            fixedButtonPressedEvent = {
                cameraViewModel.controlFixedScreen(!isPressedFixedBtn.value)
                isPressedFixedBtn.value = !isPressedFixedBtn.value
            },
            zoomInOutEvent = {
                cameraViewModel.setZoomLevel(it)
            }
        )

    }

}

//고정된 화면을 보여주는 컴포저블
@Composable
fun ShowEdgeImage(
    capturedEdgesBitmap: Bitmap?, // 고정된 이미지
    modifier: Modifier = Modifier, //수정자

) {
    if (capturedEdgesBitmap != null) {
        Image(
            modifier = modifier.alpha(0.5F),
            bitmap = capturedEdgesBitmap.asImageBitmap(), contentDescription = "Edge Image"
        )
    }

}







