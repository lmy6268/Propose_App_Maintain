package com.example.proposeapplication.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import com.example.camera.core.camera.FormatItem
import com.example.camera.core.camera.getPreviewOutputSize
import com.example.proposeapplication.domain.repository.CameraRepository
import kotlinx.coroutines.suspendCancellableCoroutine

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    CameraRepository {
    //변수 초기화

    //카메라를 관리할 수 있는 매니저 모듈
    private val cameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    //현재 사용중인 카메라 아이디를 저장
    private lateinit var nowCamId: String

    //촬영 세션
    private lateinit var session: CameraCaptureSession

    //현재 카메라의 특징을 가짐
    private lateinit var cameraCharacteristics: CameraCharacteristics

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
        nowCamId = res[0].cameraId
        cameraCharacteristics = cameraManager.getCameraCharacteristics(nowCamId)
        res
    }


    //캡쳐된 이미지를 읽는 리더 객체
    private val capturedImageReader by lazy {
        cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!.let { size ->
                ImageReader.newInstance(
                    size.width, size.height, ImageFormat.JPEG, IMAGE_BUFFER_SIZE
                )
            }
    }

    private lateinit var openedCamera: CameraDevice
    override fun getPreviewSize(display: Display) = availableCameras.let {
        getPreviewOutputSize(
            applicationContext, display, cameraCharacteristics, SurfaceHolder::class.java
        )
    }


    override suspend fun initPreview(surface: Surface) {
        openedCamera = openCamera(nowCamId)
        val targets = listOf(surface, capturedImageReader.surface)
        session = suspendCancellableCoroutine { cont ->
            val captureCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc =
                        RuntimeException("Camera ${openedCamera.id} session configuration failed")
                    Log.e("CameraFragment.TAG", exc.message, exc)
                    cont.resumeWithException(exc)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val configs = targets.map {
                    OutputConfiguration(it)
                }
                openedCamera.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        configs,
                        applicationContext.mainExecutor,
                        captureCallback
                    )
                )
            } else {
                openedCamera.createCaptureSession(
                    targets, captureCallback, null
                )
            }
        }
        session.setRepeatingRequest(
            openedCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                .apply { addTarget(surface) }.build(), null, null
        )
    }

    override fun saveResult() {
        TODO("Not yet implemented")
    }

    override fun takePhoto(): Bitmap {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(cameraId: String) = suspendCancellableCoroutine { cont ->
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) = cont.resume(camera)
            override fun onDisconnected(camera: CameraDevice) {
                Log.w("CameraFragment.TAG", "Camera $cameraId has been disconnected")
                openedCamera.close()
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
    }
}