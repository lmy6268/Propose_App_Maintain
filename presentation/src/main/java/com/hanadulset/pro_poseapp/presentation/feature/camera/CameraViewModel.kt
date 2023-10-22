package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.core.net.toFile
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
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.StopPointOffsetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.camera.tracking.UpdatePointOffsetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.config.WriteUserLogUseCase
import com.hanadulset.pro_poseapp.utils.ImageUtils
import com.hanadulset.pro_poseapp.utils.camera.CameraState
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.io.File
import javax.inject.Inject

@ExperimentalGetImage
@HiltViewModel
class CameraViewModel @Inject constructor(
    //UseCases
    private val bindCameraUseCase: BindCameraUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
    private val setZoomLevelUseCase: SetZoomLevelUseCase,
    private val recommendCompInfoUseCase: RecommendCompInfoUseCase,
    private val recommendPoseUseCase: RecommendPoseUseCase,
    private val getLatestImageUseCase: GetLatestImageUseCase,
    private val getPoseFromImageUseCase: GetPoseFromImageUseCase,
    private val setFocusUseCase: SetFocusUseCase,
    private val writeUserLogUseCase: WriteUserLogUseCase,
    private val updatePointOffsetUseCase: UpdatePointOffsetUseCase,
    private val stopPointOffsetUseCase: StopPointOffsetUseCase

) : ViewModel() {

    private val _trackingSwitchON = MutableStateFlow(false)

    private val viewRateList = listOf(
        ViewRate(
            name = "3:4", aspectRatioType = AspectRatio.RATIO_4_3, aspectRatioSize = Size(3, 4)
        ), ViewRate(
            "9:16", aspectRatioType = AspectRatio.RATIO_16_9, aspectRatioSize = Size(9, 16)
        )
    )


    private val _backgroundDataState = MutableStateFlow<Pair<Int, List<Double>>?>(null)
    val backgroundDataState = _backgroundDataState.asStateFlow()

    private val _aspectRatioState = MutableStateFlow(viewRateList[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()


    private val _previewState = MutableStateFlow(CameraState(CameraState.CAMERA_INIT_NOTHING))


    private val _capturedBitmapState = MutableStateFlow<Uri?>( //캡쳐된 이미지 상태
        null
    )
    private val _poseResultState = MutableStateFlow<List<PoseData>?>(null)
    private val _fixedScreenState = MutableStateFlow<Bitmap?>(null)
    private val _modifiedPointState = MutableStateFlow<Offset?>(null)

    val pointOffsetState = _modifiedPointState.asStateFlow()


    //State Getter

    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()


    val fixedScreenState = _fixedScreenState.asStateFlow()
    val previewState = _previewState.asStateFlow()
    private val _bitmapState = MutableStateFlow<Bitmap?>(null)
    private val _bitmapDemandNow = MutableStateFlow(false)


    private var previewSizeState: androidx.compose.ui.geometry.Size? = null
    private val _poseOnRecommend = MutableStateFlow(false)


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->

        _bitmapState.onSubscription {
            Log.d("현재 인식됨: ", "네")
        }


        imageProxy.use {
            _bitmapState.value = ImageUtils.imageToBitmap(it.image!!, it.imageInfo.rotationDegrees)
            trackToNewOffset()
        }
    }


    private fun convertAnalyzedOffsetToPreviewOffset(
        reversed: Boolean,
        offset: SizeF,
        analyzedImageSize: Size
    ): SizeF {
        return if (reversed) //preview -> analyzed
            offset.let {
                SizeF(
                    (it.width / previewSizeState!!.width) * analyzedImageSize.width,
                    (it.height / previewSizeState!!.height) * analyzedImageSize.height
                )
            } else offset.let {// analyzed -> preview
            SizeF(
                (it.width / analyzedImageSize.width) * previewSizeState!!.width,
                (it.height / analyzedImageSize.height) * previewSizeState!!.height
            )
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
            _previewState.value = bindCameraUseCase(
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
        if (_poseOnRecommend.value.not()) {
            _poseOnRecommend.value = true //포즈 추천이 시작됨을 알림
            _poseResultState.value = null
            if (_bitmapDemandNow.value.not()) _bitmapDemandNow.value = true
            viewModelScope.launch {
                _bitmapState.value?.let { bitmap ->
                    val recommendedData = recommendPoseUseCase(bitmap)
                    _poseResultState.value =
                        recommendedData.poseDataList.apply {
                            add(0, PoseData(poseId = -1, -1))
                        }
                    _backgroundDataState.value =
                        recommendedData.let { Pair(it.backgroundId, it.backgroundAngleList) }
                    _poseOnRecommend.value = false//포즈 추천이 끝남을 알림
                }
            }
        }
    }


    private fun trackToNewOffset() {
        //구도 추천 로직
        if (_trackingSwitchON.value && _modifiedPointState.value != null) {
            val backgroundBitmap = _bitmapState.value!!
            //이미지 사용하기
            val analyzedImageSize = Size(backgroundBitmap.width, backgroundBitmap.height)
            viewModelScope.launch {
                val res = updatePointOffsetUseCase(
                    targetOffset = convertAnalyzedOffsetToPreviewOffset(
                        reversed = true,
                        offset = SizeF(
                            _modifiedPointState.value!!.x,
                            _modifiedPointState.value!!.y
                        ),
                        analyzedImageSize = analyzedImageSize
                    ),
                    backgroundBitmap = backgroundBitmap
                )
                if (res == null) {
                    stopToTrack() //만약 에러인 경우 추적을 그만함.
                } else {
                    _modifiedPointState.value = convertAnalyzedOffsetToPreviewOffset(
                        false,
                        res,
                        analyzedImageSize = analyzedImageSize
                    ).let { Offset(it.width, it.height) }
                }
            }
        }
    }


    fun startToTrack(previewSize: androidx.compose.ui.geometry.Size) {
        previewSizeState = previewSize
        _trackingSwitchON.value = true
        viewModelScope.launch {
            _bitmapState.value?.let { bitmap ->
                recommendCompInfoUseCase(bitmap).let { res ->
                    _modifiedPointState.value = previewSizeState!!.center.let {
                        Offset(
                            it.x * (1f + if (res.first == "horizon") res.second / 100f else 0f),
                            it.y * (1f + if (res.first == "vertical") res.second / 100f else 0f)
                        )
                    }

                }
            }
        }
    }

    fun stopToTrack() {
        _trackingSwitchON.value = false
        _modifiedPointState.value = null
        stopPointOffsetUseCase()
    }

    fun setZoomLevel(zoomLevel: Float) = setZoomLevelUseCase(zoomLevel)

    fun changeViewRate(idx: Int): Boolean {
        val res =
            _aspectRatioState.value.aspectRatioType == viewRateList[idx].aspectRatioType
        if (res.not()) _aspectRatioState.value = viewRateList[idx]
        return res

    }

    fun controlFixedScreen(isRequest: Boolean) {
        if (isRequest) {
            viewModelScope.launch {
                _bitmapState.value?.let { backgroundBitmap ->
                    _fixedScreenState.value = showFixedScreenUseCase(
                        backgroundBitmap = backgroundBitmap
                    )
                }
            }
        } else _fixedScreenState.value = null

    }

    fun getPoseFromImage(uri: Uri?) {
        _fixedScreenState.value = null
        val res = getPoseFromImageUseCase(uri)
        _fixedScreenState.value = res
        viewModelScope.launch {
            File(uri.toString()).delete()
        }

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
