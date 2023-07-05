package com.example.propose_application

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.Transformation
import com.example.camera.core.camera.FormatItem
import com.example.camera.core.camera.OrientationLiveData
import com.example.camera.core.camera.computeExifOrientation
import com.example.camera.core.camera.getPreviewOutputSize
import com.example.camera.domain.CameraCaptureUseCase
import com.example.camera.domain.CameraCaptureUseCaseImpl
import com.example.propose_application.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt


class CameraFragment : Fragment() {


    //카메라 리스트: 줌 인 줌 아웃할 때 사용 할 듯?
    private lateinit var cameraList: List<FormatItem>

    //카메라 매니저 - 카메라를 다루기 위한 메니저
    private lateinit var cameraManager: CameraManager

    //카메라 특성
    private lateinit var characteristics: CameraCharacteristics

    private var _fragmentCameraViewBinding: FragmentCameraBinding? = null
    private val fragmentCameraViewBinding get() = _fragmentCameraViewBinding!!

    private lateinit var relativeOrientation: OrientationLiveData
    private lateinit var cameraViewModel: CameraViewModel
    private var aspectRatio: Float = 0F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCameraBinding.inflate(inflater).apply {
        _fragmentCameraViewBinding = this
    }.root

    //뷰가 생성된 후, 각 값 세팅해주기
    //Surface 뷰에 미리보기를 띄워주기

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        detectCameras() //이용할 카메라를 선택하기 위함.
        cameraViewModel = ViewModelProvider(
            this,
            CameraViewModelFactory(
                cameraList[0].cameraId,
                cameraManager, requireActivity().application
            )
        ).get(CameraViewModel::class.java)


        // Used to rotate the output media to match device orientation
        relativeOrientation = OrientationLiveData(requireContext(), characteristics).apply {
            observe(viewLifecycleOwner) { orientation ->
                Log.d(TAG, "Orientation changed: $orientation")
            }
        }
    }

    private fun setUI() {
        //버튼 별 설정
        fragmentCameraViewBinding.apply {
            captureButton.setOnClickListener { captureBtn ->
                Log.d("ImageCaptured!: ", "이미지가 촬영됨.")
                captureBtn.isEnabled = false //버튼을 잠시 클릭 못하게 함.
                Log.d("Image On process!: ", "이미지가 처리중임.")
                lifecycleScope.launch(Dispatchers.IO) {
                    val image = cameraViewModel.takePhoto(relativeOrientation)
                    requireView().post {
                        ivResult.setImageBitmap(image)
                        Log.d("Image On processed!: ", "이미지가 처리됨.")
                        captureBtn.isEnabled = true
                    }
                }
            }
            lockInBtn.setOnClickListener {
                Log.d("ImageCaptured!: ", "이미지가 촬영됨.")
                lockButtons(false)
                Log.d("Image On process!: ", "이미지가 처리중임.")

                lifecycleScope.launch(Dispatchers.Main) {
                    ivOverlay.apply {
                        Glide.with(rootView).load(cameraViewModel.setLockIn(viewFinder)).into(this)
                        alpha = 0.5F
                    }
                    closeOverlay.apply {
                        this.isVisible = true
                        setOnClickListener {
                            ivOverlay.setImageResource(0)
                            it.isVisible = false
                        }
                    }
                    Log.d("Image On processed!: ", "이미지가 처리됨.")
                    lockButtons(true)
                }
            }
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

                    //적절한 미리보기 사이즈를 정해주는 곳 -> 한번 유심히 들여다
                    val previewSize = getPreviewOutputSize(
                        fragmentCameraViewBinding.viewFinder.display,
                        characteristics,
                        SurfaceHolder::class.java
                    )

                }
            })
        }
        setCamera()
    }

    private fun lockButtons(active: Boolean) {
        fragmentCameraViewBinding.apply {
            lockInBtn.isEnabled = active
            captureButton.isEnabled = active
        }
    }

    //카메라 세팅을 담당하는 메소드
    private fun setCamera() {
        cameraViewModel.initCamera(fragmentCameraViewBinding.viewFinder.holder.surface) //뷰에서 표시할 surf
        cameraViewModel.cameraStateLiveData.observe(requireActivity()) {
            when (it) {
                CameraState.READY_TO_PREVIEW
                -> requireView().post {
                    cameraViewModel.getPreview(fragmentCameraViewBinding.viewFinder.holder.surface)
                    cameraViewModel.cameraStateLiveData.postValue(CameraState.READY_TO_TAKE)
                }


                else -> {}
            }
        }
    }


    //카메라 인식하기
    private fun detectCameras() {
        val availableCameras = mutableListOf<FormatItem>()
        val cameraIds = cameraManager.cameraIdList.filter { camId ->
            cameraManager.getCameraCharacteristics(camId).let { ch ->
                ch.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.contains(
                    //정상 작동하는 카메라만 필터링한다.
                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
                ) ?: false
            }
        }
        cameraIds.forEach { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val orientation =
                characteristics.get(CameraCharacteristics.LENS_FACING)!! //카메라 방향 을 저장해둠

            availableCameras.add(
                FormatItem(
                    orientation,
                    id, ImageFormat.JPEG
                )
            )
        }
        cameraList = availableCameras.toList()
        characteristics =
            cameraManager.getCameraCharacteristics(cameraList[CURRENT_CAMERA_NUMBER].cameraId)

    }

    //


    override fun onStop() {
        super.onStop()
        try {
            cameraViewModel.closeApp()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onResume() {
        super.onResume()
        setUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewModel.destroyVM()
    }

    override fun onDestroyView() {
        _fragmentCameraViewBinding = null
        super.onDestroyView()
    }

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
        private const val CURRENT_CAMERA_NUMBER: Int = 0
    }
}
