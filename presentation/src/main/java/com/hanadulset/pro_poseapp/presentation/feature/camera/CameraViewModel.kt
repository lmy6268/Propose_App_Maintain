package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.GetPoseFromImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendCompInfoUseCase
import com.hanadulset.pro_poseapp.domain.usecase.ai.RecommendPoseUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.CaptureImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.GetLatestImageUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.SetFocusUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.SetZoomLevelUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.ShowFixedScreenUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.ShowPreviewUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.GetTrackingDataUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StopTrackingDataUseCase
import com.hanadulset.pro_poseapp.domain.usecase.config.WriteUserLogUseCase
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@ExperimentalGetImage
@HiltViewModel
class CameraViewModel @Inject constructor(
    //UseCases
    private val showPreviewUseCase: ShowPreviewUseCase,
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
        Pair(AspectRatio.RATIO_4_3, Size(3, 4)),
        Pair(AspectRatio.RATIO_16_9, Size(9, 16))
    )

    private val _previewState = MutableStateFlow(CameraState(CameraState.CAMERA_INIT_NOTHING))
    private val _trackerSwitchState = MutableStateFlow(false) //트래커를 켜고 끄는 스위치
    private val _trackerDataState = MutableStateFlow<Offset?>(null)
    private var _trackerOffset: Offset? = null
    private var _trackerRadius = 30


    private val _viewRateIdxState = MutableStateFlow(0)
    private val _viewRateState = MutableStateFlow(viewRateList[0].first)
    private val _aspectRatioState = MutableStateFlow(viewRateList[0].second)


    private val _capturedBitmapState = MutableStateFlow<Uri?>( //캡쳐된 이미지 상태
        null
    )
    private val _poseResultState = MutableStateFlow<Pair<DoubleArray?, List<PoseData>?>?>(null)
    private val _compResultState = MutableStateFlow<Pair<String, Int>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)
    private var _previewSize: Size? = null
    private var _analyzeSize: Size? = null


    //State Getter

    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()
    val compResultState = _compResultState.asStateFlow()

    val viewRateIdxState = _viewRateIdxState.asStateFlow()
    val aspectRatioState = _aspectRatioState.asStateFlow()
    val viewRateState = _viewRateState.asStateFlow()
    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()
    val trackerDataState =
        _trackerDataState.asStateFlow()


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        imageProxy.use { image ->
            if (reqFixState.value) {
                reqFixState.value = false
                val res = showFixedScreenUseCase(image)
                _fixedScreenState.value = res
            }
            if (_trackerSwitchState.value) {
                viewModelScope.launch {
                    val resFromTracker = getTrackingDataUseCase(
                        inputFrame = image,
                        inputOffset = Pair(_trackerOffset!!.x, _trackerOffset!!.y),
                        radius = _trackerRadius
                    ).let {
                        Offset(it.first, it.second)
                    }
                    //현재 미리보기에 맞는 좌표로 변환
                    val totalResData = resFromTracker.let {
                        Offset(
                            round((it.x / _analyzeSize!!.height) * _previewSize!!.width),
                            round((it.y / _analyzeSize!!.width) * _previewSize!!.height)
                        )
                    }
                    _trackerDataState.value = totalResData

                    if (resFromTracker.x in 0F.._analyzeSize!!.height.toFloat() && resFromTracker.y in 0F.._analyzeSize!!.width.toFloat()) {
                        Log.d(
                            "analyze Point: ",
                            "trackerPoint - (${resFromTracker.x},${resFromTracker.y}) / imageAnalyzeSize : ${_analyzeSize!!.height} X ${_analyzeSize!!.width}"
                        )
                        Log.d(
                            "trackerPoint Location: ",
                            "trackerPoint - (${_trackerDataState.value!!.x},${_trackerDataState.value!!.y}) / previewSize : ${_previewSize!!.width} X ${_previewSize!!.height}"
                        )
                    }

                }
            }

            //포즈 선정 로직
            if (reqPoseState.value) {
                reqPoseState.value = false
                _poseResultState.value = null
                viewModelScope.launch {
                    _poseResultState.value = recommendPoseUseCase(
                        image = image.image!!,
                        rotation = image.imageInfo.rotationDegrees
                    ).let { poseDatas ->
                        poseDatas.copy(
                            second = listOf(
                                PoseData(-1, -1, -1), //해제데이터를 넣기 위함.
                                *(poseDatas.second).toTypedArray()
                            )
                        )
                    }
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

    fun attachTracker(offsetInPreview: Offset, previewSize: Size) {
        val offsetRate =
            Pair(offsetInPreview.x / previewSize.width, offsetInPreview.y / previewSize.height)
        val trackerOffset = Offset(
            _analyzeSize!!.height * offsetRate.first, _analyzeSize!!.width * offsetRate.second
        )
        _trackerOffset = trackerOffset
        _previewSize = previewSize
        _trackerSwitchState.value = true //
    }

    fun detachTracker() {
        _trackerOffset = null
        _trackerSwitchState.value = false
        stopTrackingDataUseCase()
    }


    fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        aspectRatio: Int,
        previewRotation: Int
    ) {
        _previewState.value =
            CameraState(cameraStateId = CameraState.CAMERA_INIT_ON_PROCESS) // OnProgress
        viewModelScope.launch {
            _previewState.value =
                showPreviewUseCase(
                    lifecycleOwner,
                    surfaceProvider,
                    aspectRatio = aspectRatio,
                    analyzer = imageAnalyzer,
                    previewRotation = previewRotation
                )
            _analyzeSize = _previewState.value.imageAnalyzerResolution
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

    fun reqPoseRecommend() {
        if (reqPoseState.value.not()) reqPoseState.value = true
    }


    fun reqCompRecommend() {
        if (reqCompState.value.not()) reqCompState.value = true
    }


    fun setZoomLevel(zoomLevel: Float) = setZoomLevelUseCase(zoomLevel)

    fun changeViewRate(idx: Int) {
        _viewRateIdxState.value = idx
        _viewRateState.value = viewRateList[idx].first
        _aspectRatioState.value = viewRateList[idx].second
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
