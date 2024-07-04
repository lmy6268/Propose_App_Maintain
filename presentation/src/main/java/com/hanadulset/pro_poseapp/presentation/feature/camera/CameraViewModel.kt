package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanadulset.pro_poseapp.domain.usecase.ai.GetPoseFromImageUseCase
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
import com.hanadulset.pro_poseapp.domain.usecase.gallery.DeleteImageFromPicturesUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.LoadUserSetUseCase
import com.hanadulset.pro_poseapp.domain.usecase.user.SaveUserSetUseCase
import com.hanadulset.pro_poseapp.utils.camera.ViewRate
import com.hanadulset.pro_poseapp.utils.eventlog.CaptureEventData
import com.hanadulset.pro_poseapp.utils.image.imageToBitmap
import com.hanadulset.pro_poseapp.utils.model.camera.PreviewResolutionData
import com.hanadulset.pro_poseapp.utils.model.camera.ProPoseCameraState
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSize
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSizeF
import com.hanadulset.pro_poseapp.utils.model.pose.Pose
import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val updatePointOffsetUseCase: UpdatePointOffsetUseCase,
    private val stopPointOffsetUseCase: StopPointOffsetUseCase,
    private val loadUserSetUseCase: LoadUserSetUseCase,
    private val saveUserSetUseCase: SaveUserSetUseCase,
    private val deleteImageFromPicturesUseCase: DeleteImageFromPicturesUseCase

) : ViewModel() {

    private val _trackingSwitchON = MutableStateFlow(false)

    private val viewRateList = listOf(
        ViewRate(
            name = "4:3", aspectRatioType = AspectRatio.RATIO_4_3, aspectRatioSize = ProPoseSize(3, 4)
        ), ViewRate(
            "16:9", aspectRatioType = AspectRatio.RATIO_16_9, aspectRatioSize = ProPoseSize(9, 16)
        )
    )
    private val _proPoseAppSettingsState = MutableStateFlow<ProPoseAppSettings?>(null)
    val userSetState = _proPoseAppSettingsState.asStateFlow()


    private val _backgroundDataState = MutableStateFlow<Pair<Int, List<Double>>?>(null)
    val backgroundDataState = _backgroundDataState.asStateFlow()

    private val _aspectRatioState = MutableStateFlow(viewRateList[0])
    val aspectRatioState = _aspectRatioState.asStateFlow()


    private val _previewState =
        MutableStateFlow<ProPoseCameraState<PreviewResolutionData>>(ProPoseCameraState.loading())


    private val _capturedBitmapState = MutableStateFlow<Uri?>( //캡쳐된 이미지 상태
        null
    )
    private val _poseResultState = MutableStateFlow<MutableList<Pose>?>(null)
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


    private var previewSizeState:ProPoseSizeF? = null
    private val _poseOnRecommend = MutableStateFlow(false)


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        imageProxy.use {
            _bitmapState.value = imageToBitmap(it.image!!, it.imageInfo.rotationDegrees)
            trackToNewOffset()
        }
    }


    private fun convertAnalyzedOffsetToPreviewOffset(
        reversed: Boolean, offset: ProPoseSizeF, analyzedImageSize: ProPoseSize
    ): ProPoseSizeF {
        return if (reversed) //preview -> analyzed
            offset.let {
                ProPoseSizeF(
                    (it.width / previewSizeState!!.width) * analyzedImageSize.width,
                    (it.height / previewSizeState!!.height) * analyzedImageSize.height
                )
            } else offset.let {// analyzed -> preview
            ProPoseSizeF(
                (it.width / analyzedImageSize.width) * previewSizeState!!.width,
                (it.height / analyzedImageSize.height) * previewSizeState!!.height
            )
        }
    }


    fun bindCameraToLifeCycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: () -> Preview.SurfaceProvider,
        previewRotation: () -> Int
    ) {
        _previewState.value = ProPoseCameraState.loading()
        viewModelScope.launch {
            val res = bindCameraUseCase(
                lifecycleOwner,
                surfaceProvider(),
                aspectRatio = aspectRatioState.value.aspectRatioType,
                analyzer = imageAnalyzer,
                previewRotation = previewRotation()
            )
            _previewState.value = res
        }
    }

    fun getPhoto(captureEventData: CaptureEventData) {
        viewModelScope.launch {
            _capturedBitmapState.value = captureImageUseCase(captureEventData)
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
                    _poseResultState.update {
                        recommendedData.poseList.apply {
                            add(0, Pose(id = -1, -1))
                        }.subList(
                            0,
                            if (_proPoseAppSettingsState.value != null) _proPoseAppSettingsState.value!!.maxRecommendedPoseCnt + 1
                            else recommendedData.poseList.size
                        )
                    }
                    _backgroundDataState.update {
                        recommendedData.let { Pair(it.backgroundId, it.backgroundAngleList) }
                    }

                    _poseOnRecommend.value = false
                    //포즈 추천이 끝남을 알림
                }
            }
        }
    }


    private fun trackToNewOffset() {
        //구도 추천 로직
        if (_trackingSwitchON.value && _modifiedPointState.value != null) {
            val backgroundBitmap = _bitmapState.value!!
            //이미지 사용하기
            val analyzedImageSize = ProPoseSize(backgroundBitmap.width, backgroundBitmap.height)
            viewModelScope.launch {
                val res = updatePointOffsetUseCase(
                    targetOffset = convertAnalyzedOffsetToPreviewOffset(
                        reversed = true, offset = ProPoseSizeF(
                            _modifiedPointState.value!!.x, _modifiedPointState.value!!.y
                        ), analyzedImageSize = analyzedImageSize
                    ), backgroundBitmap = backgroundBitmap
                )
                if (res == null) {
                    stopToTrack() //만약 에러인 경우 추적을 그만함.
                } else {
                    _modifiedPointState.update {
                        convertAnalyzedOffsetToPreviewOffset(
                            false, res, analyzedImageSize = analyzedImageSize
                        ).let { Offset(it.width, it.height) }
                    }
                }
            }
        }
    }


    fun startToTrack(previewSize: ProPoseSizeF) {
        previewSizeState = previewSize
        _trackingSwitchON.value = true
        viewModelScope.launch {
            _bitmapState.value?.let { bitmap ->
                recommendCompInfoUseCase(bitmap).let { res ->
                    _modifiedPointState.update {
                        previewSizeState!!.center.let {
                            Offset(
                                it.x * ((1F + res.first * 2)),
                                it.y * ((1F + res.second * 2))
                            )
                        }
                    }

                }
            }
        }
    }

    fun stopToTrack() {
        _trackingSwitchON.update { false }
        _modifiedPointState.update { null }
        stopPointOffsetUseCase()
    }

    fun setZoomLevel(zoomLevel: Float) = setZoomLevelUseCase(zoomLevel)

    fun changeViewRate(idx: Int): Boolean {
        val res = _aspectRatioState.value.aspectRatioType == viewRateList[idx].aspectRatioType
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

    fun getPoseFromImage(uri: Uri) {
        _fixedScreenState.value = null
        val res = getPoseFromImageUseCase(uri)
        _fixedScreenState.value = res
        viewModelScope.launch {
            Log.d("따오기 이미지: ", uri.toString())
            deleteImageFromPicturesUseCase(uri)
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


    fun loadUserSet() {
        viewModelScope.launch {
            _proPoseAppSettingsState.update { loadUserSetUseCase() }
        }
    }

    fun saveUserSet(proPoseAppSettings: ProPoseAppSettings) {
        viewModelScope.launch {
            saveUserSetUseCase(proPoseAppSettings = proPoseAppSettings)
        }
    }

}
