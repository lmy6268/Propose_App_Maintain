package com.example.proposeapplication.presentation.view.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.CameraViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.LowerButtons
import com.example.proposeapplication.presentation.view.camera.CameraModules.PoseResultScreen
import com.example.proposeapplication.presentation.view.camera.CameraModules.UpperButtons
import com.example.proposeapplication.utils.pose.PoseData
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

    val capturedThumbnailBitmap: Bitmap by cameraViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
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
                .fillMaxWidth()
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
                    .size(
                        cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp
                    )
//                .aspectRatio(viewRateState.second)
                    .align(Alignment.TopCenter),
                poseResultData = poseRecPair,
                onVisibilityEvent = {
                    poseScreenVisibleState.value = false
                }
            )
        }


//        Box(
//            Modifier
//                .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
////                .aspectRatio(viewRateState.second)
//                .align(Alignment.TopCenter)
//        ) {
//
//
//            if (poseRecPair != Pair(
//                    null, null
//                ) && poseCloseState.value.not()
//            ) { //포즈 목록이 도착하면
//                recomPoseSizeState.intValue = poseRecPair.second!!.size
//
//                var offset by remember {
//                    mutableStateOf(
//                        Offset(
//                            cameraDisplayPxSize.value.width / 2f,
//                            cameraDisplayPxSize.value.height / 2f
//                        )
//                    )
//                }
//                var zoom by remember {
//                    mutableFloatStateOf(1F)
//                }
//
//                val transformState =
//                    rememberTransformableState { zoomChange, offsetChange, _ ->
//                        if (zoom * zoomChange in 0.5f..2f) zoom *= zoomChange
//                        val tmp = offset + offsetChange
//                        if (tmp.x in -1f..cameraDisplayPxSize.value.width.toFloat() - 1
//                            && tmp.y in -1f..cameraDisplayPxSize.value.height.toFloat() - lowerBarDisplayPxSize.value.height.toFloat() + 20F
//                        )
//                            offset += offsetChange
//                    }
//
//                Canvas(
//                    modifier = Modifier
//                        .graphicsLayer(
//                            scaleX = zoom,
//                            scaleY = zoom,
//                            translationX = offset.x,
//                            translationY = offset.y
//                        )
//                        .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
//                        .transformable(state = transformState)
//
//                ) {
////                    if (poseRecPair!!.first!!.isNotEmpty()) {
////                        offset = offset.copy(
////                            (poseRecPair!!.first!![0] * size.width).toFloat(),
////                            (poseRecPair!!.first!![0] * size.height).toFloat()
////                        )
////                    }
//
////                    drawImage(
////                        image = BitmapFactory.decodeResource(
////                            context.resources,
////                            poseRecPair!!.second!![nextRecomPoseState.intValue].poseDrawableId
////                        ).asImageBitmap(),
////                    )
//                    drawRect(
//                        Color.White,
//                        size = Size(200F, 200F)
//                    )
//
//                }
//                //다음 포즈 선택 버튼
//                if ((selectedModeIdxState.intValue in 0..1) && recomPoseSizeState.intValue > 0)
//                    Box(
//                        Modifier.size(
//                            cameraDisplaySize.value.width.dp,
//                            cameraDisplaySize.value.height.dp
//                        )
//                    ) {
//                        IconButton(
//                            onClick = {
//                                poseCloseState.value = true
//                            },
//                            modifier = Modifier
//                                .size(50.dp)
//                                .offset(x = 0.dp, y = upperButtonsRowSize.value.height.dp)
//                                .align(Alignment.TopEnd)
//
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.based_circle),
//                                contentDescription = "배경",
//                                tint = Color.White
//                            )
//                            Icon(
//                                painter = painterResource(id = R.drawable.close),
//                                contentDescription = "포즈 추천 닫기"
//                            )
//                        }
//
//                        IconButton(modifier = Modifier
//                            .size(50.dp)
//                            .offset(x = (-20).dp)
//                            .align(
//                                Alignment.CenterEnd
//                            ), onClick = {
//                            if (nextRecomPoseState.intValue in 0 until recomPoseSizeState.intValue - 1) nextRecomPoseState.intValue += 1
//                            else nextRecomPoseState.intValue = 0
//                        }) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.based_circle),
//                                contentDescription = "배경",
//                                tint = Color.White
//                            )
//                            Icon(
//                                painter = painterResource(id = R.drawable.refresh),
//                                contentDescription = "배경"
//                            )
//                        }
//                    }
//
//            } else if (poseRecPair == Pair(null, null)) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                )
//            }
//        }


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







