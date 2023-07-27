package com.example.proposeapplication.presentation.view

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import com.example.proposeapplication.utils.camera.OrientationLiveData
import com.example.camera.core.camera.getPreviewOutputSize
import com.example.proposeapplication.presentation.CamearUiState
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _fragmentCameraViewBinding: FragmentCameraBinding? = null
    private val fragmentCameraViewBinding get() = _fragmentCameraViewBinding!!
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var relativeOrientation: OrientationLiveData

//    //Pytorch 사용을 위해
//    val torchController by lazy{
//        TorchController(requireContext())
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCameraBinding.inflate(inflater).apply {
        _fragmentCameraViewBinding = this
    }.root

    //뷰가 생성된 후, 각 값 세팅해주기
    //Surface 뷰에 미리보기를 띄워주기

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        relativeOrientation = OrientationLiveData(requireContext()).apply {
            observe(viewLifecycleOwner) { orientation ->
                when (orientation) {
                    Surface.ROTATION_0 -> {}
                    Surface.ROTATION_90 -> {}
                    Surface.ROTATION_180 -> {}
                    else -> {}
                }
                Log.d(CameraFragment::class.simpleName, "Orientation changed: $orientation")
            }
        }
        setUI()
    }

    override fun onResume() {
        super.onResume()
        setCaptureButton()
    }


    private fun setCaptureButton() {
        fragmentCameraViewBinding.apply {
            captureButton.setOnClickListener {
                lifecycleScope.launch {
                    mainViewModel.apply {
                        lockButtons(false)
                        takePhoto(relativeOrientation.value!!)
                        //
                        cUiState.collectLatest {
                            when (it) {
                                is CamearUiState.Success -> {
                                    if (it.data != null)
                                        ((it.data) as Bitmap).apply {
                                            Log.d(
                                                "${CameraFragment::class.simpleName}",
                                                "${height} * ${width}"
                                            )
                                            ivResult.setImageBitmap(this)
                                        }
                                    lockButtons(true)
                                }


                                is CamearUiState.Error -> {
                                    Log.e(
                                        "Exception: ", it.exception.message.toString()
                                    )
                                    lockButtons(true)
                                }

                                else -> null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUI() {

        //버튼 별 설정
        fragmentCameraViewBinding.apply {

            //버튼 레이어를 네비바 위로 옮겨주기 위함.
            lowerBox.setOnApplyWindowInsetsListener { v, insets ->
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    v.translationX = (-insets.systemWindowInsetRight).toFloat()
                    v.translationY = (-insets.systemWindowInsetBottom).toFloat()
                    insets.consumeSystemWindowInsets()
                } else {
                    v.translationX = (-insets.getInsets(
                        WindowInsets.Type.systemBars()
                    ).right).toFloat()
                    v.translationY = (-insets.getInsets(
                        WindowInsets.Type.systemBars()
                    ).bottom).toFloat()
                    WindowInsets.CONSUMED
                }
            }
            viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

                override fun surfaceChanged(
                    holder: SurfaceHolder, format: Int, width: Int, height: Int
                ) = Unit

                override fun surfaceCreated(holder: SurfaceHolder) {
                    // Selects appropriate preview size and configures view finder
                    fragmentCameraViewBinding.viewFinder.apply {
                        //적절한 미리보기 사이즈를 정해주는 곳 -> 한번 유심히 들여다 봐야할듯
                        val previewSize = mainViewModel.getPreviewSize(display)
                        this.setAspectRatio(previewSize.width, previewSize.height)
                        view!!.post {
                            mainViewModel.showPreview(fragmentCameraViewBinding.viewFinder.holder.surface)
                        }
                    }
                }
            })
        }
    }

//            //셀카 전환 버튼 구현
//            btnChangeFB.setOnClickListener {
//                if (cameraList.size > 1) cameraViewModel.changeCamera(
//                    when (cameraViewModel.cameraId) {
//                        cameraList[0].cameraId -> cameraList[1].cameraId
//                        cameraList[1].cameraId -> cameraList[0].cameraId
//                        else -> ""
//                    }, viewFinder.holder.surface
//                )
//            }


    private fun lockButtons(active: Boolean) {
        fragmentCameraViewBinding.apply {
            lockInBtn.isEnabled = active
            captureButton.isEnabled = active
        }
    }
}

//
//    //카메라 세팅을 담당하는 메소드
//    private fun setCamera() {
//        cameraViewModel.initCamera(fragmentCameraViewBinding.viewFinder.holder.surface) //뷰에서 표시할 surf
//        cameraViewModel.cameraStateLiveData.observe(requireActivity()) {
//            when (it) {
//                //미리보기 준비 완료 시
//                CameraViewModel.Companion.CameraState.READY_TO_PREVIEW
//                -> requireView().post {
//                    cameraViewModel.getPreview(fragmentCameraViewBinding.viewFinder.holder.surface)
//                    cameraViewModel.cameraStateLiveData.postValue(CameraViewModel.Companion.CameraState.READY_TO_TAKE)
//                }
//
//                else -> {}
//            }
//        }
//    }
//
//    private fun executeLockIn() {
//        fragmentCameraViewBinding.apply {
//            Log.d("ImageCaptured!: ", "이미지가 촬영됨.")
//            lockButtons(false)
//            Log.d("Image On process!: ", "이미지가 처리중임.")
//
//            lifecycleScope.launch(Dispatchers.Main) {
//                ivOverlay.apply {
//                    Glide.with(rootView).load(cameraViewModel.setLockIn(viewFinder, true))
//                        .sizeMultiplier(0.5f).into(this)
//                    alpha = 0.5F
//                }
//                closeOverlay.apply {
//                    this.isVisible = true
//                    setOnClickListener {
//                        ivOverlay.setImageResource(0)
//                        it.isVisible = false
//                    }
//                }
//                Log.d("Image On processed!: ", "이미지가 처리됨.")
//                lockButtons(true)
//            }
//        }
//    }
//
//    private fun executeCapture() {
//
//        fragmentCameraViewBinding.apply {
//            Log.d("ImageCaptured!: ", "이미지가 촬영됨.")
//            captureButton.isEnabled = false //버튼을 잠시 클릭 못하게 함.
//            Log.d("Image On process!: ", "이미지가 처리중임.")
//            lifecycleScope.launch(Dispatchers.IO) {
//                val image = cameraViewModel.takePhoto(relativeOrientation)
//
//                val time = measureTimeMillis {
//                    torchController.analyzeImage(requireContext(), image)
//                }
//
//                requireView().post {
//                    Toast.makeText(
//                        requireContext(),
//                        "파이토치 실행 소요시간 : ${time} ms",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    ivResult.setImageBitmap(image)
//                    Log.d("Image On processed!: ", "이미지가 처리됨.")
//                    captureButton.isEnabled = true
//                }
//            }
//        }
//    }
//
//    //카메라 인식하기
//    private fun detectCameras() {
//        val availableCameras = mutableListOf<FormatItem>()
//        val cameraIds = cameraManager.cameraIdList.filter { camId ->
//            cameraManager.getCameraCharacteristics(camId).let { ch ->
//                ch.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.contains(
//                    //정상 작동하는 카메라만 필터링한다.
//                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
//                ) ?: false
//            }
//        }
//        cameraIds.forEach { id ->
//            val characteristics = cameraManager.getCameraCharacteristics(id)
//            val orientation =
//                characteristics.get(CameraCharacteristics.LENS_FACING)!! //카메라 방향 을 저장해둠
//
//            availableCameras.add(
//                FormatItem(
//                    orientation,
//                    id, ImageFormat.JPEG
//                )
//            )
//        }
//        cameraList = availableCameras.toList()
//        characteristics =
//            cameraManager.getCameraCharacteristics(cameraList[CURRENT_CAMERA_NUMBER].cameraId)
//
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        try {
//            cameraViewModel.closeCamera()
//        } catch (exc: Throwable) {
//            Log.e(TAG, "Error closing camera", exc)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        setUI()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraViewModel.destroyVM()
//    }
//
//    override fun onDestroyView() {
//        _fragmentCameraViewBinding = null
//        super.onDestroyView()
//    }
//
//    companion object {
//        private val TAG = CameraFragment::class.java.simpleName
//        private const val CURRENT_CAMERA_NUMBER: Int = 0
//    }
//}
