package com.example.proposeapplication.utils.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
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
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import com.example.camera.core.camera.FormatItem
import com.example.camera.core.camera.computeExifOrientation
import com.example.camera.core.camera.decodeExifOrientation
import com.example.proposeapplication.utils.ImageProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withTimeout

//카메라를 다루는 컨트롤러
class CameraController(private val context: Context) {

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private val imageProcessor by lazy {
        ImageProcessor(context)
    }

    private var openedCamera: CameraDevice? = null//사용중인 카메라 객체

    //촬영 세션
    private lateinit var session: CameraCaptureSession

    //현재 카메라의 특징을 가짐
    private lateinit var cameraCharacteristics: CameraCharacteristics


    //현재 사용중인 카메라 아이디를 저장
    private lateinit var nowCamId: String

    //사용 가능한 카메라 목록
    private val availableCameras by lazy {
        val res = mutableListOf<FormatItem>()
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
            res.add(
                FormatItem(
                    orientation, id, ImageFormat.JPEG
                )
            )
        }
        nowCamId = res[0].cameraId //현재 사용할 카메라 번호
        cameraCharacteristics = cameraManager.getCameraCharacteristics(nowCamId)
        res
    }

    //캡쳐된 이미지를 읽는 리더 객체
    private val capturedImageReader by lazy {
        cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(ImageFormat.JPEG)
            .maxByOrNull { it.height * it.width }!!.let { size ->
                ImageReader.newInstance(
                    size.width, size.height, ImageFormat.JPEG, IMAGE_BUFFER_SIZE
                )
            }
    }

    suspend fun takePhoto(orientation: Int) = getCapturedImage(
        imageProcessor.computeRelativeRotation(
            cameraCharacteristics, orientation
        )
    ).use { result ->
        val buffer = result.image.planes[0].buffer
        val exifOrientation = result.orientation
        val array = ByteArray(buffer.remaining())
        buffer.get(array)


        BitmapFactory.decodeByteArray(array, 0, array.size).let { src ->
            Log.d(
                CameraController::class.simpleName,
                "dimensions set: ${src.width} x ${src.height}"
            )
            val tmp = Bitmap.createScaledBitmap(
                src, src.width / 5, src.height / 5, true
            )
            //별도로 처리
            CoroutineScope(Dispatchers.IO).launch {
                imageProcessor.saveImageToGallery(
                    Bitmap.createBitmap(
                        src,
                        0,
                        0,
                        src.width,
                        src.height,
                        decodeExifOrientation(exifOrientation),
                        true
                    )
                )
            }

            Bitmap.createBitmap(
                tmp, 0, 0, tmp.width, tmp.height, decodeExifOrientation(exifOrientation), true
            )
        }
    }

    //고정 기능에 대한 결과값을 반환하는 함수
    private suspend fun provideFixedScreen() {

    }


    private suspend fun getCapturedImage(orientationData: Int) = suspendCoroutine { cont ->
        // Flush any images left in the image reader
        @Suppress("ControlFlowWithEmptyBody") while (capturedImageReader.acquireNextImage() != null) {
        }
        // Start a new image queue
        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        capturedImageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            Log.d("TAG", "Image available in queue: ${image.timestamp}")
            imageQueue.add(image)
        }, null)


        //캡쳐 요청을 생성
        val captureRequest = session.device.createCaptureRequest(
            CameraDevice.TEMPLATE_STILL_CAPTURE
        ).apply { addTarget(capturedImageReader.surface) }


        //캡쳐를 진행
        session.capture(
            captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                    Log.d(
                        "TAG", "Capture result received: $resultTimestamp"
                    ) //이미지가 정상적으로 처리될 때 로그를 남김
                    CoroutineScope(cont.context).launch {
                        try {
                            //5초 타임아웃
                            withTimeout(5000L) {
                                while (true) {
                                    // Dequeue images while timestamps don't match
                                    val image = withContext(Dispatchers.IO) {
                                        imageQueue.take()
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && image.format != ImageFormat.DEPTH_JPEG && image.timestamp != resultTimestamp) continue

                                    capturedImageReader.setOnImageAvailableListener(null, null)

                                    // Clear the queue of images, if there are left
                                    while (imageQueue.size > 0) {
                                        withContext(Dispatchers.IO) {
                                            imageQueue.take()
                                        }.close()
                                    }

                                    // Compute EXIF orientation metadata
                                    val rotation = orientationData
                                    val mirrored =
                                        cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                                    val exifOrientation = computeExifOrientation(rotation, mirrored)

                                    // Build the result and resume progress
                                    cont.resume(
                                        CombinedCaptureResult(
                                            image,
                                            result,
                                            exifOrientation,
                                            capturedImageReader.imageFormat
                                        )
                                    )
                                }
                            }
                        } catch (e: TimeoutException) {
                            cont.resumeWithException(e)
                        }
                    }
                }
            }, null
        )
    }


    suspend fun setPreview(surface: Surface) {
        if (openedCamera == null) openedCamera = openCamera(nowCamId)
        val targets = listOf(surface, capturedImageReader.surface)
        session = suspendCancellableCoroutine { cont ->
            val captureCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc =
                        RuntimeException("Camera ${openedCamera!!.id} session configuration failed")
                    Log.e("CameraFragment.TAG", exc.message, exc)
                    cont.resumeWithException(exc)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val configs = targets.map {
                    OutputConfiguration(it)
                }
                openedCamera!!.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        configs,
                        context.mainExecutor,
                        captureCallback
                    )
                )
            } else {
                openedCamera!!.createCaptureSession(
                    targets, captureCallback, null
                )
            }
        }
        session.setRepeatingRequest(
            openedCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                .apply { addTarget(surface) }.build(), null, null
        )
    }

    fun getPreviewSize(actContext: Context, display: Display) = availableCameras.let {
        val size = getPreviewOutputSize(
            actContext, display, cameraCharacteristics, SurfaceHolder::class.java
        )
        size
    }


    @SuppressLint("MissingPermission")
    private suspend fun openCamera(cameraId: String) = suspendCancellableCoroutine { cont ->
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) = cont.resume(camera)
            override fun onDisconnected(camera: CameraDevice) {
                Log.w("CameraFragment.TAG", "Camera $cameraId has been disconnected")
                openedCamera!!.close()
                openedCamera = null
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
        }, null)
    }


    companion object {
        const val IMAGE_BUFFER_SIZE = 5
        private val TAG = CameraController::class.simpleName

        data class CombinedCaptureResult(
            val image: Image, val metadata: CaptureResult, val orientation: Int, val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }
    }

}