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

    private val resNetModule by lazy {
        LiteModuleLoader.load(
            assetFilePath(
                "noopt_resnet50.ptl"
            )
        )
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


    fun runResNet(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true).apply {
            config = Bitmap.Config.ARGB_8888
        }
        val meanArray = arrayOf(0.485F, 0.456F, 0.406F).toFloatArray()
        val stdArray = arrayOf(0.229F, 0.224F, 0.225F).toFloatArray()

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            meanArray,
            stdArray
        )
        val output = resNetModule!!.forward(IValue.from(inputTensor))

        //결과값 텐서 도출
        Log.d(TAG, "Now output ")
//        val outputs = outputTuple[0].toTensor().dataAsFloatArray //결과값이 담긴 배열 가져옴
        Log.d(TAG, output.toTensor().dataAsFloatArray.size.toString())

    }

    companion object {
        //        private val NO_STD_RGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        private val TAG: String = TorchController::class.java.simpleName
//        private val NO_MEAN_RGB = floatArrayOf(0.0f, 0.0f, 0.0f)
    }
}