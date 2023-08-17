package com.example.proposeapplication.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proposeapplication.domain.usecase.camera.CaptureImageUseCase
import com.example.proposeapplication.domain.usecase.camera.GetCompInfoUseCase
import com.example.proposeapplication.domain.usecase.camera.SetZoomLevelUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowFixedScreenUseCase
import com.example.proposeapplication.domain.usecase.camera.ShowPreviewUseCase
import com.example.proposeapplication.utils.pose.PoseRecommendControllerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    //UseCases
    private val showPreviewUseCase: ShowPreviewUseCase,
    private val captureImageUseCase: CaptureImageUseCase,
    private val showFixedScreenUseCase: ShowFixedScreenUseCase,
    private val setZoomLevelUseCase: SetZoomLevelUseCase,
    private val getCompInfoUseCase: GetCompInfoUseCase
) : ViewModel() {
    //Switches
    val reqFixedScreenState = MutableStateFlow(false)// 고정 화면 요청 on/off
    private val reqPoseState = MutableStateFlow(false)// 포즈 추천 요청 on/off
    private val reqCompState = MutableStateFlow(false) // 구도 추천 요청 on/off
    private val poseRecommendControllerImpl = PoseRecommendControllerImpl(context) //포즈 추천 컨트롤러

    //Result Holder
    private val _edgeDetectBitmapState = MutableStateFlow<Bitmap?>(//고정된 이미지 상태
        null
    )

    private val _capturedBitmapState = MutableStateFlow<Bitmap>( //캡쳐된 이미지 상태
        Bitmap.createBitmap(
            100, 100, Bitmap.Config.ARGB_8888
        )
    )
    private val _poseResultState = MutableStateFlow(listOf(""))
    private val _compResultState = MutableStateFlow("")

    //State Getter
    val edgeDetectBitmapState = _edgeDetectBitmapState.asStateFlow() //고정 이미지의 상태를 저장해두는 변수
    val capturedBitmapState = _capturedBitmapState.asStateFlow()
    val poseResultState = _poseResultState.asStateFlow()
    val compResultState = _compResultState.asStateFlow()


    //매 프레임의 image를 수신함.
    private val imageAnalyzer = ImageAnalysis.Analyzer { it ->
        it.use { image ->
            //포즈 선정 로직
            if (reqPoseState.value) {
                viewModelScope.launch {
                    val target = adjustRotationInfo(image)
                    testPose(target)
//                    _poseResultState.value =
//                        PoseRecommendModule.getHOG(target).toString()


//                        poseRecommendControllerImpl.getRecommendPose(image.toBitmap())
                    reqPoseState.value = false
                }
            }
            //구도 추천 로직
            if (reqCompState.value) {
                viewModelScope.launch {
                    _compResultState.value = getCompInfoUseCase(adjustRotationInfo(image))
                    reqCompState.value = false
                }
            }
            //고정화면 로직
            if (reqFixedScreenState.value) {
                _edgeDetectBitmapState.value = null
                viewModelScope.launch {
                    _edgeDetectBitmapState.value =
                        showFixedScreenUseCase(
                            adjustRotationInfo(image)
                        )!!
                }
                reqFixedScreenState.value = false //고정 요청 버튼을 off
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

    fun reqPoseRecommend() {
        reqPoseState.value = true
    }

    fun testPose(bitmap: Bitmap) {


        Log.d(
            "Pose data elapse time", "${
                measureTimeMillis {
                    viewModelScope.launch {
                        val res = poseRecommendControllerImpl.getRecommendPose(bitmap)
                        _poseResultState.value = listOf(res)
//                    PoseRecommendModule.getHOG(bitmap).toString
                        Log.d("Pose data: ", res)
                    }
                }
            }ms"


        )
//        viewModelScope.launch {
//            _poseResultState.value =
////                    PoseRecommendModule.getHOG(bitmap).toString()
//                poseRecommendControllerImpl.getRecommendPose(bitmap)
//        }
    }

    fun reqCompRecommend() {
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
}