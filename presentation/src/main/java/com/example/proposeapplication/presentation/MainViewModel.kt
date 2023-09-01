package com.example.proposeapplication.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.ai.RecommendCompInfoUseCase
import com.example.proposeapplication.domain.usecase.ai.RecommendPoseUseCase
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.camera.GetLatestImageUseCase
import com.example.proposeapplication.domain.usecase.camera.SetZoomLevelUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowFixedScreenUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import com.example.proposeapplication.utils.pose.PoseData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //UseCases
    private val showPreviewUseCase: ShowPreviewUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
    private val setZoomLevelUseCase: SetZoomLevelUseCase,
    private val recommendCompInfoUseCase: RecommendCompInfoUseCase,
    private val recommendPoseUseCase: RecommendPoseUseCase,
    private val getLatestImageUseCase: GetLatestImageUseCase
) : ViewModel() {
    //Switches

    // 고정 화면 요청 on/off
    val reqFixedScreenState = MutableStateFlow(false).apply {
        viewModelScope.launch {
            collectLatest {
                if (it) {
                    _edgeDetectBitmapState.value = null //이미지
                    _edgeDetectBitmapState.value =
                        showFixedScreenUseCase()
                    this@apply.update { false }
                }
            }
        }
    }
    private val reqPoseState = MutableStateFlow(false)// 포즈 추천 요청 on/off
    private val reqCompState = MutableStateFlow(false) // 구도 추천 요청 on/off


    //Result Holder
    private val _edgeDetectBitmapState = MutableStateFlow<Bitmap?>(//고정된 이미지 상태
        null
    )

    private val _capturedBitmapState = MutableStateFlow( //캡쳐된 이미지 상태
        Bitmap.createBitmap(
            100, 100, Bitmap.Config.ARGB_8888
        )
    )
    private val _poseResultState = MutableStateFlow<Pair<DoubleArray?, List<PoseData>?>?>(null)
    private val _compResultState = MutableStateFlow("")
    private val _downloadResultState = MutableStateFlow(mutableMapOf<String, Long>())

    //State Getter
    val edgeDetectBitmapState = _edgeDetectBitmapState.asStateFlow() //고정 이미지의 상태를 저장해두는 변수
    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()
    val compResultState = _compResultState.asStateFlow()
    val downloadResultState = _downloadResultState.asStateFlow()


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { it ->
        it.use { image ->
            //포즈 선정 로직
            if (reqPoseState.value) {
                reqPoseState.value = false
                _poseResultState.value = Pair(null, null)
                viewModelScope.launch {
                    _poseResultState.value =
                        recommendPoseUseCase(adjustRotationInfo(image))
                }


            }
            //구도 추천 로직
            if (reqCompState.value) {
                reqCompState.value = false
                viewModelScope.launch {
                    _compResultState.value = recommendCompInfoUseCase(adjustRotationInfo(image))
                }
            }
        }
    }


    fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy
    ) {
        showPreviewUseCase(
            lifecycleOwner,
            surfaceProvider,
            ratio,
            analyzer = imageAnalyzer

        )
    }

    fun getPhoto(

    ) {
        viewModelScope.launch {
            _capturedBitmapState.value = captureImageUseCase()
        }
    }

    fun reqPoseRecommend() {
        if (reqPoseState.value.not())
            reqPoseState.value = true
    }


    fun reqCompRecommend() {
        if (reqCompState.value.not())
            reqCompState.value = true
    }

    fun setZoomLevel(zoomLevel: Float) = setZoomLevelUseCase(zoomLevel)
    private fun adjustRotationInfo(image: ImageProxy) = image.toBitmap().let { bitmap ->
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) },
            true
        )
    }

    //최근 이미지
    fun lastImage() =
        getLatestImageUseCase()

}
