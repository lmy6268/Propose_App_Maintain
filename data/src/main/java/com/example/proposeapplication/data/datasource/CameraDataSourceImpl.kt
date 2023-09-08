package com.example.proposeapplication.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.proposeapplication.data.datasource.interfaces.CameraDataSource
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import org.opencv.android.OpenCVLoader
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraDataSourceImpl(private val context: Context) : CameraDataSource {

    private lateinit var cameraProvider: ProcessCameraProvider
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var executor: Executor
    private var camera: Camera? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun showPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        analyzer: Analyzer
    ) {
        OpenCVLoader.initDebug()
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        executor = ContextCompat.getMainExecutor(context)


        //현재 애플리케이션의 메인 스레드의 Executor를 가져온다.
        cameraProviderFuture.addListener(
            makeCameraListener(
                lifecycleOwner,
                surfaceProvider,
                ratio,
                cameraProviderFuture,
                analyzer
            ), executor
        )
    }

    override suspend fun takePhoto(): Bitmap = suspendCancellableCoroutine { cont ->
        imageCapture!!.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                image.use { imageProxy ->
                    // 원본 이미지를 획득함
                    val rotateMatrix =
                        Matrix().apply { setRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
                    val origin = imageProxy.toBitmap().let {
                        Bitmap.createBitmap(
                            it, 0, 0, it.width, it.height, rotateMatrix, false
                        )
                    }
                    cont.resume(origin)
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


    @OptIn(ExperimentalZeroShutterLag::class)
    @SuppressLint("RestrictedApi")
    private fun makeCameraListener(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        ratio: AspectRatioStrategy,
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        analyzer: Analyzer
    ) = Runnable {
        cameraProvider = cameraProviderFuture.get()
        val ratioSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY
            )
            .setAspectRatioStrategy(ratio).build()

        val analyzerRatioSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(640, 640), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            ).setAspectRatioStrategy(ratio).build()


        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview = Preview.Builder().setResolutionSelector(ratioSelector).build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().setResolutionSelector(
            ratioSelector
        ).setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()

        imageAnalysis =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(analyzerRatioSelector)
                .build()
                .apply {
                    setAnalyzer(
                        executor, analyzer
                    )
                }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, // Use the provided lifecycle owner
                cameraSelector, preview, imageCapture, imageAnalysis
            )
            Log.d("CameraStatus: ", camera!!.cameraInfo.cameraState.value.toString())
            // Now you have the CameraControl instance if you need it
        } catch (exc: Exception) {
            // Handle camera initialization error
            Log.e("Error on Init Camera", "에러입니다", exc)

        }
    }


    override fun setZoomLevel(zoomLevel: Float) {
//        val minValue = camera.cameraInfo.zoomState.value!!.minZoomRatio
//        val maxValue = camera.cameraInfo.zoomState.value!!.maxZoomRatio
//        Log.d("MIN/MAX ZoomRatio: ","$minValue/$maxValue")
        camera!!.cameraControl.setZoomRatio(zoomLevel)
    }
}