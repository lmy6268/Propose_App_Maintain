package com.hanadulset.pro_poseapp.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.hanadulset.pro_poseapp.data.datasource.interfaces.ModelRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Size
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
        val path = if (file.exists() && file.length() > 0) file.absolutePath
        else context.assets.open(moduleAssetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            file.absolutePath
        }
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
    override suspend fun preRun() = suspendCoroutine {
        CoroutineScope(Dispatchers.Main).launch {
            resNetModule = loadModel("model_resnet.ptl")
//            yoloModule = loadModel("model_yolov5s.ptl")
//            bbPredictionModule = loadModel("model_bbprediction_dqlite.ptl")
            vapNetModule = loadModel("vapnet.ptl")
            it.resume(true)
        }
    }


    override fun runVapNet(bitmap: Bitmap): Pair<String, Int> {
        val resizedBitmap = imageProcessDataSource.resizeBitmapWithOpenCV(bitmap, RESNET_INPUT_SIZE)

        val meanArray = arrayOf(0.485F, 0.456F, 0.406F).toFloatArray()
        val stdArray = arrayOf(0.229F, 0.224F, 0.225F).toFloatArray()

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap, meanArray, stdArray
        )
        val output = vapNetModule.forward(IValue.from(inputTensor))
        val outputTuple = output.toTuple()
        val (suggestion, adjustment, magnitude) = Triple<FloatArray, FloatArray, FloatArray>(
            outputTuple[0].toTensor().dataAsFloatArray,
            outputTuple[1].toTensor().dataAsFloatArray,
            outputTuple[2].toTensor().dataAsFloatArray
        )
        val threshold = 0.65
        val list = listOf("Left", "Right", "Up", "Down")

        val res = if (suggestion[0] > threshold) { //조정이 필요한 경우
            val idx = adjustment.toList().indexOf(adjustment.max())
            val magOutPut = magnitude[idx]
            Log.d(
                "test data: ",
                "Move to ${list[idx]}, ${(magOutPut.absoluteValue * 100).roundToInt()}% \n (suggestion:${suggestion.toList()[0]})"
            )
            when (idx) {
                in 0..1 -> {
                    val value = (magOutPut.absoluteValue * 100).roundToInt()
                    Pair(
                        "horizon",
                        if (idx == 0) -value else value
                    ) //Left인경우, 중심 기준으로 - 이기 때문에 -를 붙여준다.
                }

                else -> {
                    val value = (magOutPut.absoluteValue * 100).roundToInt()
                    Pair(
                        "vertical",
                        if (idx == 2) -value else value
                    ) //UP 인 경우, 좌표상으로는 -이므로, 앞에 -를 붙여준다
                }
            }


        } else {
            Pair("good", 0)
        }

        Log.d("구도추천 결과:", res.toString())

        return res
    }

    fun convert1DTo3D(
        inputArray: FloatArray, depth: Int, rows: Int, cols: Int
    ): Array<Array<FloatArray>> {
        val result = Array(depth) { Array(rows) { FloatArray(cols) } }

        for (i in 0 until depth) {
            for (j in 0 until rows) {
                for (k in 0 until cols) {
                    result[i][j][k] = inputArray[i * rows * cols + j * cols + k]
                }
            }
        }

        return result
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