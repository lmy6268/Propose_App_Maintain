package com.example.proposeapplication.presentation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowFixedScreenUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import com.example.proposeapplication.utils.pose.PoseRecommendModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //UseCases
    private val showPreviewUseCase: ShowPreviewUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
) : ViewModel() {
    //Switches
    private val reqFixedScreenState = MutableStateFlow(false)// 고정 화면 요청 on/off
    private val reqPoseState = MutableStateFlow(false)// 포즈 추천 요청 on/off
    private val reqCompoState = MutableStateFlow(false) // 구도 추천 요청 on/off

    //Result Holder
    private val _edgeDetectBitmapState = MutableStateFlow(//고정된 이미지 상태
        Bitmap.createBitmap(
            100, 100, Bitmap.Config.ARGB_8888
        )
    )
    private val _capturedBitmapState = MutableStateFlow( //캡쳐된 이미지 상태
        Bitmap.createBitmap(
            100, 100, Bitmap.Config.ARGB_8888
        )
    )
    private val _poseResultState = MutableStateFlow("")

    //State Getter
    val edgeDetectBitmapState = _edgeDetectBitmapState.asStateFlow() //고정 이미지의 상태를 저장해두는 변수
    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { it ->
        it.use { image ->
            //포즈 선정 로직
            if (reqPoseState.value) {
                viewModelScope.launch {
                    val target = image.toBitmap().let { bitmap ->
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
                    _poseResultState.value = PoseRecommendModule.getHOG(target).toString()
                    reqPoseState.value = false
                }
            }
            //구도 추천 로직

            //고정화면 로직
            if (reqFixedScreenState.value) {
                Log.d("data", image.planes.toString())
                viewModelScope.launch {
                    _edgeDetectBitmapState.value =
                        showFixedScreenUseCase(image.toBitmap().let { bitmap ->
                            Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) },
                                true
                            )
                        })!!
                    reqFixedScreenState.value = false //고정 요청 버튼을 off
                }
            }
        }
    }


    fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy
    ) {
        showPreviewUseCase(lifecycleOwner, surfaceProvider, ratio, imageAnalyzer)
    }

    fun getPhoto(

    ) {
        viewModelScope.launch {
            _capturedBitmapState.value = captureImageUseCase()
        }
    }

    fun reqFixedScreen() {
        reqFixedScreenState.value = true
    }

    fun reqPoseRecommend() {
        reqPoseState.value = true
    }

    fun testPose(bitmap: Bitmap) {
        viewModelScope.launch {
            _poseResultState.value = PoseRecommendModule.getHOG(bitmap).toString()

        }
    }

}