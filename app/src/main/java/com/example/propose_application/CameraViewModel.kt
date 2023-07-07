package com.example.propose_application


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
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
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.camera.core.background.CustomThreadManager
import com.example.camera.core.camera.OrientationLiveData
import com.example.camera.core.camera.computeExifOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//UI 레이어와 도메인 레이어를 연결하는 인터페이스
// 이지만,,, 현재는 도메인과 데이터 레이어 분리가 안되어있는 상태,, -> 의존성 주입을 몷라서 고민된다.


class CameraViewModel(
    private var cameraId: String,
    private var cameraManager: CameraManager,
//    private val cameraCaptureUseCase: CameraCaptureUseCase
    application: Application
) : AndroidViewModel(application) {
    private var camera: CameraDevice? = null
    var session: CameraCaptureSession? = null
    private lateinit var characteristics: CameraCharacteristics
    private val IMAGE_BUFFER_SIZE = 3
    val cameraStateLiveData: MutableLiveData<CameraState> =
        MutableLiveData(CameraState.DO_NOTHING) //기본값

    //
    lateinit var capturedImageReader: ImageReader


    /** 카메라 스레드를 다루는 [Handler]*/
    private val cameraHandler by lazy {
        customThreadManager.getHandler("camera") as Handler
    }

    /** 이미지 스레드를 다루는 [Handler]  */
    private val imageReaderHandler by lazy {
        customThreadManager.getHandler("imageReader") as Handler
    }

    /**스레드 관리를 책임지는 클래스**/
    private val customThreadManager = CustomThreadManager.instance.apply {
        this.addHandler("imageReader")
        this.addHandler("camera")
    }

    //카메라를 동작하는 Executor ( API 30 이상 )
    private var cameraExecutor: Executor? = null

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
        capturedImageReader.close()
        customThreadManager.destroy() //스레드 관리 객체 초기화
        if (cameraExecutor != null) (cameraExecutor as ExecutorService).shutdown()
    }

    //카메라 객체를 가져오는 함수
    @SuppressLint("MissingPermission")
    private suspend fun getCamera(cameraId: String) =
        suspendCancellableCoroutine { cont ->
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)
                override fun onDisconnected(camera: CameraDevice) {
                    Log.w("CameraFragment.TAG", "Camera $cameraId has been disconnected")
                    closeCamera()
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
                    if (cont.isActive) {
                        cont.resumeWithException(exc)
                    }
                }
            }, cameraHandler)
        }


    private suspend fun getCameraSession(device: CameraDevice, targets: List<Surface>) =
        suspendCoroutine { cont ->
            //결과물에 대한 설정 값 목록
            val configs = ArrayList<OutputConfiguration>().apply {
                targets.forEach {
                    this.add(OutputConfiguration(it))
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cameraExecutor = Executors.newSingleThreadExecutor()
                device.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR, configs.toList(),
                        cameraExecutor!!,
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
//            saveResult(result)
            convertBufferToBitmap(result.image.planes[0].buffer, result.orientation)
        }

    /** 락인 기능
     * **/
    suspend fun setLockIn(viewFinder: SurfaceView, isEdge: Boolean) =
        //미리보기 화면 캡쳐를 통해 락인 기능 활성화
        suspendCancellableCoroutine<Bitmap?> { cont ->
            val bitmap =
                Bitmap.createBitmap(viewFinder.width, viewFinder.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(
                viewFinder,
                bitmap,
                { res ->
                    if (res == PixelCopy.SUCCESS) {
                        //이곳에 이미지 처리를 담아보자
                        cont.resume(if (isEdge) edgeDetection(bitmap) else bitmap)
                    } else cont.resume(null)

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
            ).apply { addTarget(capturedImageReader.surface) }

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
                                    && image.timestamp != resultTimestamp
                                ) continue


                                // Unset the image reader listener
                                imageReaderHandler.removeCallbacks(timeoutRunnable)
                                capturedImageReader.setOnImageAvailableListener(null, null)


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
                                    CombinedCaptureResult(
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
            camera = getCamera(cameraId) //cameraDevice 객체 얻기

            //촬영 해상도
            //이건 사용자가 선택할 수 있게 해볼까? -> 설정 화면 관련
            val size = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

            //가장 고해상도의 화면을 가져온다. -> 캡쳐된 이미지의
            capturedImageReader = ImageReader.newInstance(
                size.width, size.height, ImageFormat.JPEG,
                IMAGE_BUFFER_SIZE
            )
            Log.d(
                "사용 가능한 사이즈", Arrays.toString(
                    characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                    )!!.getOutputSizes(ImageFormat.JPEG)
                )
            )
            val targets = listOf(viewSurface, capturedImageReader.surface)
            session = getCameraSession(camera!!, targets)
            cameraStateLiveData.value = CameraState.READY_TO_PREVIEW
        }
    }

    fun closeCamera() {
        camera!!.close()
    }

    //ByteBuffer 를 비트맵으로 바꿔줌
    private fun convertBufferToBitmap(buffer: ByteBuffer, degree: Int): Bitmap =
        ByteArray(buffer.remaining()).apply { buffer.get(this) }.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

    private suspend fun saveResult(result: CombinedCaptureResult): File =
        suspendCoroutine { cont ->
            val buffer = result.image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
            try {
                val output = createFile(getApplication<Application>().applicationContext, "jpg")
                FileOutputStream(output).use { it.write(bytes) }
                cont.resume(output)
            } catch (exc: IOException) {
                Log.e("TAG", "Unable to write JPEG image to file", exc)
                cont.resumeWithException(exc)
            }
        }

    private fun createFile(context: Context, extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
    }

    companion object {
        enum class CameraState {
            DO_NOTHING,
            INIT_CAMERA_START,
            INIT_CAMERA_ERROR,
            READY_TO_PREVIEW,
            READY_TO_TAKE,
            TAKE_COMPLETE, TAKE_ERROR
        }

        data class CombinedCaptureResult(
            val image: Image, val metadata: CaptureResult, val orientation: Int, val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        class CameraViewModelFactory(
            private var cameraId: String,
            private var cameraManager: CameraManager,
            private val application: Application
//    private val cameraCaptureUseCase: CameraCaptureUseCase
        ) : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                    return CameraViewModel(
                        cameraId,
                        cameraManager,
                        application
//                ,cameraCaptureUseCase
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private fun makeGray(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat) // bitmap을 매트릭스로 변환

        //Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)



        return bitmap.copy(bitmap.config, true).apply {
            Utils.matToBitmap(mat, this)
        }
    }

    private fun edgeDetection(bitmap: Bitmap): Bitmap {
        val input = Mat()
        Utils.bitmapToMat(bitmap, input) // bitmap을 매트릭스로 변환
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY)
        //Convert to detected picture
        Imgproc.Canny(input, input, 50.0, 80.0)

        return bitmap.copy(bitmap.config, true).apply {
            Utils.matToBitmap(input, this)
        }
    }

}



