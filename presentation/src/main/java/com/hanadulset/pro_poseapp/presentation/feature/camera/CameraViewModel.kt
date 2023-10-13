package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.GetPoseFromImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendCompInfoUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendPoseUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.BindCameraUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.CaptureImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.GetLatestImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.SetFocusUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.SetZoomLevelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.ShowFixedScreenUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.UnbindCameraUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.GetTrackingDataUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StopTrackingDataUseCase
import com.hanadulset.pro_poseapp.domain.usecase.config.WriteUserLogUseCase
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalGetImage
@HiltViewModel
class CameraViewModel @Inject constructor(
    //UseCases
    private val bindCameraUseCase: BindCameraUseCase,
    private val unbindCameraUseCase: UnbindCameraUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
    private val setZoomLevelUseCase: SetZoomLevelUseCase,
    private val recommendCompInfoUseCase: RecommendCompInfoUseCase,
    private val recommendPoseUseCase: RecommendPoseUseCase,
    private val getLatestImageUseCase: GetLatestImageUseCase,
    private val getPoseFromImageUseCase: GetPoseFromImageUseCase,
    private val setFocusUseCase: SetFocusUseCase,
    private val writeUserLogUseCase: WriteUserLogUseCase,
    private val getTrackingDataUseCase: GetTrackingDataUseCase,
    private val stopTrackingDataUseCase: StopTrackingDataUseCase

) : ViewModel() {

    private val reqFixState = MutableStateFlow(false)// 포즈 추천 요청 on/off
    private val reqPoseState = MutableStateFlow(false)// 포즈 추천 요청 on/off
    private val reqCompState = MutableStateFlow(false) // 구도 추천 요청 on/off

    private val viewRateList = listOf(
        ViewRate(
            name = "3:4",
            aspectRatioType = AspectRatio.RATIO_4_3,
            aspectRatioSize = Size(3, 4)
        ), ViewRate(
            "9:16",
            aspectRatioType = AspectRatio.RATIO_16_9,
            aspectRatioSize = Size(9, 16)
        )
    )


    private val _aspectRatioState = MutableStateFlow(viewRateList[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()


    private val _previewState = MutableStateFlow(CameraState(CameraState.CAMERA_INIT_NOTHING))


    private val _capturedBitmapState = MutableStateFlow<Uri?>( //캡쳐된 이미지 상태
        null
    )
    private val _poseResultState = MutableStateFlow<List<PoseData>?>(null)
    private val _compResultState = MutableStateFlow<Pair<String, Int>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)


    //State Getter

    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()
    val compResultState = _compResultState.asStateFlow()


    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        imageProxy.use { image ->
            if (reqFixState.value) {
                reqFixState.value = false
                val res = showFixedScreenUseCase(image)
                _fixedScreenState.value = res
            }

            //포즈 선정 로직
            if (reqPoseState.value) {
                reqPoseState.value = false
                _poseResultState.value = null
                viewModelScope.launch {
                    _poseResultState.value = recommendPoseUseCase(
                        image = image.image!!,
                        rotation = image.imageInfo.rotationDegrees
                    ).toMutableList().apply {
                        add(0, PoseData(-1, -1, -1))
                    }.toList()
                }
            }
            //구도 추천 로직
            if (reqCompState.value) {
                reqCompState.value = false
                viewModelScope.launch {
                    _compResultState.value = Pair("", Int.MIN_VALUE)
                    _compResultState.value = recommendCompInfoUseCase(
                        image = image.image!!,
                        rotation = image.imageInfo.rotationDegrees
                    )
                }
            }
        }
    }

    fun bindCameraToLifeCycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        previewRotation: Int
    ) {
        _previewState.value =
            CameraState(cameraStateId = CameraState.CAMERA_INIT_ON_PROCESS) // OnProgress
        viewModelScope.launch {
            _previewState.value =
                bindCameraUseCase(
                    lifecycleOwner,
                    surfaceProvider,
                    aspectRatio = aspectRatioState.value.aspectRatioType,
                    analyzer = imageAnalyzer,
                    previewRotation = previewRotation
                )
        }
    }

    fun getPhoto(eventLog: EventLog) {
        viewModelScope.launch {
            _capturedBitmapState.value = captureImageUseCase()
        }
        CoroutineScope(Dispatchers.IO).launch {
            writeUserLogUseCase(eventLog)
        }
    }

    fun getViewRateList() = viewRateList

    fun reqPoseRecommend() {
        if (reqPoseState.value.not()) reqPoseState.value = true
    }


    fun reqCompRecommend() {
        if (reqCompState.value.not()) reqCompState.value = true
    }


    fun setZoomLevel(zoomLevel: Float) = setZoomLevelUseCase(zoomLevel)

    fun changeViewRate(idx: Int): Boolean {
        val res = _aspectRatioState.value.aspectRatioType == viewRateList[idx].aspectRatioType
        if (res.not()) _aspectRatioState.value = viewRateList[idx]
        return res

    }

    fun controlFixedScreen(isRequest: Boolean) {
        reqFixState.value = isRequest
        if (isRequest.not()) _fixedScreenState.value = null
    }

    fun getPoseFromImage(uri: Uri?) {
        _fixedScreenState.value = null
        val res = getPoseFromImageUseCase(uri)
        _fixedScreenState.value = res
    }

    fun unbindCamera() = unbindCameraUseCase()

    //최근 이미지 불러오기
    fun getLastImage() {
        viewModelScope.launch {
            _capturedBitmapState.value = getLatestImageUseCase()
        }
    }

    fun setFocus(meteringPoint: MeteringPoint, durationMilliSeconds: Long) {
        setFocusUseCase(meteringPoint, durationMilliSeconds)
    }


}
