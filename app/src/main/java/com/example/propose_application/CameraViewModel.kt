package com.example.propose_application

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.graphics.ImageFormat
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.camera.core.camera.AutoFitSurfaceView
import com.example.camera.core.camera.OrientationLiveData
import com.example.camera.core.camera.SmartSize
import com.example.camera.core.camera.computeExifOrientation
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.TimeoutException

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//UI 레이어와 도메인 레이어를 연결하는 인터페이스
// 이지만,,, 현재는 도메인과 데이터 레이어 분리가 안되어있는 상태,, -> 의존성 주입을 몷라서 고민된다.
enum class CameraState {
    DO_NOTHING,
    INIT_CAMERA_START,
    INIT_CAMERA_ERROR,
    READY_TO_PREVIEW,
    READY_TO_TAKE,
    TAKE_COMPLETE, TAKE_ERROR
}


class CameraViewModelFactory(
    private var cameraId: String,
    private var cameraManager: CameraManager,
//    private val cameraCaptureUseCase: CameraCaptureUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(
                cameraId,
                cameraManager
//                ,cameraCaptureUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CameraViewModel(
    private var cameraId: String,
    private var cameraManager: CameraManager,
//    private val cameraCaptureUseCase: CameraCaptureUseCase
) : ViewModel() {
    private var camera: CameraDevice? = null
    var session: CameraCaptureSession? = null
    private lateinit var characteristics: CameraCharacteristics
    private val IMAGE_BUFFER_SIZE = 3
    val cameraStateLiveData: MutableLiveData<CameraState> =
        MutableLiveData(CameraState.DO_NOTHING) //기본값
    //
    lateinit var capturedImageReader: ImageReader

    /**[HandlerThread] 촬영된 이미지가 동작하는 스레드 */
    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    /**[Handler] corresponding to[imageReaderThread] */
    private val imageReaderHandler = Handler(imageReaderThread.looper)

    /**[HandlerThread] 모든 카메라가 동작하는 스레드 */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** 카메라 스레드를 다루는 [Handler]*/
    private val cameraHandler = Handler(cameraThread.looper)


    //preview 화면을 얻어오는 메소드
    fun getPreview(surface: Surface) {
        session!!.setRepeatingRequest(
            camera!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply { addTarget(surface) }.build(),
            null, imageReaderHandler
        )
    }


    fun destroyVM() {
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }

    //카메라 객체를 가져오는 함수
    @SuppressLint("MissingPermission")
    private suspend fun getCamera(cameraId: String) =
        suspendCancellableCoroutine { cont ->
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)
                override fun onDisconnected(camera: CameraDevice) {
                    Log.w("CameraFragment.TAG", "Camera $cameraId has been disconnected")
                    closeApp()
//                        requireActivity().finish()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    val msg = when (error) {
                        ERROR_CAMERA_DEVICE -> "Fatal (device)"
                        ERROR_CAMERA_DISABLED -> "Device policy"
                        ERROR_CAMERA_IN_USE -> "Camera in use"
                        ERROR_CAMERA_SERVICE -> "Fatal (service)"
                        ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                        else -> "Unknown"
                    }
                    val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                    Log.e("CameraFragment.TAG", exc.message, exc)
                    if (cont.isActive) cont.resumeWithException(exc)
                }
            }, cameraHandler)
        }


    private suspend fun getCameraSession(device: CameraDevice, targets: List<Surface>) =
        suspendCoroutine<CameraCaptureSession> { cont ->
            //결과물에 대한 설정 값 목록
            val configs = ArrayList<OutputConfiguration>().apply {
                targets.forEach {
                    this.add(OutputConfiguration(it))
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                device.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR, configs.toList(),
                        cameraHandler as Executor, //여기엔 무얼 넣어야 할까?
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) =
                                cont.resume(session)

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                val exc =
                                    RuntimeException("Camera ${device.id} session configuration failed")
                                Log.e("CameraFragment.TAG", exc.message, exc)
                                cont.resumeWithException(exc)
                            }

                        })
                )
            } else {
                device.createCaptureSession(
                    targets,
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(session: CameraCaptureSession) =
                            cont.resume(session)

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            val exc =
                                RuntimeException("Camera ${device.id} session configuration failed")
                            Log.e("CameraFragment.TAG", exc.message, exc)
                            cont.resumeWithException(exc)
                        }
                    },
                    cameraHandler
                )
            }

        }

    //카메라를 촬영하는 메소드
    suspend fun takePhoto(relativeOrientation: OrientationLiveData) =
        getCapturedImage(relativeOrientation).use { result ->
            convertBufferToBitmap(result.image.planes[0].buffer, result.orientation)
        }

    suspend fun setLockIn(viewFinder:SurfaceView) =
        //미리보기 화면 캡쳐를 통해 락인 기능 활성화
        suspendCancellableCoroutine<Bitmap?> {
                cont->
            val bitmap = Bitmap.createBitmap(viewFinder.width, viewFinder.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(viewFinder,
                bitmap,
                {res->
                    if(res== PixelCopy.SUCCESS) cont.resume(bitmap)
                    else cont.resume(null)

                }, Handler(Looper.getMainLooper())
            )
        }


    private suspend fun getCapturedImage(relativeOrientation: OrientationLiveData) =
        suspendCoroutine { cont ->
            // Flush any images left in the image reader
            @Suppress("ControlFlowWithEmptyBody")
          while (capturedImageReader.acquireNextImage() != null) {
            }


            // Start a new image queue
            val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
           capturedImageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()
                Log.d("TAG", "Image available in queue: ${image.timestamp}")
                imageQueue.add(image)
            }, imageReaderHandler)

            val captureRequest = session!!.device.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            ).apply {addTarget(capturedImageReader.surface)}

            session!!.capture(
                captureRequest.build(),
                object : CameraCaptureSession.CaptureCallback() {

                    override fun onCaptureStarted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        timestamp: Long,
                        frameNumber: Long
                    ) {
                        super.onCaptureStarted(session, request, timestamp, frameNumber)
                        //혹시 촬영 효과 있다면 추가
                    }

                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        super.onCaptureCompleted(session, request, result)
                        val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                        Log.d("TAG", "Capture result received: $resultTimestamp")

                        // Set a timeout in case image captured is dropped from the pipeline
                        val exc = TimeoutException("Image dequeuing took too long")
                        val timeoutRunnable = Runnable { cont.resumeWithException(exc) }
                        imageReaderHandler.postDelayed(
                            timeoutRunnable,
                            5000L
                        )

                        // Loop in the coroutine's context until an image with matching timestamp comes
                        // We need to launch the coroutine context again because the callback is done in
                        //  the handler provided to the `capture` method, not in our coroutine context
                        viewModelScope.launch(cont.context) {
                            while (true) {
                                // Dequeue images while timestamps don't match
                                val image =
                                    withContext(Dispatchers.IO) {
                                        imageQueue.take()
                                    }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                                    && image.format != ImageFormat.DEPTH_JPEG
                                    && image.timestamp != resultTimestamp) continue


                                // Unset the image reader listener
                                imageReaderHandler.removeCallbacks(timeoutRunnable)
                         capturedImageReader.setOnImageAvailableListener(null,null)


                                // Clear the queue of images, if there are left
                                while (imageQueue.size > 0) {
                                    withContext(Dispatchers.IO) {
                                        imageQueue.take()
                                    }.close()
                                }

                                // Compute EXIF orientation metadata
                                val rotation =
                                    relativeOrientation.value ?: 0
                                val mirrored =
                                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                                val exifOrientation = computeExifOrientation(rotation, mirrored)

                                // Build the result and resume progress
                                cont.resume(
                                    CameraFragment.Companion.CombinedCaptureResult(
                                        image, result, exifOrientation,
                                  capturedImageReader.imageFormat

                                    )
                                )
                            }
                        }
                    }
                },
                cameraHandler
            )

        }

    fun initCamera(viewSurface: Surface) {
        viewModelScope.launch {
            cameraStateLiveData.value = CameraState.INIT_CAMERA_START
            characteristics = cameraManager.getCameraCharacteristics(cameraId)
            camera = getCamera(cameraId) //cameraDevice

            val size = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
                //가장 고해상도의 화면을 가져온다.
            capturedImageReader = ImageReader.newInstance(
                size.width, size.height, ImageFormat.JPEG,
                IMAGE_BUFFER_SIZE
            )
            Log.d("사용 가능한 사이즈", Arrays.toString(characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!.getOutputSizes(ImageFormat.JPEG))
            )
            val targets = listOf(viewSurface, capturedImageReader.surface)
            session = getCameraSession(camera!!, targets)
            cameraStateLiveData.value = CameraState.READY_TO_PREVIEW
        }
    }

    fun closeApp() {
        camera!!.close()
    }

    //Buffer를 비트맵으로 바꿔줌
    private fun convertBufferToBitmap(buffer: ByteBuffer, degree: Int): Bitmap =
        ByteArray(buffer.remaining()).apply { buffer.get(this) }.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

}