package com.example.proposeapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.provider.ContactsContract.CommonDataKinds.Im
import androidx.core.graphics.scale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.example.proposeapplication.utils.pose.PoseRecommendControllerImpl
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TorchController(private val context: Context) {

    private val resNetModule by lazy {
        LiteModuleLoader.load(
            assetFilePath(
                "noopt_resnet50.ptl" //모델 파일 이름
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

    //리사이징 메소드
    fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap {
        val inputImageMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputResizeBitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.bitmapToMat(bitmap, inputImageMat)
        Imgproc.cvtColor(inputImageMat,inputImageMat,Imgproc.COLOR_RGBA2RGB) //알파값을 빼고 저장
        Imgproc.resize(inputImageMat, inputImageMat, size)
        Utils.matToBitmap(inputImageMat, outputResizeBitmap)
        return outputResizeBitmap
    }

    fun runResNet(bitmap: Bitmap): FloatArray {
        val resizedBitmap = resizeBitmapWithOpenCV(bitmap, Size(224.0, 224.0))

        val meanArray = arrayOf(0.485F, 0.456F, 0.406F).toFloatArray()
        val stdArray = arrayOf(0.229F, 0.224F, 0.225F).toFloatArray()

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            meanArray,
            stdArray
        )


        val output = resNetModule!!.forward(IValue.from(inputTensor))
        return output.toTensor().dataAsFloatArray
    }

    companion object {
        //        private val NO_STD_RGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        private val TAG: String = TorchController::class.java.simpleName
//        private val NO_MEAN_RGB = floatArrayOf(0.0f, 0.0f, 0.0f)
    }
}