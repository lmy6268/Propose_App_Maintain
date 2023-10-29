package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.net.Uri
import android.util.Range
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.hanadulset.pro_poseapp.presentation.component.UIComponents.AnimatedSlideToLeft
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


//리컴포지션시 데이터가 손실되는 문제를 해결하기 위한, 전역변수

class GetContentActivityResult(
    private val launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {
    fun launch() {
        launcher.launch(
            PickVisualMediaRequest(
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun Screen(
    cameraViewModel: CameraViewModel,
    previewView: PreviewView,
    onClickGalleryBtn: () -> Unit,
    onClickSettingBtnEvent: () -> Unit,
    cameraInit: () -> Unit,
    onFinishEvent: () -> Unit
) {

    BackHandler(onBack = onFinishEvent)
    val userEdgeDetectionSwitch = remember { mutableStateOf(false) }
    val systemEdgeDetectionSwitch = remember { mutableStateOf(false) }

    @Composable
    fun rememberGetContentActivityResult(
        aspectRatio: ViewRate,
        onGetPoseFromImage: (Uri?) -> Unit,
    ): GetContentActivityResult {

        val cropImageLauncher =
            rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
                if (result.isSuccessful) {
                    val uriContent = result.uriContent
                    onGetPoseFromImage(uriContent)
                    userEdgeDetectionSwitch.value = true
                    if (systemEdgeDetectionSwitch.value) systemEdgeDetectionSwitch.value = false
                }

            }

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                if (it != null) {
                    val cropOptions = CropImageContractOptions(
                        it, CropImageOptions(
                            aspectRatioX = aspectRatio.aspectRatioSize.width,
                            aspectRatioY = aspectRatio.aspectRatioSize.height,
                            fixAspectRatio = true,
                        )
                    )
                    cropImageLauncher.launch(cropOptions)
                }

            }

        return remember(launcher) {
            GetContentActivityResult(launcher = launcher)
        }
    }

    val screenSize =
        LocalConfiguration.current.let { DpSize(it.screenWidthDp.dp, it.screenHeightDp.dp) }


    val openGalleryEvent by rememberUpdatedState(newValue = onClickGalleryBtn)
    val localDensity = LocalDensity.current
    val aspectRatio by cameraViewModel.aspectRatioState.collectAsStateWithLifecycle()

    //여기서 결과 값을 전달 받은경우에만 따오기  버튼을 활성화 하도록 해보자
    val getImageForEdgeLauncher = rememberGetContentActivityResult(
        onGetPoseFromImage = { uri -> cameraViewModel.getPoseFromImage(uri) },
        aspectRatio = aspectRatio
    )


    val stopTrackingPoint = remember { { cameraViewModel.stopToTrack() } }


    //포즈 추천 결과
    val backgroundAnalysisResult by cameraViewModel.backgroundDataState.collectAsStateWithLifecycle()

    val needToCloseViewRate = remember { mutableStateOf(false) }
    val upperBarSize = remember { mutableStateOf<DpSize?>(null) }
    val scope = rememberCoroutineScope()






    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter(
                RequestDisallowInterceptTouchEvent()
            ) { motionEvent -> //여기서 포커스 링을 세팅하는데, 여기서 문제가 생긴 것 같다.
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        upperBarSize.value?.run {
                            localDensity.run {
                                val xRange = Range(0F, width.toPx())
                                val yRange = Range(0F, height.toPx())
                                val isOkayToClose =
                                    (motionEvent.x in xRange && motionEvent.y in yRange).not()
                                //화면 변경 창 닫기
                                if (isOkayToClose) scope.launch {
                                    needToCloseViewRate.value = true
                                    delay(100L)
                                    needToCloseViewRate.value = false
                                }
                            }
                        }
                        false
                    }

                    else -> {
                        false
                    }
                }

            },
        contentAlignment = Alignment.Center
    ) {
        val galleryImageUri by cameraViewModel.capturedBitmapState.collectAsStateWithLifecycle()

        val previewAreaSize = remember {
            mutableStateOf(DpSize(0.dp, 0.dp))
        }
        val compPointOffset by cameraViewModel.pointOffsetState.collectAsStateWithLifecycle()
        val edgeImageState by cameraViewModel.fixedScreenState.collectAsStateWithLifecycle()

        val showPoseListUnderBarState = rememberSaveable { mutableStateOf(false) }
        val lowerBarSize = remember {
            mutableStateOf(DpSize(0.dp, 0.dp))
        }

        val compStateInit = false
        val compState = rememberSaveable { mutableStateOf(compStateInit) }
        //햔재 전달된 포즈 데이터
        val currentPoseDataList by cameraViewModel.poseResultState.collectAsStateWithLifecycle()
        val selectedPoseIndex = rememberSaveable {
            mutableIntStateOf(1)
        }
        val captureBtnClickState = remember { mutableStateOf(false) }
        val poseScale = remember {
            mutableFloatStateOf(1f)
        }

        //셔터 버튼을 눌렀을 때 발생되는 이벤트
        val shutterEvent = remember {
            {
                cameraViewModel.getPhoto(
                    EventLog(
                        eventId = EventLog.EVENT_CAPTURE,
                        poseID = currentPoseDataList?.get(selectedPoseIndex.intValue)?.poseId ?: -1,
                        prevRecommendPoses = currentPoseDataList?.let { it.map { poseData -> poseData.poseId } },
                        timestamp = System.currentTimeMillis().toString(),
                        backgroundId = backgroundAnalysisResult?.first,
                        backgroundHog = backgroundAnalysisResult?.second?.toString()
                    )
                )
                stopTrackingPoint() // 구도 추천을 다시 시작함.
                captureBtnClickState.value = true
            }
        }


        //중간 ( 미리보기, 포즈 추천, 구도 추천 보기 화면 ) 모듈
        CameraScreenPreviewArea.PreviewArea(
            modifier = Modifier
                .aspectRatio(aspectRatio.aspectRatioSize.let { (it.width.toFloat() / it.height.toFloat()) })
                .onGloballyPositioned { coordinates ->
                    coordinates.size.let {
                        previewAreaSize.value = DpSize(it.width.dp, it.height.dp)
                    }
                }
                .align(Alignment.Center),
            initCamera = cameraInit,
            preview = previewView,
            upperBarSize = upperBarSize.value ?: DpSize(0.dp, 0.dp),
            isRecommendCompEnabled = compState.value,
            loadLastImage = { cameraViewModel.getLastImage() },
            onFocusEvent = {
                cameraViewModel.setFocus(it.first, it.second)
            },
            poseData = currentPoseDataList?.get(selectedPoseIndex.intValue),
            pointerOffset = compPointOffset,
            edgeImageBitmap = edgeImageState,
            triggerNewPoint = {
                cameraViewModel.startToTrack(with(localDensity) {
                    Size(
                        it.width.toPx(),
                        it.height.toPx()
                    )
                })
            },
            onStopTrackPoint = stopTrackingPoint,
            isCaptured = captureBtnClickState.value,
            onStopCaptureAnimation = {
                captureBtnClickState.value = false
            },
            poseScale = poseScale.floatValue
        )

        //상단 버튼
        CameraScreenUpperBar.UpperBar(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    if (upperBarSize.value == null) {
                        coordinates.size.run {
                            upperBarSize.value = with(localDensity) {
                                DpSize(width = width.toDp(), height = height.toDp())
                            }
                        }
                    }
                }
//                .padding(top = 30.dp)
                .height(screenSize.height / 9)
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            viewRateList = cameraViewModel.getViewRateList(),
            onChangeCompSetEvent = {
                compState.value = it
                if (it.not()) stopTrackingPoint()
            },
            moveToInfo = onClickSettingBtnEvent,
            onSelectedViewRate = { idx ->
                // 화면 비율을 조절할 때 발생하는 이벤트
                if (cameraViewModel.changeViewRate(idx = idx).not()) cameraInit()
                stopTrackingPoint()
            },
            compStateInit = compStateInit,
            needToCloseViewRateList = needToCloseViewRate.value
        )

        AnimatedSlideToLeft(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        lowerBarSize.value.let { lowSize ->
                            if (lowSize.width == 0.dp && lowSize.height == 0.dp)
                                lowerBarSize.value = coordinates.size.let {
                                    DpSize(it.width.toDp(), it.height.toDp())
                                }
                        }
                    }
                },
            isVisible = showPoseListUnderBarState.value.not()
        ) {
            //하단바 관련 모듈
            CameraScreenUnderBar.UnderBar(
                //따오기 관련 처리


                //줌레벨 변경 시
                onZoomLevelChangeEvent = { zoomLevel ->
                    cameraViewModel.setZoomLevel(zoomLevel)
                },
                onGalleryButtonClickEvent = openGalleryEvent,
                //촬영 시에 EventLog를 인자로 넘겨줘야한다.
                onShutterClickEvent = shutterEvent,
                //현재 선택된 인덱스에 대해서 전달하기 위함..
                //어차피 한쪽 (포즈 추천 화면)은 읽기만 하면된다.
                //고정 버튼 클릭시
                onSystemEdgeDetectionClicked = {
                    if (userEdgeDetectionSwitch.value) userEdgeDetectionSwitch.value = false
                    cameraViewModel.controlFixedScreen(systemEdgeDetectionSwitch.value.not())
                    systemEdgeDetectionSwitch.value = systemEdgeDetectionSwitch.value.not()
                },
                onUserEdgeDetectionClicked = {
                    when (userEdgeDetectionSwitch.value) {
                        true -> {
                            cameraViewModel.controlFixedScreen(false)
                            userEdgeDetectionSwitch.value = false
                        }

                        else -> {
                            getImageForEdgeLauncher.launch()
                        }
                    }
                },
                //썸네일 이미지 설정
                //포즈 추천버튼 눌렀을 때
                onPoseRecommendEvent = {
                    if (currentPoseDataList == null)
                        cameraViewModel.reqPoseRecommend()
                    showPoseListUnderBarState.value = true
                },
                lowerLayerPaddingBottom = 0.dp,
                galleryImageUri = galleryImageUri,
                userEdgeDetectionValue = userEdgeDetectionSwitch.value,
                systemEdgeDetectionValue = systemEdgeDetectionSwitch.value


            )
        }

        AnimatedSlideToLeft(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        lowerBarSize.value.let { lowSize ->
                            if (lowSize.width == 0.dp && lowSize.height == 0.dp)
                                lowerBarSize.value = coordinates.size.let {
                                    DpSize(it.width.toDp(), it.height.toDp())
                                }
                        }
                    }
                },
            isVisible = showPoseListUnderBarState.value
        ) {
            //여기에 포즈 리스트를 담은 하단 버튼 배열을 띄워준다.
            ClickPoseBtnUnderBar(
                poseList = currentPoseDataList,
                onRefreshPoseData = {
                    CoroutineScope(Dispatchers.Main).launch {
                        cameraViewModel.reqPoseRecommend()
                        cameraViewModel.poseResultState.collectLatest {
                            if (it == null) selectedPoseIndex.intValue = 1
                        }
                    }
                },
                initScale = poseScale.floatValue,
                onClickShutterBtn = shutterEvent,
                onSelectedPoseIndexEvent = {
                    selectedPoseIndex.intValue = it
                    poseScale.floatValue = 1F //여기서 현재 포즈에 대한 스케일 값을 조정해주면 된다.
                },
                currentSelectedIdx = selectedPoseIndex.intValue,
                onClickCloseBtn = {
                    showPoseListUnderBarState.value = showPoseListUnderBarState.value.not()
                },
                onGalleryButtonClickEvent = openGalleryEvent,
                galleryImageUri = galleryImageUri,
                onChangeScale = {
                    poseScale.floatValue = it
                }
            )
        }
    }

}









