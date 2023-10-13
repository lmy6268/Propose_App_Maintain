package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions


//리컴포지션시 데이터가 손실되는 문제를 해결하기 위한, 전역변수
var savedData = 0
var saveRecentlyPoses = arrayListOf<Int>()

@OptIn(ExperimentalComposeUiApi::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun Screen(
    cameraViewModel: CameraViewModel,
    previewView: PreviewView,
    onClickGalleryBtn: () -> Unit,
    onClickSettingBtnEvent: () -> Unit,
    cameraInit: () -> Unit
) {
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val galleryImageUri by cameraViewModel.capturedBitmapState.collectAsStateWithLifecycle()
        val upperBarSize = remember { mutableStateOf<DpSize>(DpSize(0.dp, 0.dp)) }
        val previewAreaSize = remember {
            mutableStateOf(DpSize(0.dp, 0.dp))
        }
        val edgeImageState by cameraViewModel.fixedScreenState.collectAsStateWithLifecycle()

        val showPoseListUnderBarState = rememberSaveable { mutableStateOf(false) }


        val lowerBarSize = remember {
            mutableStateOf(DpSize(0.dp, 0.dp))
        }
        val needToCloseViewRate = remember {
            mutableStateOf(false)
        }


        val compStateInit = false
        val compState = rememberSaveable { mutableStateOf(compStateInit) }
        //햔재 전달된 포즈 데이터
        val currentPoseDataList by cameraViewModel.poseResultState.collectAsStateWithLifecycle()
        val selectedPoseIndex = remember {
            mutableIntStateOf(0)
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


        val shutterEvent = remember {
            {
//                cameraViewModel.getPhoto(
//                    EventLog(
//
//                    )
//                )
            }
        }

        //이벤트 로그를 위한 이전 결과값들을 누적하는 변수
        val recordForEventLog = remember {
            mutableStateOf(null)
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
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .shadow(elevation = 2.dp, shape = RectangleShape),
            padding = if (aspectRatio.aspectRatioType == AspectRatio.RATIO_4_3) upperBarSize.value.height else 0.dp,
            poseList = currentPoseDataList,
            preview = previewView,
            selectedPoseIndex = selectedPoseIndex.intValue,
            upperBarSize = upperBarSize.value,
            isRecommendCompEnabled = compState.value,
            loadLastImage = { cameraViewModel.getLastImage() },
            onFocusEvent = {
                cameraViewModel.setFocus(it.first, it.second)
            },
            pointerOffset = null,
            edgeImageBitmap = edgeImageState,
            initCamera = cameraInit
        )

        //상단 버튼
        CameraScreenUpperBar.UpperBar(
            modifier = Modifier
                .heightIn(130.dp)
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        upperBarSize.value.let { upSize ->
                            if (upSize.width == 0.dp && upSize.height == 0.dp)
                                upperBarSize.value = coordinates.size.let {
                                    DpSize(it.width.toDp(), it.height.toDp())
                                }
                        }
                    }
                }
                .zIndex(2F)
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color(0x50FFFFFF)),


            viewRateList = cameraViewModel.getViewRateList(),
            onChangeCompSetEvent = {
                compState.value = it
            },
            moveToInfo = onClickSettingBtnEvent,
            onSelectedViewRate = { idx ->
                if (cameraViewModel.changeViewRate(idx = idx).not()) cameraInit()
            },
            compStateInit = compStateInit,
            needToCloseViewRateList = needToCloseViewRate.value
        )

        if (showPoseListUnderBarState.value.not())
        //하단바 관련 모듈
            CameraScreenUnderBar.UnderBar(
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
                onGalleryButtonClickEvent = onClickGalleryBtn,
                //촬영 시에 EventLog를 인자로 넘겨줘야한다.
                onShutterClickEvent = shutterEvent,
                //현재 선택된 인덱스에 대해서 전달하기 위함..
                //어차피 한쪽 (포즈 추천 화면)은 읽기만 하면된다.
                onSelectedPoseIndexEvent = { index ->
                    if (currentPoseDataList != null && currentPoseDataList!!.isNotEmpty()) selectedPoseIndex.intValue =
                        index
                },
                //고정 버튼 클릭시
                onFixedButtonClickEvent = { isRequest ->
                    cameraViewModel.controlFixedScreen(isRequest)
                },
                //썸네일 이미지 설정
                galleryImageUri = galleryImageUri,
                //포즈 추천버튼 눌렀을 때
                onPoseRecommendEvent = {
                    if (currentPoseDataList == null)
                        cameraViewModel.reqPoseRecommend()

                    showPoseListUnderBarState.value = true
                },
                lowerLayerPaddingBottom = 50.dp,
                aboveSize = previewAreaSize.value.height + upperBarSize.value.height
            )
        else {
            //여기에 포즈 리스트를 담은 하단 버튼 배열을 띄워준다.
            ClickPoseBtnUnderBar(
                modifier = Modifier
                    .animateContentSize { _, _ -> }
                    .sizeIn(lowerBarSize.value.width, lowerBarSize.value.height)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
                poseList = currentPoseDataList,
                onRefreshPoseData = {
                    cameraViewModel.reqPoseRecommend()
                    selectedPoseIndex.intValue = 0
                },
                onClickShutterBtn = shutterEvent,
                onSelectedPoseIndexEvent = {
                    selectedPoseIndex.intValue = it
                },
                onClickCloseBtn = {
                    showPoseListUnderBarState.value = showPoseListUnderBarState.value.not()
                }
            )

        }

    }


}









