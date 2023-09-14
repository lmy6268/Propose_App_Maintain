package com.example.proposeapplication.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.proposeapplication.data.datasource.interfaces.ModelRunner
import org.opencv.core.Size
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

class ModelRunnerImpl(private val context: Context) : ModelRunner {

    private lateinit var resNetModule: Module
    private lateinit var yoloModule: Module
    private lateinit var bbPredictionModule: Module
    private lateinit var vapNetModule: Module


    private val imageProcessDataSource by lazy {
        ImageProcessDataSourceImpl()
    }

    //path를 알면 로드할 수 있음 .
    override fun loadModel(moduleAssetName: String): Module {
        val file = File(context.dataDir, moduleAssetName)
        val path =
//            if (file.exists() && file.length() > 0)
            file.absolutePath
//        else context.assets.open(moduleAssetName).use { `is` ->
//            FileOutputStream(file).use { os ->
//                val buffer = ByteArray(4 * 1024)
//                var read: Int
//                while (`is`.read(buffer).also { read = it } != -1) {
//                    os.write(buffer, 0, read)
//                }
//                os.flush()
//            }
//            file.absolutePath
//        }
        return LiteModuleLoader.load(path)
    }


    override fun runResNet(bitmap: Bitmap): FloatArray {
        val resizedBitmap = imageProcessDataSource.resizeBitmapWithOpenCV(bitmap, RESNET_INPUT_SIZE)

        val meanArray = arrayOf(0.485F, 0.456F, 0.406F).toFloatArray()
        val stdArray = arrayOf(0.229F, 0.224F, 0.225F).toFloatArray()

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap, meanArray, stdArray
        )
        val output = resNetModule.forward(IValue.from(inputTensor))
        return output.toTensor().dataAsFloatArray
    }

    // [Bounding Box Prediction] 을 진행한다.
    override fun runBbPrediction(originBitmap: Bitmap, layoutBitmap: Bitmap): DoubleArray {
        //originBitmap을 480*480으로 리사이징 한다.
        val resizedOriginBitmap =
            imageProcessDataSource.resizeBitmapWithOpenCV(originBitmap, Size(480.0, 480.0))
        //Input Tensor를 만든다.
        val resizeOriginInputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedOriginBitmap, NO_MEAN_RGB, NO_STD_RGB
        )
        val layoutInputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            layoutBitmap, NO_MEAN_RGB, NO_STD_RGB
        )

        //모델에 추론을 시킨다.
        val output = bbPredictionModule.forward(
            IValue.from(resizeOriginInputTensor), IValue.from(layoutInputTensor)
        )
        //결과값을 반환한다.
        val outputTensor = output.toTensor()
        return outputTensor.dataAsDoubleArray
    }

    //YOLO모델을 실행한다.
    override fun runYolo(bitmap: Bitmap): Pair<Size, FloatArray> {
        val mScaleSize = Size(
            bitmap.width.toFloat() / YOLO_INPUT_SIZE.width,
            bitmap.height.toFloat() / YOLO_INPUT_SIZE.height
        )

        val resizedBitmap = imageProcessDataSource.resizeBitmapWithOpenCV(bitmap, YOLO_INPUT_SIZE)
//            Bitmap.createScaledBitmap(
//            bitmap, YOLO_INPUT_SIZE.width.toInt(), YOLO_INPUT_SIZE.height.toInt(), true )
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap, NO_MEAN_RGB, NO_STD_RGB
        )
        val outputTuple = yoloModule.forward(IValue.from(inputTensor)).toTuple()
        val outputTensor = outputTuple[0].toTensor()
        val outputs = outputTensor.dataAsFloatArray
// **When Using v7 Model**
//        val outputs = emptyList<Float>().toMutableList().apply {
//            outputTensor.toTensorList().forEach {
//                addAll(it.dataAsFloatArray.toList())
//            }
//        }.toFloatArray()
        return Pair(mScaleSize, outputs)
    }

    //모델을 예열한다.
    override fun preRun() {
        resNetModule = loadModel("model_resnet.ptl")
        yoloModule = loadModel("model_yolov5s.ptl")
        bbPredictionModule = loadModel("model_bbprediction_dqlite.ptl")
        vapNetModule = loadModel("vapnet.ptl")
    }

    override fun runVapNet(bitmap: Bitmap) {
        val resizedBitmap = imageProcessDataSource.resizeBitmapWithOpenCV(bitmap, RESNET_INPUT_SIZE)

        val meanArray = arrayOf(0.485F, 0.456F, 0.406F).toFloatArray()
        val stdArray = arrayOf(0.229F, 0.224F, 0.225F).toFloatArray()

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap, meanArray, stdArray
        )
        val output = vapNetModule
            .forward(IValue.from(inputTensor))
        val outputTuple = output.toTuple()
        val (suggestion, adjustment, magnitude) = Triple<FloatArray, FloatArray, FloatArray>(
            outputTuple[0].toTensor().dataAsFloatArray,
            outputTuple[1].toTensor().dataAsFloatArray,
            outputTuple[2].toTensor().dataAsFloatArray
        )
        Log.d("VapNet Result : ", "${suggestion.toList()} , ${adjustment.toList()}, ${magnitude.toList()}")
//        val threshold = 0.98
//        if (suggestion[0] > threshold) {
//            val idx = adjustment.toList().indexOf(adjustment.max())
//            val magOutPut = magnitude[idx]
//            Log.d("VapNet Result : ", "idx - $idx, magOutPut - $magOutPut")
//        }
    }


    companion object {
        private val YOLO_INPUT_SIZE = Size(640.0, 640.0) //리사이징 사이즈
        private val RESNET_INPUT_SIZE = Size(224.0, 224.0)
        private val NO_STD_RGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        private val TAG: String = ModelRunnerImpl::class.java.simpleName
        private val NO_MEAN_RGB = floatArrayOf(0.0f, 0.0f, 0.0f)

        /** 모델을 다운받을 위치
         * */
        private const val MODEL_URL = ""
    }

}