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


    //모델을 예열한다.
    override suspend fun preRun() = suspendCoroutine {
        CoroutineScope(Dispatchers.Main).launch {
            vapNetModule = loadModel(VAPNET_FILE)
            it.resume(true)
        }
    }


    override fun runVapNet(bitmap: Bitmap): Pair<Float, Float> {
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
        val suggestionThreshold = 0.9
        val adjustmentThreshold = 0.5
        var res: Pair<Float, Float> = Pair(0F, 0F)
        if (suggestion[0] > suggestionThreshold) { //조정이 필요한 경우
            val magIndex = adjustment.toList().indexOf(adjustment.max())
            val magOutput = magnitude[magIndex]

            if (adjustment.sum() in 0.99F..1.01F) {   //이전 모델 이용시
                res = when (magIndex) {
                    in 0..1 -> {
                        val horizontalMoveRate = magOutput.absoluteValue
                        res.copy(first= if (magIndex == 0) -horizontalMoveRate else horizontalMoveRate)
                    }

                    else -> {
                        val verticalMoveRate = magOutput.absoluteValue
                        res.copy(second = if (magIndex == 2) -verticalMoveRate else verticalMoveRate)
                    }
                }
            } else {//최근 모델 이용시
                // 방향 별 움직인 비율 체크 -> 값이 큰 인덱스를 해당 방향 인덱스로 가짐
                val horizontalMoveIndex = if (adjustment[0] > adjustment[1]) 0 else 1
                val verticalMoveIndex = if (adjustment[2] > adjustment[3]) 2 else 3
                if (adjustment[horizontalMoveIndex] >= adjustmentThreshold)
                    res =
                        res.copy(first = magnitude[horizontalMoveIndex] * (if (horizontalMoveIndex == 0) -1F else 1F))
                if (adjustment[verticalMoveIndex] >= adjustmentThreshold)
                    res =
                        res.copy(second = magnitude[verticalMoveIndex] * (if (verticalMoveIndex == 2) -1F else 1F))
            }
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
        private val RESNET_INPUT_SIZE = Size(224.0, 224.0)
        private const val VAPNET_FILE = "vapnet.ptl"
    }

}