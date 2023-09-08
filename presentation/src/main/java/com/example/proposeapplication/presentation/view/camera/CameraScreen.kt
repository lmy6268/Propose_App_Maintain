package com.example.proposeapplication.presentation.view.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
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
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.LowerButtons
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
@Composable
fun Screen(
    navController: NavHostController, mainViewModel: MainViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val localDensity = LocalDensity.current
    val previewView = PreviewView(context)
    val isUpdated = remember {
        mutableStateOf(false)
    }
    val isPressedFixedBtn = remember {
        mutableStateOf(false)
    }
    val selectedModeIdxState = remember {
        mutableIntStateOf(1)
    }
    val nextRecomPoseState = remember {
        mutableIntStateOf(0)
    }
    val recomPoseSizeState = remember {
        mutableIntStateOf(0)
    }
    val captureBtnClickState = remember {
        mutableStateOf(false)
    }
    val poseCloseState = remember {
        mutableStateOf(false)
    }

    val capturedEdgesBitmap: Bitmap? by mainViewModel.edgeDetectBitmapState.collectAsStateWithLifecycleRemember(
        initial = null
    )//업데이트된 고정화면을 가지고 있는 변수
    val capturedThumbnailBitmap: Bitmap by mainViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
    val compResultDirection: String by mainViewModel.compResultState.collectAsState()
    val viewRateIdxState by mainViewModel.viewRateIdxState.collectAsStateWithLifecycleRemember(
        initial = 0
    )
    val viewRateState by mainViewModel.viewRateState.collectAsStateWithLifecycleRemember(
        initial = mainViewModel.viewRateState.replayCache[0]
    )

    val poseRecPair: Pair<DoubleArray?, List<PoseData>?>? by mainViewModel.poseResultState.collectAsStateWithLifecycleRemember(
        initial = null
    )

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
        val boxSize = remember {
            mutableStateOf(IntSize(0, 0))
        }

        AndroidView(factory = {
//            mainViewModel.testS3()
            previewView.apply {
                mainViewModel.showPreview(
                    lifecycleOwner, previewView.surfaceProvider, viewRateState.first
                )
                this.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(viewRateState.second)
                .animateContentSize { initialValue, targetValue -> }
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        boxSize.value = coordinates.size.let {
                            IntSize(
                                it.width.toDp().value.toInt(), it.height.toDp().value.toInt()
                            )
                        }
                    }
                }
                .align(Alignment.TopCenter)
        ) {
            it.scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        //엣지 화면
        ShowEdgeImage(
            mainViewModel = mainViewModel,
            capturedEdgesBitmap = capturedEdgesBitmap,
            isPressedFixedBtn = isPressedFixedBtn,
            modifier = Modifier
                .align(Alignment.TopCenter)
//                .aspectRatio(viewRateState.second)
                .size(boxSize.value.width.dp, boxSize.value.height.dp)
//                .then(
//                    if (viewRateIdxState == 0) Modifier.offset(0.dp, (-100).dp)
//                    else Modifier
//                )
        )
        Box(
            Modifier
                .size(boxSize.value.width.dp, boxSize.value.height.dp)
//                .aspectRatio(viewRateState.second)
                .align(Alignment.TopCenter)
        ) {


            if (poseRecPair != null && poseRecPair != Pair(
                    null, null
                ) && poseCloseState.value.not()
            ) { //포즈 목록이 도착하면
                recomPoseSizeState.intValue = poseRecPair!!.second!!.size

                var offsetX by remember { mutableFloatStateOf(boxSize.value.width / 2F) } //기본 좌표
                var offsetY by remember { mutableFloatStateOf((boxSize.value.height / 2F)) }
                var zoom by remember {
                    mutableFloatStateOf(1F)
                }

                Canvas(modifier = Modifier
                    .size(boxSize.value.width.dp, boxSize.value.height.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                        detectTransformGestures { centroid, pan, gestureZoom, rotation ->
                            zoom *= gestureZoom
                        }
                    }

                ) {
                    if (poseRecPair!!.first!!.isNotEmpty()) {
                        offsetX = (poseRecPair!!.first!![0] * size.width).toFloat()
                        offsetY = (poseRecPair!!.first!![0] * size.height).toFloat()
                    }

                    withTransform({
                        scale(zoom)
                        translate(offsetX, offsetY)
                    }) {
                        drawImage(
                            image = BitmapFactory.decodeResource(
                                context.resources,
                                poseRecPair!!.second!![nextRecomPoseState.intValue].poseDrawableId
                            ).asImageBitmap(),
                        )
                    }
                }
                //다음 포즈 선택 버튼
                if ((selectedModeIdxState.intValue in 0..1) && recomPoseSizeState.intValue > 0)
                    Box(
                        Modifier.size(boxSize.value.width.dp, boxSize.value.height.dp)
                    ) {
                        IconButton(
                            onClick = {
                                poseCloseState.value = true
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .offset(x = 0.dp, y = upperButtonsRowSize.value.height.dp)
                                .align(Alignment.TopEnd)

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.based_circle),
                                contentDescription = "배경",
                                tint = Color.White
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "포즈 추천 닫기"
                            )
                        }

                        IconButton(modifier = Modifier
                            .size(50.dp)
                            .offset(x = (-20).dp)
                            .align(
                                Alignment.CenterEnd
                            ), onClick = {
                            if (nextRecomPoseState.intValue in 0 until recomPoseSizeState.intValue - 1) nextRecomPoseState.intValue += 1
                            else nextRecomPoseState.intValue = 0
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.based_circle),
                                contentDescription = "배경",
                                tint = Color.White
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.refresh),
                                contentDescription = "배경"
                            )
                        }
                    }

            } else if (poseRecPair == Pair(null, null)) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
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
                mainViewModel.changeViewRate(idx)
                mainViewModel.showPreview(
                    lifecycleOwner, previewView.surfaceProvider, viewRateState.first
                )
            })

        CompositionArrow(
            arrowDirection = compResultDirection,
            modifier = Modifier
                .aspectRatio(viewRateState.second)
                .heightIn(150.dp)
                .align(Alignment.Center)
                .offset(y = (-100).dp)
        )

        LowerButtons(
            captureBtnClickState = captureBtnClickState,
            selectedModeIdxState = selectedModeIdxState,
            capturedThumbnailBitmap = capturedThumbnailBitmap,
            mainViewModel = mainViewModel,
            isPressedFixedBtn = isPressedFixedBtn,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .heightIn(150.dp)
                .padding(bottom = 100.dp)
        ) {
            mainViewModel.reqPoseRecommend()


        }

    }

}

@Composable
fun ShowEdgeImage(
    mainViewModel: MainViewModel,
    capturedEdgesBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    isPressedFixedBtn: MutableState<Boolean>
) {
    //Fix for AfterImage error when use fixedScreen feature.
    if (isPressedFixedBtn.value) {
        val reqState: Boolean by mainViewModel.reqFixedScreenState.collectAsState(true)
        LaunchedEffect(Unit) {
            mainViewModel.reqFixedScreenState.value = true
        }
        // 요청 처리가 끝났다면 -> 이미지를 받아온다.
        if (!reqState) capturedEdgesBitmap?.let { bitmap ->
            Image(
                modifier = modifier.alpha(0.5F),

                bitmap = bitmap.asImageBitmap(), contentDescription = "Edge Image"
            )
        }
    }

}







