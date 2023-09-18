package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModules.LowerButtons
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModules.UpperButtons
import com.hanadulset.pro_poseapp.presentation.feature.camera.PoseScreen.PoseResultScreen
import com.hanadulset.pro_poseapp.utils.pose.PoseData
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
    preRunView: PreviewView,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    onClickSettingBtnEvent: () -> Unit
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
    val isInitState = remember {
        mutableStateOf(false)
    }
    val selectedPoseId = remember {
        mutableStateOf<Int?>(null)
    }


    val capturedThumbnailBitmap: Uri? by cameraViewModel.capturedBitmapState.collectAsState() //업데이트된 캡쳐화면을 가지고 있는 변수
    val compResultDirection: String by cameraViewModel.compResultState.collectAsState()
    val viewRateIdxState by cameraViewModel.viewRateIdxState.collectAsStateWithLifecycleRemember(
        initial = 0
    )
    val aspectRatioState by cameraViewModel.aspectRatioState.collectAsStateWithLifecycleRemember(
        initial = cameraViewModel.aspectRatioState.replayCache[0]
    )
    val viewRateState by cameraViewModel.viewRateState.collectAsState()
    val testS3State by cameraViewModel.testOBject.collectAsState()

    val previewView = remember {
        preRunView.apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                cameraViewModel.getPoseFromImage(uri)
//                isPressedFixedBtn.value = false
            }
        }

    val poseRecPair: Pair<DoubleArray?, List<PoseData>?>? by cameraViewModel.poseResultState.collectAsState()

//        뷰를 계속 업데이트 하면서 생겼던 오류
    if (isUpdated.value.not()) {
        isUpdated.value = true
    }

    LaunchedEffect(compResultDirection) {
        if (compResultDirection != "") Toast.makeText(
            context,
            compResultDirection,
            Toast.LENGTH_SHORT
        ).show()
    }
    LaunchedEffect(testS3State) {
        if (testS3State != "") Toast.makeText(context, "버전 아이디: $testS3State", Toast.LENGTH_SHORT)
            .show()
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

        val shutterAnimationState = remember {
            mutableStateOf(false)
        }


        LaunchedEffect(viewRateState) {
            if (isInitState.value.not()) isInitState.value = true
            else cameraViewModel.showPreview(
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

//        AnimatedVisibility(
//            visible = shutterAnimationState.value,
//        ) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
////                .aspectRatio(viewRateState.second)
//                    .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
//                    .background(Color(0x50FFFFFF))
//            )
//        }
        CameraModules.HorizonAndVerticalCheckScreen(
            modifier = Modifier
                .align(Alignment.TopCenter)
//                .aspectRatio(viewRateState.second)
                .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
        )

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
                onPoseChangeEvent = {
                    selectedPoseId.value = it.poseId //포즈 변경 시, 해당 데이터를 트레킹 함.
                },
                clickedItemIndexState = 0,
                onVisibilityEvent = {
                    poseScreenVisibleState.value = false
                },

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
            onSettingEvent =
            onClickSettingBtnEvent,
            viewRateClickEvent = { idx ->
                cameraViewModel.changeViewRate(idx)
            })


//        CompositionArrow(
//            arrowDirection = compResultDirection,
//            modifier = Modifier
//                .aspectRatio(aspectRatioState)
//                .heightIn(150.dp)
//                .align(Alignment.Center)
//                .offset(y = (-100).dp)
//        )

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
                shutterAnimationState.value = true
                cameraViewModel.getPhoto()
                shutterAnimationState.value = false
            },
            poseBtnClickEvent = {
                cameraViewModel.reqPoseRecommend()
                poseScreenVisibleState.value = true
            },
            fixedButtonPressedEvent = {
                isPressedFixedBtn.value = !isPressedFixedBtn.value
                cameraViewModel.controlFixedScreen(isPressedFixedBtn.value)
            },
            zoomInOutEvent = {
                cameraViewModel.setZoomLevel(it)
            },
            ddaogiFeatureEvent = {
//                if(isPressedFixedBtn.value)

                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )

//                cameraViewModel.reqCompRecommend()
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







