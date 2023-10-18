package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


//리컴포지션시 데이터가 손실되는 문제를 해결하기 위한, 전역변수


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

    val screenSize =
        LocalConfiguration.current.let { DpSize(it.screenWidthDp.dp, it.screenHeightDp.dp) }


    val openGalleryEvent by rememberUpdatedState(newValue = onClickGalleryBtn)
    val localDensity = LocalDensity.current
    val aspectRatio by cameraViewModel.aspectRatioState.collectAsStateWithLifecycle()
    val cropImageLauncher =
        rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
            if (result.isSuccessful) {
                // Use the returned uri.
                val uriContent = result.uriContent
                cameraViewModel.getPoseFromImage(uriContent)
            } else {
                // An error occurred.
                val exception = result.error
            }
        }
    val stopTrackingPoint = remember { { cameraViewModel.stopToTrack() } }


    //포즈 추천 결과
    val backgroundAnalysisResult by cameraViewModel.backgroundDataState.collectAsStateWithLifecycle()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val cropOptions = CropImageContractOptions(
                    uri, CropImageOptions(
                        aspectRatioX = aspectRatio.aspectRatioSize.width,
                        aspectRatioY = aspectRatio.aspectRatioSize.height,
                        fixAspectRatio = true,
                    )
                )
                cropImageLauncher.launch(cropOptions)
            }

        }
    val needToCloseViewRate = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter(RequestDisallowInterceptTouchEvent()) { motionEvent -> //여기서 포커스 링을 세팅하는데, 여기서 문제가 생긴 것 같다.
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        scope.launch {
                            needToCloseViewRate.value = true
                            delay(10L)
                            needToCloseViewRate.value = false
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
        val upperBarSize = remember { DpSize(screenSize.width, screenSize.height / 9 + 30.dp) }
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
        val getEdgeFromUserImage = remember {
            {
                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
        val captureBtnClickState = remember { mutableStateOf(false) }

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
                .align(Alignment.TopCenter),
            initCamera = cameraInit,
            padding = if (aspectRatio.aspectRatioType == AspectRatio.RATIO_4_3) upperBarSize.height else 0.dp,
            poseList = currentPoseDataList,
            preview = previewView,
            selectedPoseIndex = selectedPoseIndex.intValue,
            upperBarSize = upperBarSize,
            isRecommendCompEnabled = compState.value,
            loadLastImage = { cameraViewModel.getLastImage() },
            onFocusEvent = {
                cameraViewModel.setFocus(it.first, it.second)
            },
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
            }
        )

        //상단 버튼
        CameraScreenUpperBar.UpperBar(
            modifier = Modifier
                .padding(top = 30.dp)
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

        AnimatedVisibility(
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
            visible = showPoseListUnderBarState.value.not(),
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
                initialOffsetX = { fullHeight -> -fullHeight }
            ).plus(fadeIn()),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                targetOffsetX = { fullHeight -> -fullHeight }
            ).plus(fadeOut())
        ) {
            //하단바 관련 모듈
            CameraScreenUnderBar.UnderBar(
                //따오기 관련 처리
                onEdgeDetectEvent = {
                    when (it) {
                        true -> getEdgeFromUserImage()
                        else -> cameraViewModel.controlFixedScreen(false)
                    }
                },
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
                onFixedButtonClickEvent = { isRequest ->
                    cameraViewModel.controlFixedScreen(isRequest)
                },
                //썸네일 이미지 설정
                //포즈 추천버튼 눌렀을 때
                onPoseRecommendEvent = {
                    if (currentPoseDataList == null)
                        cameraViewModel.reqPoseRecommend()
                    showPoseListUnderBarState.value = true
                },
                lowerLayerPaddingBottom = 50.dp,
                galleryImageUri = galleryImageUri
            )
        }
        AnimatedVisibility(
            modifier = Modifier
                .animateContentSize { _, _ -> }
                .sizeIn(lowerBarSize.value.width, lowerBarSize.value.height)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            visible = showPoseListUnderBarState.value,
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
                initialOffsetX = { fullHeight -> fullHeight }
            ).plus(fadeIn()),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                targetOffsetX = { fullHeight -> fullHeight }
            ).plus(fadeOut())
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
                onClickShutterBtn = shutterEvent,
                onSelectedPoseIndexEvent = {
                    selectedPoseIndex.intValue = it
                },
                currentSelectedIdx = selectedPoseIndex.intValue,
                onClickCloseBtn = {
                    showPoseListUnderBarState.value = showPoseListUnderBarState.value.not()
                },
                onGalleryButtonClickEvent = openGalleryEvent,
                galleryImageUri = galleryImageUri
            )
        }
    }


}









