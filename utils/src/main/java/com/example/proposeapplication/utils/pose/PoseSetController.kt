package com.example.proposeapplication.utils.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.example.proposeapplication.utils.R
import com.example.proposeapplication.utils.TorchController
import org.opencv.core.Size
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Arrays
import java.util.stream.Collectors


class PoseSetController(private val context: Context) {
    private val torchController by lazy {
        TorchController(context)
    }

    //팔레트
    private val palette by lazy {
        context.assets.open("color_palette.json").use { `is` ->
            val str = BufferedReader(InputStreamReader(`is`)).lines()
                .collect(Collectors.joining())
            str.replace(" ", "").let { input ->
                val cleanedInput = input
                    .replace("[[", "[")
                    .replace("],[", "]@[")
                    .replace("]]", "]")
                val stringArrays = cleanedInput.split("@").toTypedArray()

                val array = Array(stringArrays.size) { idx ->
                    stringArrays[idx]
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.toInt() }
                        .toTypedArray()
                }
                array
            }
        }
    }


    fun getPosePosition(rawBitmap: Bitmap): DoubleArray {
        val (mScaleSize, outputArray) = torchController.runYolo(rawBitmap)
        val yoloResult = outputsToNMSPredictions(mScaleSize, outputArray)
        val layoutImageBitmap = makeLayoutImage(yoloResult)
        return torchController.runBbPrediction(
            rawBitmap,
            layoutImageBitmap
        ) //centerx, centery, width, height (모두 0~1)
    }

    //2. 레이아웃 이미지 생성
    private fun makeLayoutImage(yoloResult: ArrayList<PredictResult>) =
        Bitmap.createBitmap(
            480,
            480,
            Bitmap.Config.ARGB_8888
        )
            .apply {
                eraseColor(
                    Color.rgb(
                        palette[0][0],
                        palette[0][1],
                        palette[0][2]
                    )
                )//a. 480*480 이미지를 color palette의 0번째 색으로 생성
                val initPixelData =
                    MutableList<MutableList<MutableList<Int>>>(480) { MutableList(480) { mutableListOf() } }

                //Yolo 결과값을 이용하여 레이아웃 이미지를 칠한다.
                yoloResult.forEach { predictResult ->
                    val rect = Rect(
                        (0.75 * predictResult.box.left).toInt(),
                        (0.75 * predictResult.box.top).toInt(),
                        (0.75 * predictResult.box.right).toInt(),
                        (0.75 * predictResult.box.bottom).toInt()
                    )
                    val targetColorIdx = predictResult.classIdx + 1
                    for (y in rect.top until rect.bottom) {
                        for (x in rect.left until rect.right) {
                            //색 값 저장
                            initPixelData[y][x].add(palette[targetColorIdx].let { rgb ->
                                Color.rgb(rgb[0], rgb[1], rgb[2])
                            })
                            //평균 내어 값을 저장
                            val avgColor = initPixelData[y][x].sum() / initPixelData[y][x].size
                            //평균 색을 칠함
                            this.setPixel(x, y, avgColor)
                        }
                    }
                }

            }


    private fun outputsToNMSPredictions(
        scaleSize: Size,
        outputs: FloatArray
    ): ArrayList<PredictResult> {
        val resultDataList = ArrayList<PredictResult>()
        for (i in 0 until YOLO_OUTPUT_ROW) {
            if (outputs[i * YOLO_OUTPUT_COLUMN + 4] > YOLO_THRESHOLD) {
                val (x, y) = Pair(
                    outputs[i * YOLO_OUTPUT_COLUMN],
                    outputs[i * YOLO_OUTPUT_COLUMN + 1]
                )
                val (w, h) = Pair(
                    outputs[i * YOLO_OUTPUT_COLUMN + 2],
                    outputs[i * YOLO_OUTPUT_COLUMN + 3]
                )

                val rect = Rect(
                    (scaleSize.width * (x - w / 2)).toInt(),
                    (scaleSize.height * (y - h / 2)).toInt(),
                    (scaleSize.width * (x + w / 2)).toInt(),
                    (scaleSize.height * (y + h / 2)).toInt(),
                )
                var maxValue = outputs[i * YOLO_OUTPUT_COLUMN + 5]
                var clsIdx = 0
                for (j in 0 until YOLO_OUTPUT_COLUMN - 5) {
                    if (outputs[i * YOLO_OUTPUT_COLUMN + 5 + j] > maxValue) {
                        maxValue = outputs[i * YOLO_OUTPUT_COLUMN + 5 + j]
                        clsIdx = j
                    }
                }

                resultDataList.add(
                    PredictResult(
                        classIdx = clsIdx,
                        score = outputs[i * YOLO_OUTPUT_COLUMN + 4],
                        box = rect
                    )
                )
            }
        }
        return nonMaxSuppression(resultDataList)
    }


    // The two methods nonMaxSuppression and IOU below are ported
// from https://github.com/hollance/YOLO-CoreML-MPSNNGraph/blob/master/Common/Helpers.swift
    private fun nonMaxSuppression(
        boxes: ArrayList<PredictResult>
    ): ArrayList<PredictResult> {

        // Do an argsort on the confidence scores, from high to low.
        boxes.sortWith { o1, o2 -> o1.score.compareTo(o2.score) }
        val selected: ArrayList<PredictResult> = ArrayList()
        val active = BooleanArray(boxes.size)
        Arrays.fill(active, true)
        var numActive = active.size

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        var done = false
        var i = 0
        while (i < boxes.size && !done) {
            if (active[i]) {
                val boxA: PredictResult = boxes[i]
                selected.add(boxA)
                if (selected.size >= YOLO_NMS_LIMIT) break
                for (j in i + 1 until boxes.size) {
                    if (active[j]) {
                        val boxB: PredictResult = boxes[j]
                        if (IOU(boxA.box, boxB.box) > YOLO_THRESHOLD) {
                            active[j] = false
                            numActive -= 1
                            if (numActive <= 0) {
                                done = true
                                break
                            }
                        }
                    }
                }
            }
            i++
        }
        return selected
    }

    private fun IOU(a: Rect, b: Rect): Float {
        val areaA = ((a.right - a.left) * (a.bottom - a.top)).toFloat()
        if (areaA <= 0.0) return 0.0f
        val areaB = ((b.right - b.left) * (b.bottom - b.top)).toFloat()
        if (areaB <= 0.0) return 0.0f
        val intersectionMinX = a.left.coerceAtLeast(b.left).toFloat()
        val intersectionMinY = a.top.coerceAtLeast(b.top).toFloat()
        val intersectionMaxX = a.right.coerceAtMost(b.right).toFloat()
        val intersectionMaxY = a.bottom.coerceAtMost(b.bottom).toFloat()
        val intersectionArea =
            (intersectionMaxY - intersectionMinY).coerceAtLeast(0f) * (intersectionMaxX - intersectionMinX).coerceAtLeast(
                0f
            )
        return intersectionArea / (areaA + areaB - intersectionArea)
    }

    data class PredictResult(
        val classIdx: Int, val score: Float, val box: Rect
    )

    companion object {
        private const val YOLO_OUTPUT_ROW = 25200
        private const val YOLO_OUTPUT_COLUMN = 85
        private const val YOLO_THRESHOLD = 0.3F
        private const val YOLO_NMS_LIMIT = 15
    }
}