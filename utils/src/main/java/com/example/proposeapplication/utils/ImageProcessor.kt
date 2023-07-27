package com.example.proposeapplication.utils

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.os.Build
import android.provider.MediaStore
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//이미지를 처리하는 프로세서
class ImageProcessor(private val context: Context) {
    /**
     * Computes rotation required to transform from the camera sensor orientation to the
     * device's current orientation in degrees.
     *
     * @param characteristics the [CameraCharacteristics] to query for the sensor orientation.
     * @param surfaceRotation the current device orientation as a Surface constant
     * @return the relative rotation from the camera sensor to the current device orientation.
     */


    fun computeRelativeRotation(
        characteristics: CameraCharacteristics,
        surfaceRotation: Int
    ): Int {
        val sensorOrientationDegrees =
            characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        val deviceOrientationDegrees = when (surfaceRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        // Reverse device orientation for front-facing cameras
        val sign = if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
            CameraCharacteristics.LENS_FACING_FRONT
        ) 1 else -1

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientationDegrees - (deviceOrientationDegrees * sign) + 360) % 360
    }

    fun createFile(context: Context, extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
    }

    fun saveImage(file: File) {
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.insert(uri, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "images/*")
        })

    }

    private fun requestPermission() {

    }
}