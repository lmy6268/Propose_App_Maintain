package com.example.proposeapplication.presentation.view

import android.annotation.SuppressLint
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proposeapplication.utils.camera.OrientationLiveData
import com.example.proposeapplication.presentation.uistate.CamearUiState
import com.example.proposeapplication.presentation.MainViewModel
import com.example.proposeapplication.presentation.databinding.FragmentCameraBinding
import com.example.proposeapplication.utils.pose.PoseRecommendModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _fragmentCameraViewBinding: FragmentCameraBinding? = null
    private val fragmentCameraViewBinding get() = _fragmentCameraViewBinding!!
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var relativeOrientation: OrientationLiveData


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCameraBinding.inflate(LayoutInflater.from(requireActivity())).apply {
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
                        captureButton.isEnabled = false
                        takePhoto(relativeOrientation.value!!)

                        captureUiState.collectLatest {
                            when (it) {
                                is CamearUiState.Success -> {
                                    if (it.data != null)
                                        ((it.data) as Bitmap).apply {

                                            Log.d(
                                                "${CameraFragment::class.simpleName}",
                                                "$height * $width"
                                            )
                                            Glide.with(root).load(this).circleCrop().into(ivResult)
                                        }
                                    captureButton.isEnabled = true
                                }

                                is CamearUiState.Error -> {
                                    Log.e(
                                        "Exception: ", it.exception.message.toString()
                                    )
                                    captureButton.isEnabled = true
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
            //버튼 레이어를 네비바 위로 옮겨주기 위함 -> 권한 설정 곧바로 동작하지 않고 앱이 다시 시작되어야 작동합니다.
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
            lockInBtn.setOnClickListener {
                lifecycleScope.launch {
                    lockInBtn.isEnabled = false
                    mainViewModel.getFixedScreen(viewFinder)
                    mainViewModel.fixedScreenUiState.collectLatest {
                        if (it is CamearUiState.Success) {
                            if (it.data != null) setFixedScreen((it.data as Bitmap))
                            lockInBtn.isEnabled = true
                        }
                    }
                }
            }

            viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

                override fun surfaceChanged(
                    holder: SurfaceHolder, format: Int, width: Int, height: Int
                ) = Unit

                override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                    // Selects appropriate preview size and configures view finder
                    fragmentCameraViewBinding.viewFinder.apply {
                        //적절한 미리보기 사이즈를 정해주는 곳 -> 한번 유심히 들여다 봐야할듯
                        mainViewModel.getPreviewSize(requireContext(), display).apply {
                            setAspectRatio(width, height)
                            view!!.post {
                                mainViewModel.showPreview(holder.surface)
                            }
                        }

                    }
                }
            })
        }
    }

    private fun setFixedScreen(bitmap: Bitmap) {

        fragmentCameraViewBinding.apply {
            ivOverlay.apply {
                Glide.with(rootView).load(bitmap)
                    .sizeMultiplier(0.5f).into(this)
                alpha = 0.5F
            }
            closeOverlay.apply {
                isVisible = true
                setOnClickListener {
                    ivOverlay.setImageResource(0)
                    isVisible = false
                }
            }
        }

    }
}
//    companion object {
//        private val TAG = CameraFragment::class.java.simpleName
//        private const val CURRENT_CAMERA_NUMBER: Int = 0
//    }
//}
