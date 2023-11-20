package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.Settings
import android.util.Range
import android.util.SizeF
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.UIComponents.AnimatedSlideToLeft
import com.hanadulset.pro_poseapp.utils.UserSet
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventLog
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


@SuppressLint("HardwareIds")
@OptIn(ExperimentalComposeUiApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun Screen(
    cameraViewModel: CameraViewModel,
    previewView: () -> PreviewView,
    onClickGalleryBtn: () -> Unit,
    onClickSettingBtnEvent: () -> Unit,
    cameraInit: () -> Unit,
    onFinishEvent: () -> Unit,
    userSet: () -> UserSet
) {
    val nowUserSet = remember {
        mutableStateOf(userSet())
    }
    LaunchedEffect(key1 = userSet()) {
        nowUserSet.value = userSet()
    }
    val userEdgeDetectionSwitch = remember { mutableStateOf(false) }
    val systemEdgeDetectionSwitch = remember { mutableStateOf(false) }
    val isShowPoseItemList = rememberSaveable { mutableStateOf(false) }
    val currentPoseItemDataList = cameraViewModel.poseResultState.collectAsStateWithLifecycle()
    val selectedPoseItemDataIndex = rememberSaveable {
        mutableIntStateOf(1)
    }
    val currentPoseItemScale = rememberSaveable {
        mutableFloatStateOf(
            currentPoseItemDataList.value?.get(selectedPoseItemDataIndex.intValue)?.imageScale ?: 1F
        )
    }
    val currentPoseItemOffset = rememberSaveable {
        mutableStateOf<SizeF?>(null)
    }

    fun afterGetNewPoseDataList() {
        currentPoseItemScale.floatValue = 1F //여기서 현재 포즈에 대한 스케일 값을 조정해주면 된다.
        currentPoseItemOffset.value = null
    }
//    LaunchedEffect(nowUserSet) {
//        if (nowUserSet.value.isPoseOn.not()) {
//            selectedPoseItemDataIndex.intValue = 0
//            currentPoseItemScale.floatValue = 1F
//            isShowPoseItemList.value = false
//        }
//    }

    val context = LocalContext.current

    val closeEdgeScreen = {
        var isClose = false
        //잠금 기능 해제
        if (userEdgeDetectionSwitch.value) {
            cameraViewModel.controlFixedScreen(false)
            userEdgeDetectionSwitch.value = false
            isClose = true
        }
        if (systemEdgeDetectionSwitch.value) {
            cameraViewModel.controlFixedScreen(systemEdgeDetectionSwitch.value.not())
            systemEdgeDetectionSwitch.value = systemEdgeDetectionSwitch.value.not()
            isClose = true
        }
        isClose
    }



    BackHandler(onBack = {
        if (closeEdgeScreen().not()) {
            onFinishEvent()
            //앱 종료 이벤트 발생
            Firebase.analytics.logEvent("EVENT_APP_CLOSE") {
                param("timeStamp", System.currentTimeMillis())
                param("closeWay", "BY_BACK_BUTTON")
                param(
                    "deviceID", Settings.Secure.getString(
                        context.contentResolver, Settings.Secure.ANDROID_ID
                    )
                )
            }
        }
    })
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
                    if (systemEdgeDetectionSwitch.value) {
                        systemEdgeDetectionSwitch.value = false
                    }
                    nowUserSet.value = nowUserSet.value.copy(isCompOn = false)
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


    val localDensity = LocalDensity.current
    val aspectRatio = cameraViewModel.aspectRatioState.collectAsStateWithLifecycle()

    //여기서 결과 값을 전달 받은경우에만 따오기  버튼을 활성화 하도록 해보자
    val getImageForEdgeLauncher = rememberGetContentActivityResult(
        onGetPoseFromImage = { uri -> cameraViewModel.getPoseFromImage(uri) },
        aspectRatio = aspectRatio.value
    )


    val stopTrackingPoint = remember { { cameraViewModel.stopToTrack() } }


    //포즈 추천 결과
    val backgroundAnalysisResult by cameraViewModel.backgroundDataState.collectAsStateWithLifecycle()

    val needToCloseViewRate = remember { mutableStateOf(false) }
    val upperBarSize = remember { mutableStateOf(DpSize.Zero) }
    val touchEventCoroutineScope = rememberCoroutineScope()

    val cameraZoomLevelState = rememberSaveable {
        mutableFloatStateOf(1F)
    }
    val galleryImageUri = cameraViewModel.capturedBitmapState.collectAsStateWithLifecycle()

    val previewAreaSize = remember {
        mutableStateOf(DpSize.Zero)
    }
    val previewViewBottomRightOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    val compPointOffset = cameraViewModel.pointOffsetState.collectAsStateWithLifecycle()
    val edgeImageState = cameraViewModel.fixedScreenState.collectAsStateWithLifecycle()


    val captureBtnClickState = remember { mutableStateOf(false) }

    val maxScale = remember {
        mutableFloatStateOf(
            2F
        )
    }

    val doPoseRecommend = {
        cameraViewModel.reqPoseRecommend()
        afterGetNewPoseDataList()
        selectedPoseItemDataIndex.intValue = 1
    }

    val recommendPoseEvent = remember<(Boolean?) -> Unit> {
        //구도 추천이 완료되면, 포즈 추천을 자동으로 받게 함.
        { triggerByComp ->
            //포즈 추천의 경우는 구도추천에 의한 것 ( 자동추천 켠경우) or 포즈버튼을 누른 경우
            when (triggerByComp) {
                true -> if (nowUserSet.value.isPoseOn) doPoseRecommend()
                false -> doPoseRecommend()
                //자동추천에 고려하지 않아도 됨.
                else -> if (currentPoseItemDataList.value == null) doPoseRecommend()
            }


//                if (nowUserSet.value.isPoseOn) {
//                    if (currentPoseItemDataList.value == null || triggerByComp) {
//                        cameraViewModel.reqPoseRecommend()
//                        afterGetNewPoseDataList()
//                        selectedPoseItemDataIndex.intValue = 1
//                    }
//
//                }
        }
    }
    val localCutOutPadding = WindowInsets.displayCutout.asPaddingValues()


    //셔터 버튼을 눌렀을 때 발생되는 이벤트
    val shutterEvent = remember {
        {
            cameraViewModel.getPhoto(
                CaptureEventLog(
                    poseID = currentPoseItemDataList.value?.get(selectedPoseItemDataIndex.intValue)?.poseId
                        ?: -1,
                    prevRecommendPoses = currentPoseItemDataList.value?.let { it.map { poseData -> poseData.poseId } },
                    timestamp = System.currentTimeMillis().toString(),
                    backgroundId = backgroundAnalysisResult?.first,
                    backgroundHog = backgroundAnalysisResult?.second?.toString()
                )
            )
            stopTrackingPoint() // 구도 추천을 다시 시작함.
            captureBtnClickState.value = true
        }
    }
    val touchEvent: (MotionEvent) -> Boolean = { motionEvent -> //여기서 포커스 링을 세팅하는데, 여기서 문제가 생긴 것 같다.
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                upperBarSize.value.run {
                    localDensity.run {
                        val xRange = Range(0F, width.toPx())
                        val yRange = Range(0F, height.toPx())
                        val isOkayToClose =
                            (motionEvent.x in xRange && motionEvent.y in yRange).not()
                        //화면 변경 창 닫기
                        if (isOkayToClose) touchEventCoroutineScope.launch {
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
    }



    Column(
        Modifier
            .fillMaxSize()
            .background(LocalColors.current.secondaryWhite100)
            .displayCutoutPadding()
    ) {
        //상단 버튼
        CameraScreenUpperBar.UpperBar(modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                if (upperBarSize.value == DpSize.Zero) {
                    coordinates.size.run {
                        upperBarSize.value = with(localDensity) {
                            DpSize(width = width.toDp(), height = height.toDp())
                        }
                    }
                }
            }
            .weight(1F, fill = true),
            viewRateList = cameraViewModel.getViewRateList(),
            moveToInfo = onClickSettingBtnEvent,
            onSelectedViewRate = { idx ->
                // 화면 비율을 조절할 때 발생하는 이벤트
                if (cameraViewModel.changeViewRate(idx = idx).not()) cameraInit()
                stopTrackingPoint()
                closeEdgeScreen()
            },
            needToCloseViewRateList = { needToCloseViewRate.value })
        Box(
            modifier = Modifier
                .weight(11F)
                .then(
                    if (aspectRatio.value.aspectRatioType == AspectRatio.RATIO_16_9) Modifier.background(
                        color = LocalColors.current.subPrimaryBlack100
                    ) else Modifier
                )
                .pointerInteropFilter(
                    RequestDisallowInterceptTouchEvent(), touchEvent
                ), contentAlignment = Alignment.Center
        ) {
            //중간 ( 미리보기, 포즈 추천, 구도 추천 보기 화면 ) 모듈
            CameraScreenPreviewArea.PreviewArea(modifier = Modifier
                .aspectRatio(aspectRatio.value.aspectRatioSize.let { (it.width.toFloat() / it.height.toFloat()) })
                .onGloballyPositioned { coordinates ->
                    coordinates.size.let {
                        previewAreaSize.value = DpSize(it.width.dp, it.height.dp)
                    }
                    previewViewBottomRightOffset.value = coordinates.boundsInRoot().bottomRight
                }
                .align(Alignment.TopCenter),
                initCamera = cameraInit,
                preview = previewView,
                upperBarSize = { upperBarSize.value },
                isRecommendCompEnabled = { nowUserSet.value.isCompOn },
                isRecommendPoseEnabled = { nowUserSet.value.isPoseOn },
                loadLastImage = { cameraViewModel.getLastImage() },
                onFocusEvent = {
                    cameraViewModel.setFocus(it.first, it.second)
                },
                poseData = { currentPoseItemDataList.value?.get(selectedPoseItemDataIndex.intValue) },
                pointerOffsetState = { compPointOffset.value },
                edgeImageBitmap = { edgeImageState.value },
                triggerNewPoint = {
                    cameraViewModel.startToTrack(with(localDensity) {
                        Size(
                            it.width.toPx(), it.height.toPx()
                        )
                    })
                },
                onStopTrackPoint = stopTrackingPoint,
                capturedState = { captureBtnClickState.value },
                onStopCaptureAnimation = {
                    captureBtnClickState.value = false
                },
                poseScaleState = { currentPoseItemScale.floatValue },
                poseOffsetState = { currentPoseItemOffset.value },
                onPoseChangeOffset = { currentPoseItemOffset.value = it },
                onPointMatched = { isOnHorizon ->
                    if (isOnHorizon()) recommendPoseEvent(true)
                },
                onLimitMaxScale = {
                    maxScale.floatValue = it
                })

            val lowerBarSize = remember {
                mutableStateOf(DpSize.Zero)
            }
            val poseLowerBarSize = remember {
                mutableStateOf(DpSize.Zero)
            }


            //하단바
            AnimatedSlideToLeft(modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    localDensity.run {
                        lowerBarSize.value = DpSize(it.width.toDp(), it.height.toDp())
                    }
                }
                .then(if (aspectRatio.value.aspectRatioType == AspectRatio.RATIO_16_9) {
                    Modifier
                        .graphicsLayer {
                            translationX = 0F
                            translationY =
                                previewViewBottomRightOffset.value.y - (lowerBarSize.value.height.toPx() + upperBarSize.value.height.toPx() + localCutOutPadding
                                    .calculateTopPadding()
                                    .toPx())
                        }
                        .align(Alignment.TopCenter)
                } else Modifier.align(Alignment.BottomCenter))
                .padding(bottom = 10.dp),
                isVisible = isShowPoseItemList.value.not()) {
                //하단바 관련 모듈
                CameraScreenUnderBar.UnderBar(
                    //줌레벨 변경 시
                    onZoomLevelChangeEvent = { zoomLevel ->
                        cameraViewModel.setZoomLevel(zoomLevel)
                        cameraZoomLevelState.floatValue = zoomLevel
                    },
                    onGalleryButtonClickEvent = onClickGalleryBtn,
                    //촬영 시에 EventLog를 인자로 넘겨줘야한다.
                    onShutterClickEvent = shutterEvent,
                    //현재 선택된 인덱스에 대해서 전달하기 위함..
                    //어차피 한쪽 (포즈 추천 화면)은 읽기만 하면된다.
                    //고정 버튼 클릭시
                    onSystemEdgeDetectionClicked = {
                        if (userEdgeDetectionSwitch.value) userEdgeDetectionSwitch.value = false
                        cameraViewModel.controlFixedScreen(systemEdgeDetectionSwitch.value.not())
                        systemEdgeDetectionSwitch.value = systemEdgeDetectionSwitch.value.not()
                        nowUserSet.value =
                            nowUserSet.value.copy(isCompOn = systemEdgeDetectionSwitch.value.not())
                    },
                    onUserEdgeDetectionClicked = {
                        when (userEdgeDetectionSwitch.value) {
                            true -> {
                                nowUserSet.value = nowUserSet.value.copy(isCompOn = true)
                                cameraViewModel.controlFixedScreen(false)
                                userEdgeDetectionSwitch.value = false
                                if (systemEdgeDetectionSwitch.value) systemEdgeDetectionSwitch.value =
                                    false
                            }

                            else -> {
                                getImageForEdgeLauncher.launch()
                            }
                        }
                    },
                    //썸네일 이미지 설정
                    //포즈 추천버튼 눌렀을 때
                    onPoseRecommendEvent = {
                        recommendPoseEvent(null)
                        isShowPoseItemList.value = true
                    },
                    lowerLayerPaddingBottom = 0.dp,
                    galleryImageUri = { galleryImageUri.value },
                    userEdgeDetectionValue = { userEdgeDetectionSwitch.value },
                    systemEdgeDetectionValue = { systemEdgeDetectionSwitch.value },
                    zoomLevelState = { cameraZoomLevelState.floatValue },
                    isRecommendPoseEnabled = { nowUserSet.value.isPoseOn }
                )
            }

            AnimatedSlideToLeft(modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    localDensity.run {
                        poseLowerBarSize.value = DpSize(it.width.toDp(), it.height.toDp())
                    }
                }
                .then(if (aspectRatio.value.aspectRatioType == AspectRatio.RATIO_16_9) {
                    Modifier
                        .graphicsLayer {
                            translationX = 0F
                            translationY =
                                previewViewBottomRightOffset.value.y - (poseLowerBarSize.value.height.toPx() + upperBarSize.value.height.toPx() + localCutOutPadding
                                    .calculateTopPadding()
                                    .toPx())
                        }
                        .align(Alignment.TopCenter)

                } else Modifier.align(Alignment.BottomCenter))
                .then(
                    if (aspectRatio.value.aspectRatioType == AspectRatio.RATIO_16_9) Modifier.background(
                        LocalColors.current.subPrimaryBlack100.copy(
                            alpha = 0.5f
                        )
                    ) else Modifier
                )
                .padding(bottom = 10.dp),
                isVisible = isShowPoseItemList.value
            ) {

                val coroutineScope = rememberCoroutineScope()
                //여기에 포즈 리스트를 담은 하단 버튼 배열을 띄워준다.
                ClickPoseBtnUnderBar(
                    poseList = { currentPoseItemDataList.value },
                    onRefreshPoseData = {
                        coroutineScope.launch {
                            recommendPoseEvent(false)
                            cameraViewModel.poseResultState.collectLatest {
                                if (it == null) afterGetNewPoseDataList()
                            }
                        }
                    },
                    initPoseItemScale = { currentPoseItemScale.floatValue },
                    onClickShutterBtn = shutterEvent,
                    onSelectedPoseIndexEvent = {
                        selectedPoseItemDataIndex.intValue = it
                        afterGetNewPoseDataList()
                    },
                    currentSelectedPoseItemIdx = { selectedPoseItemDataIndex.intValue },
                    onClickCloseBtn = {
                        isShowPoseItemList.value = false
                    },
                    onGalleryButtonClickEvent = onClickGalleryBtn,
                    galleryImageUri = { galleryImageUri.value },
                    onChangeScale = { scale ->
                        currentPoseItemScale.floatValue = scale
                        val currentItem =
                            currentPoseItemDataList.value?.get(selectedPoseItemDataIndex.intValue)
                        currentItem?.let { poseData ->
                            currentPoseItemDataList.value?.set(
                                selectedPoseItemDataIndex.intValue, poseData.copy(
                                    imageScale = scale
                                )
                            )
                        }
                    },
                    is16By9AspectRatio = { aspectRatio.value.aspectRatioType == AspectRatio.RATIO_16_9 },
                    maxScale = { maxScale.floatValue }
                )
            }
        }
    }

}










