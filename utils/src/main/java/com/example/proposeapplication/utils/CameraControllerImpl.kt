package com.example.proposeapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraControllerImpl(private val context: Context) : CameraControllerInterface {
    private val imageProcessor by lazy {
        ImageProcessor(context)
    }
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var executor: Executor
    private lateinit var camera: Camera

    override fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: ImageAnalysis.Analyzer
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        executor = ContextCompat.getMainExecutor(context) //현재 애플리케이션의 메인 스레드의 Executor를 가져온다.

        cameraProviderFuture.addListener(
            makeCameraListener(
                lifecycleOwner, surfaceProvider, ratio, cameraProviderFuture, analyzer
            ), executor
        )
    }

    override suspend fun takePhoto(): Bitmap =
        suspendCancellableCoroutine<Bitmap> { cont ->
            imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.use { imageProxy ->
                        // 원본 이미지를 획득함
                        val rotateMatrix =
                            Matrix().apply { setRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
                        val origin = imageProxy.toBitmap().let {
                            Bitmap.createBitmap(
                                it,
                                0,
                                0,
                                it.width,
                                it.height,
                                rotateMatrix,
                                false
                            )
                        }

                        val tmp =
                            Bitmap.createScaledBitmap(
                                origin, origin.width / 5, origin.height / 5, true
                            )
                        cont.resume(tmp)
                        //갤러리에 저장함
                        CoroutineScope(Dispatchers.IO).launch {
                            imageProcessor.saveImageToGallery(origin)
                        }
                        super.onCaptureSuccess(image)
                    }

                }

                //에러가 나는 경우 에러를 반환한다.
                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                    super.onError(exception)
                }

            })
        }


    private fun makeCameraListener(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        analyzer: ImageAnalysis.Analyzer
    ) = Runnable {
        cameraProvider = cameraProviderFuture.get()

        val ratioSelector = ResolutionSelector.Builder().setAspectRatioStrategy(ratio).build()

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview = Preview.Builder().setResolutionSelector(ratioSelector).build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().setResolutionSelector(
            ratioSelector
        ).setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()

        imageAnalysis = ImageAnalysis.Builder().setResolutionSelector(ratioSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build().apply {
                setAnalyzer(
                    executor, analyzer
                )
            }


        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, // Use the provided lifecycle owner
                cameraSelector, preview, imageCapture,
                imageAnalysis
            )
            // Now you have the CameraControl instance if you need it
        } catch (exc: Exception) {
            // Handle camera initialization error
            Log.e("Error on Init Camera", "에러입니다", exc)

        }
    }

    fun getFixedScreen(rawBitmap: Bitmap) = imageProcessor.edgeDetection(rawBitmap)

    override fun getLatestImage(): Bitmap? =
        imageProcessor.getLatestImage()

    fun setZoomLevel(zoomLevel: Float) {
//        val minValue = camera.cameraInfo.zoomState.value!!.minZoomRatio
//        val maxValue = camera.cameraInfo.zoomState.value!!.maxZoomRatio
//        Log.d("MIN/MAX ZoomRatio: ","$minValue/$maxValue")
        camera.cameraControl.setZoomRatio(zoomLevel)
    }
}