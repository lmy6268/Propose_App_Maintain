package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.view.OrientationEventListener
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

object CameraScreenCompScreen {

    @Composable
    fun CompScreen(
        modifier: Modifier = Modifier,
        pointOffSet: DpOffset?,

    ) {
        val localDensity = LocalDensity.current
        val horizontalCheckCircleRadius = 30F
        val compSize = remember {
            mutableStateOf<DpSize?>(null)
        }


        //현재 구도추천 포인트의 위치  -> 아마 애니메이션 넣어줘야 할 듯
        val pointOffsetNow by rememberUpdatedState(newValue = pointOffSet)
        //구도추천 활성화 여부
        val isPointOn = remember { mutableStateOf(false) }


        Box(modifier = modifier.onGloballyPositioned { coordinates ->
            coordinates.size.let {
                with(localDensity) {
                    compSize.value = DpSize(
                        it.width.toDp(),
                        it.height.toDp()
                    )
                }
            }
        }) {
            if (compSize.value != null) {
                //흔들림 감지 -> 구도 포인트가 없을 때만, 흔들림을 감지 하기 시작한다.

                //구도추천 포인트 표시
                if (isPointOn.value && pointOffsetNow != null) CompGuidePoint(
                    areaSize = compSize.value!!, pointOffSet = pointOffsetNow!!
                )

                //수평계
                HorizontalCheckModule(modifier = modifier,
                    centerRadius = horizontalCheckCircleRadius,
                    centroid = with(localDensity) {
                        compSize.value.let {
                            Offset(
                                (it!!.width / 2).toPx(),
                                (it.height / 2).toPx()
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
        pointOffSet: DpOffset,
        pointColor: Color = Color.White,
        pointRadius: Float = 30F
    ) {
        val localDensity = LocalDensity.current
        val dpPointOffset by rememberUpdatedState(newValue = pointOffSet)


        Box(
            modifier = modifier.size(areaSize)
        ) {
            Canvas(modifier = Modifier.offset(dpPointOffset.x, dpPointOffset.y)) {
                drawCircle(
                    center = with(localDensity) {
                        dpPointOffset.let {
                            Offset(
                                it.x.toPx(), it.y.toPx()
                            )
                        }
                    }, radius = pointRadius, color = pointColor
                )
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
            mutableFloatStateOf(-1F)
        }

        val animation = remember {
            Animatable(0F)
        }
        val angleThreshold = 5F

        val rotationEventListener = rememberUpdatedState {
            object : OrientationEventListener(context.applicationContext) {
                override fun onOrientationChanged(orientation: Int) {
                    // -1이 나오면 측정을 중지한다.
                    if (orientation != -1) {
                        rotationState.floatValue = when (orientation.toFloat()) {
                            in 180F - angleThreshold..180F -> -180F
                            in 0F..angleThreshold -> -0F
                            in 360F - angleThreshold..360F -> -360F
                            else -> -orientation.toFloat()
                        }
                    }
                }
            }
        }


        LaunchedEffect(Unit) {
            animation.animateTo(
                rotationState.floatValue, animationSpec = tween(16, easing = LinearEasing)
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
                rotationState.floatValue, pivot = centroid //회전 기준점
            ) {
                if (rotationState.floatValue == 0.0F || rotationState.floatValue in listOf(
                        -180F, 180F
                    )
                ) {
                    drawLine(
                        start = leftStartOffset,
                        end = rightEndOffset,
                        strokeWidth = 8F,
                        color = Color.Yellow
                    )
                    drawCircle(
                        color = Color.Yellow,
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3.dp.toPx()
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
                        color = Color.White,
                        radius = centerRadius,
                        center = centroid,
                        style = Stroke(
                            width = 3.dp.toPx()
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