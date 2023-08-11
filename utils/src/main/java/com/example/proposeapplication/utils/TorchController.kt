package com.example.proposeapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays

class TorchController(private val context: Context) {

    private val mModule by lazy {
        LiteModuleLoader.load(assetFilePath("mobile_csnet.ptl"))

    }

    @Throws(IOException::class)
    fun assetFilePath(assetName: String?): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.applicationContext.assets.open(assetName!!).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    fun analyzeImage(bitmap: Bitmap) {

        //이미지를 224 * 224 으로 리사이징 하고 진행
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 244, 244, true).apply {
            config = Bitmap.Config.ARGB_8888
        }

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            NO_MEAN_RGB,
            NO_STD_RGB
        )
        //입력 텐서를 생성

        Log.d(
            TAG, "Make Input Tensor : ${
                Arrays.toString(inputTensor.shape())
            }"
        )
        val output = mModule!!.forward(IValue.from(inputTensor))

        //결과값 텐서 도출
        Log.d(TAG, "Now output ")
//        val outputs = outputTuple[0].toTensor().dataAsFloatArray //결과값이 담긴 배열 가져옴
        Log.d(TAG, Arrays.toString(output.toTensor().shape()))
    }

    companion object {
        private val NO_STD_RGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        private val TAG: String = TorchController::class.java.simpleName
        private val NO_MEAN_RGB = floatArrayOf(0.0f, 0.0f, 0.0f)
    }
}