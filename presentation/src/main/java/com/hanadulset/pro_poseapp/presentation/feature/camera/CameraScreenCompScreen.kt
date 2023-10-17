package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.OrientationEventListener
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.sqrt

object CameraScreenCompScreen {

    @Composable
    fun CompScreen(
        modifier: Modifier = Modifier, pointOffSet: Offset?, triggerPoint: (DpSize) -> Unit
    ) {
        val localDensity = LocalDensity.current
        val horizontalCheckCircleRadius = 60F
        val compSize = remember {
            mutableStateOf<DpSize?>(null)
        }


        //현재 구도추천 포인트의 위치  -> 아마 애니메이션 넣어줘야 할 듯
        val pointOffsetNow by rememberUpdatedState(newValue = pointOffSet)
        //구도추천 활성화 여부
        val isPointOn = remember { mutableStateOf(false) }
        val current = remember {
            mutableStateOf<Triple<Int, Int, Pair<Int, Float>>?>(null)
        }

        Box(modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                coordinates.size.let {
                    with(localDensity) {
                        compSize.value = DpSize(
                            it.width.toDp(), it.height.toDp()
                        )
                    }
                }
            }) {
            if (compSize.value != null) {
                //구도추천 포인트 표시
                if (isPointOn.value && pointOffsetNow != null) CompGuidePoint(
                    areaSize = compSize.value!!,
                    pointOffSet = pointOffsetNow!!
                )
                else {
                    //흔들림 감지 -> 구도 포인트가 없을 때만, 흔들림을 감지 하기 시작한다.
                    SensorTrigger(onTracking = {
                        isPointOn.value = true
                        triggerPoint(compSize.value!!)
                    },
                        onChangeShake = {
                            current.value = it
                        }
                    )
                    if (current.value != null)
                        current.value!!.let {
                            val distance =
                                sqrt(((it.first * it.first) + (it.second * it.second) + (it.third.first * it.third.first)).toFloat()).toInt()
                            Text(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 100.dp, start = 20.dp),
                                text = "now Sensor distance: $distance, Dt: ${it.third.second} ",
                                fontSize = 10.sp
                            )
                        }

                }

                //수평계
                HorizontalCheckModule(modifier = modifier,
                    centerRadius = horizontalCheckCircleRadius,
                    centroid = with(localDensity) {
                        compSize.value.let {
                            Offset(
                                (it!!.width / 2).toPx(), (it.height / 2).toPx()
                            )
                        }
                    })
            }


        }

    }

    @Composable
    private fun CompGuidePoint(
        modifier: Modifier = Modifier,
        areaSize: DpSize,
        pointOffSet: Offset,
        pointColor: Color = Color(0x80FFFFFF),
        pointRadius: Float = 55F
    ) {

        Box(
            modifier = modifier.size(areaSize)
        ) {
            Canvas(
                modifier = Modifier
            ) {
                drawCircle(
                    center = pointOffSet, radius = pointRadius, color = pointColor
                )
            }
        }
    }

    @Composable
    fun SensorTrigger(
        onTracking: () -> Unit,
        onChangeShake: (Triple<Int, Int, Pair<Int, Float>>) -> Unit,
    ) {
        val context = LocalContext.current

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val SHAKE_THRESHOLD_DISTANCE = 80F * 80F //Threshold
        val comboNonShakingDTThreshold = 0.4F //Dt

        val timestamp = remember {
            mutableFloatStateOf(0.0F)
        }
        val shakeState = remember {
            mutableStateOf(true)
        }

        val value = remember {
            mutableStateOf<Triple<Int, Int, Pair<Int, Float>>?>(null)
        }

        var comboDT = 0F

        val sensorListener = rememberUpdatedState {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_GYROSCOPE -> {
                            val (x, y, z) = event.values
                            val dt =
                                (event.timestamp - timestamp.floatValue) * (1.0F / 1000000000.0F)
                            timestamp.floatValue = event.timestamp.toFloat()
                            if (dt - timestamp.floatValue * (1.0F / 1000000000.0F) != 0F) {
                                val dx = abs(x * dt * 1000)
                                val dy = abs(y * dt * 1000)
                                val dz = abs(z * dt * 1000)
                                //만약 흔들림 방지턱을 넘은 경우
                                val magnitude = dx * dx + dy * dy + dz * dz
                                if (magnitude < SHAKE_THRESHOLD_DISTANCE) {
                                    //흔들린 이후, 연속적으로 흔들리지 않았다는 데이터가 30개 이상 전달된 경우
                                    if (shakeState.value && comboDT >= comboNonShakingDTThreshold) {
                                        shakeState.value = false //흔들리지 않음 상태로 변경한다.
                                        comboDT = 0F//cnt 개수를 초기화 한다.
                                    } else comboDT += dt//흔들리지 않았다는 데이터의 개수를 1증가
                                } else {
                                    if (shakeState.value.not()) shakeState.value = true
                                    //연속적으로 흔들리지 않음 상태를 만족시키지 못했으므로
                                    else comboDT = 0F
                                }
                            }
                        }

                        else -> {
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accurancy: Int) {}
            }
        }

        LaunchedEffect(key1 = shakeState.value) {
            if (shakeState.value.not()) onTracking()
        }
        LaunchedEffect(key1 = value.value) {
            if (value.value != null) onChangeShake(value.value!!)
        }

        LaunchedEffect(Unit) {
            sensorManager.registerListener(
                sensorListener.value(), gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        //리스너 등록
        DisposableEffect(Unit) {
            onDispose {
                sensorManager.unregisterListener(sensorListener.value(), gyroscopeSensor)
            }
        }
    }


    // 수평계 모듈
    @Composable
    fun HorizontalCheckModule(
        modifier: Modifier = Modifier, centerRadius: Float, centroid: Offset
    ) {
        val context = LocalContext.current

        val shortLineLength = 30F

        val rotationState = remember {
            mutableIntStateOf(0)
        }

        val animation = remember {
            Animatable(0F)
        }
        val angleThreshold = 5

        val rotationEventListener = rememberUpdatedState {
            object : OrientationEventListener(context.applicationContext) {
                override fun onOrientationChanged(orientation: Int) {
                    // -1이 나오면 측정을 중지한다.


                    if (orientation != -1) {
                        rotationState.intValue = when (orientation) {
                            in 180 - angleThreshold..180 -> 180
                            in 0..angleThreshold -> 0
                            in 180..angleThreshold + 180 -> 180
                            in 360 - angleThreshold..360 -> 360
                            else -> orientation
                        }
                        Log.d("각도 : ", orientation.toString())
                    }
                }
            }
        }


        LaunchedEffect(Unit) {
            animation.animateTo(
                rotationState.intValue.toFloat(), animationSpec = tween(16, easing = LinearEasing)
            )
        }

        DisposableEffect(Unit) {
            rotationEventListener.value().enable() //시작
            onDispose {
                rotationEventListener.value().disable()//종료
            }
        }

        Canvas(
            modifier = modifier,
        ) {
            val leftEndOffset = Offset((centroid.x - centerRadius), centroid.y)
            val leftStartOffset = Offset(leftEndOffset.x - shortLineLength, centroid.y)
            val rightStartOffset = Offset((centroid.x + centerRadius), centroid.y)
            val rightEndOffset = Offset(rightStartOffset.x + shortLineLength, centroid.y)

            //회전을 감지 한다.
            rotate(
                -rotationState.intValue.toFloat(), pivot = centroid //회전 기준점
            ) {
                val calibrationDegree = -rotationState.intValue
                if (calibrationDegree in listOf(0, 180)
                ) {
                    drawLine(
                        start = leftStartOffset,
                        end = rightEndOffset,
                        strokeWidth = 2F,
                        color = Color(0x90FFFF00)
                    )
                    drawCircle(
                        color = Color(0x90FFFF00),
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 2F
                        )
                    )
                } else {
                    drawLine(
                        Color.White, leftStartOffset, leftEndOffset, strokeWidth = 5F
                    )
                    drawLine(
                        Color.White, rightStartOffset, rightEndOffset, strokeWidth = 5F
                    )
                    drawCircle(
                        color = Color(0x90FFFFFF),
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3F
                        )
                    )
                }


            }
        }
    }


}

@Preview
@Composable
fun PreviewHorizontal() {
    CameraScreenCompScreen.HorizontalCheckModule(
        modifier = Modifier.fillMaxSize(), centerRadius = 30F, centroid = Offset(500F, 500F)
    )
}