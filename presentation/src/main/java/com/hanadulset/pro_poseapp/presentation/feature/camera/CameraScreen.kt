package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
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


@SuppressLint("HardwareIds")
@OptIn(ExperimentalComposeUiApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun Screen(
    cameraViewModel: CameraViewModel,
    previewView: PreviewView,
    onClickGalleryBtn: () -> Unit,
    onClickSettingBtnEvent: () -> Unit,
    cameraInit: () -> Unit,
    onFinishEvent: () -> Unit,
    userSet: UserSet
) {
    val userEdgeDetectionSwitch = remember { mutableStateOf(false) }
    val systemEdgeDetectionSwitch = remember { mutableStateOf(false) }
    val firebaseAnalytics = Firebase.analytics
    val context = LocalContext.current



    BackHandler(onBack = {
        onFinishEvent()
        //앱 종료 이벤트 발생
        firebaseAnalytics.logEvent("EVENT_APP_CLOSE") {
            param("timeStamp", System.currentTimeMillis())
            param("closeWay", "BY_BACK_BUTTON")
            param(
                "deviceID",
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            )
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


    val upperBarSize = remember { mutableStateOf(DpSize.Zero) }
    val scope = rememberCoroutineScope()

    val cameraZoomLevelState = rememberSaveable {
        mutableFloatStateOf(1F)
    }
    val galleryImageUri by cameraViewModel.capturedBitmapState.collectAsStateWithLifecycle()

    val previewAreaSize = remember {
        mutableStateOf(DpSize.Zero)
    }
    val previewViewBottomRightOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    val compPointOffset by cameraViewModel.pointOffsetState.collectAsStateWithLifecycle()
    val edgeImageState by cameraViewModel.fixedScreenState.collectAsStateWithLifecycle()

    val showPoseListUnderBarState = rememberSaveable { mutableStateOf(false) }


    val compStateInit = userSet.isCompOn
    //햔재 전달된 포즈 데이터
    val currentPoseDataList by cameraViewModel.poseResultState.collectAsStateWithLifecycle()
    val selectedPoseIndex = rememberSaveable {
        mutableIntStateOf(1)
    }
    val captureBtnClickState = remember { mutableStateOf(false) }
    val poseScale = rememberSaveable {
        mutableFloatStateOf(
            currentPoseDataList?.get(selectedPoseIndex.intValue)?.imageScale ?: 1F
        )
    }
    val maxScale = remember {
        mutableFloatStateOf(
            2F
        )
    }

    val currentOffset = rememberSaveable {
        mutableStateOf<SizeF?>(null)
    }

    val recommendPoseEvent = remember<(Boolean) -> Unit> {
        //구도 추천이 완료되면, 포즈 추천을 자동으로 받게 함.
        { triggerByComp ->
            if (currentPoseDataList == null || triggerByComp) {
                cameraViewModel.reqPoseRecommend()
                poseScale.floatValue = 1F
                selectedPoseIndex.intValue = 1
                currentOffset.value = null
            }
            if (showPoseListUnderBarState.value.not()) {
                showPoseListUnderBarState.value = triggerByComp.not()
            }
        }
    }


    //셔터 버튼을 눌렀을 때 발생되는 이벤트
    val shutterEvent = remember {
        {
            cameraViewModel.getPhoto()
            val event = EventLog(
                eventId = EventLog.EVENT_CAPTURE,
                poseID = currentPoseDataList?.get(selectedPoseIndex.intValue)?.poseId ?: -1,
                prevRecommendPoses = currentPoseDataList?.let { it.map { poseData -> poseData.poseId } },
                timestamp = System.currentTimeMillis().toString(),
                backgroundId = backgroundAnalysisResult?.first,
                backgroundHog = backgroundAnalysisResult?.second?.toString()
            )


            firebaseAnalytics.logEvent("EVENT_CAPTURE") {
                val hog = event.backgroundHog.toString()
                val prev = event.prevRecommendPoses.toString()
                param(
                    "poseID",
                    event.poseID.toDouble()
                )

                param(
                    "timeStamp",
                    event.timestamp
                )
                param(
                    "backgroundId",
                    event.backgroundId?.toDouble() ?: -1.0
                )
                if (prev.length > 99) {
                    prev.chunked(99).forEachIndexed { index, s ->
                        param(
                            "prevRecommendPoses_$index", s
                        )
                    }
                } else {
                    param(
                        "prevRecommendPoses",
                        prev
                    )
                }
                if (hog.length > 99) {
                    hog.chunked(99).forEachIndexed { index, s ->
                        param(
                            "backgroundHog_$index", s
                        )
                    }
                } else {
                    param(
                        "backgroundHog",
                        hog
                    )
                }

            }
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
            onChangeCompSetEvent = {
                if (it.not()) stopTrackingPoint()
            },
            moveToInfo = onClickSettingBtnEvent,
            onSelectedViewRate = { idx ->
                // 화면 비율을 조절할 때 발생하는 이벤트
                if (cameraViewModel.changeViewRate(idx = idx).not()) cameraInit()
                stopTrackingPoint()

                //잠금 기능 해제
                if (userEdgeDetectionSwitch.value) {
                    cameraViewModel.controlFixedScreen(false)
                    userEdgeDetectionSwitch.value = false
                }
                if (systemEdgeDetectionSwitch.value) {
                    cameraViewModel.controlFixedScreen(systemEdgeDetectionSwitch.value.not())
                    systemEdgeDetectionSwitch.value = systemEdgeDetectionSwitch.value.not()
                }
            },
            compStateInit = compStateInit,
            needToCloseViewRateList = needToCloseViewRate.value
        )
        Box(
            modifier = Modifier
                .weight(10F)
                .then(
                    if (aspectRatio.aspectRatioType == AspectRatio.RATIO_16_9) Modifier.background(
                        color = LocalColors.current.subPrimaryBlack100
                    ) else Modifier
                )
                .pointerInteropFilter(
                    RequestDisallowInterceptTouchEvent(),
                    touchEvent
                ),
            contentAlignment = Alignment.Center
        ) {
            //중간 ( 미리보기, 포즈 추천, 구도 추천 보기 화면 ) 모듈
            CameraScreenPreviewArea.PreviewArea(modifier = Modifier
                .aspectRatio(aspectRatio.aspectRatioSize.let { (it.width.toFloat() / it.height.toFloat()) })
                .onGloballyPositioned { coordinates ->
                    coordinates.size.let {
                        previewAreaSize.value = DpSize(it.width.dp, it.height.dp)
                    }
                    previewViewBottomRightOffset.value = coordinates.boundsInRoot().bottomRight
                }
                .align(Alignment.TopCenter),
                initCamera = cameraInit,
                preview = previewView,
                upperBarSize = upperBarSize.value,
                isRecommendCompEnabled = compStateInit,
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
                            it.width.toPx(), it.height.toPx()
                        )
                    })
                },
                onStopTrackPoint = stopTrackingPoint,
                isCaptured = captureBtnClickState.value,
                onStopCaptureAnimation = {
                    captureBtnClickState.value = false
                },
                poseScale = poseScale.floatValue,
                poseOffset = currentOffset.value,
                onPoseChangeOffset = {
                    currentOffset.value = it
                },
                onPointMatched = { recommendPoseEvent(true) },
                onLimitMaxScale = {
                    maxScale.floatValue = it
                }
            )

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
                .then(
                    if (aspectRatio.aspectRatioType == AspectRatio.RATIO_16_9) {
                        Modifier
                            .graphicsLayer {
                                translationX = 0F
                                translationY =
                                    previewViewBottomRightOffset.value.y - (lowerBarSize.value.height.toPx() + upperBarSize.value.height.toPx())
                            }
                            .align(Alignment.TopCenter)
                    } else Modifier.align(Alignment.BottomCenter)
                )
                .padding(bottom = 5.dp),
                isVisible = showPoseListUnderBarState.value.not()
            ) {
                //하단바 관련 모듈
                CameraScreenUnderBar.UnderBar(
                    //줌레벨 변경 시
                    onZoomLevelChangeEvent = { zoomLevel ->
                        cameraViewModel.setZoomLevel(zoomLevel)
                        cameraZoomLevelState.floatValue = zoomLevel
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
                    onPoseRecommendEvent = { recommendPoseEvent(false) },
                    lowerLayerPaddingBottom = 0.dp,
                    galleryImageUri = galleryImageUri,
                    userEdgeDetectionValue = userEdgeDetectionSwitch.value,
                    systemEdgeDetectionValue = systemEdgeDetectionSwitch.value,
                    zoomLevelState = cameraZoomLevelState.floatValue
                )
            }

            AnimatedSlideToLeft(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        localDensity.run {
                            poseLowerBarSize.value = DpSize(it.width.toDp(), it.height.toDp())
                        }
                    }
                    .then(
                        if (aspectRatio.aspectRatioType == AspectRatio.RATIO_16_9) {
                            Modifier
                                .graphicsLayer {
                                    translationX = 0F
                                    translationY =
                                        previewViewBottomRightOffset.value.y - (poseLowerBarSize.value.height.toPx() + upperBarSize.value.height.toPx())
                                }
                                .align(Alignment.TopCenter)

                        } else Modifier.align(Alignment.BottomCenter)
                    )
                    .then(
                        if (aspectRatio.aspectRatioType == AspectRatio.RATIO_16_9) Modifier.background(
                            LocalColors.current.subPrimaryBlack100.copy(
                                alpha = 0.5f
                            )
                        ) else Modifier
                    )
                    .padding(bottom = 5.dp),

                isVisible = showPoseListUnderBarState.value
            ) {
                //여기에 포즈 리스트를 담은 하단 버튼 배열을 띄워준다.
                ClickPoseBtnUnderBar(
                    poseList = currentPoseDataList,
                    onRefreshPoseData = {
                        CoroutineScope(Dispatchers.Main).launch {
                            cameraViewModel.reqPoseRecommend()
                            //
                            cameraViewModel.poseResultState.collectLatest {
                                if (it == null) {
                                    selectedPoseIndex.intValue = 1
                                    poseScale.floatValue = 1F
                                }
                            }
                        }
                    },
                    initPoseItemScale = poseScale.floatValue,
                    onClickShutterBtn = shutterEvent,
                    onSelectedPoseIndexEvent = {
                        selectedPoseIndex.intValue = it
                        poseScale.floatValue = 1F //여기서 현재 포즈에 대한 스케일 값을 조정해주면 된다.
                        currentOffset.value = null
                    },
                    currentSelectedPoseItemIdx = selectedPoseIndex.intValue,
                    onClickCloseBtn = {
                        showPoseListUnderBarState.value = showPoseListUnderBarState.value.not()
                    },
                    onGalleryButtonClickEvent = openGalleryEvent,
                    galleryImageUri = galleryImageUri,
                    onChangeScale = {
                        poseScale.floatValue = it
                        currentPoseDataList?.get(selectedPoseIndex.intValue)?.imageScale =
                            it //변경된 값을 지정
                    },
                    is16By9AspectRatio = aspectRatio.aspectRatioType == AspectRatio.RATIO_16_9,
                    maxScale = maxScale.floatValue
                )
            }
        }
    }


}










