package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.util.SizeF
import android.view.MotionEvent
import androidx.camera.core.MeteringPoint
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Scale
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.delay

object CameraScreenPreviewArea {

    //미리보기 영역
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PreviewArea(
        modifier: Modifier = Modifier,
        poseData: PoseData? = null,
        poseScale: Float,
        isCaptured: Boolean,
        preview: PreviewView,
        edgeImageBitmap: Bitmap?,
        poseOffset: SizeF?,
        isRecommendCompEnabled: Boolean,
        isRecommendPoseEnabled: Boolean,
        loadLastImage: () -> Unit,
        upperBarSize: DpSize,
        pointerOffset: Offset?,
        initCamera: () -> Unit,
        onFocusEvent: (Pair<MeteringPoint, Long>) -> Unit,
        triggerNewPoint: (DpSize) -> Unit,
        onStopCaptureAnimation: () -> Unit,
        onStopTrackPoint: () -> Unit,
        onPoseChangeOffset: (SizeF) -> Unit,
        onPointMatched: (Boolean) -> Unit,
        onLimitMaxScale: (Float) -> Unit
    ) {
        val localDensity = LocalDensity.current

        val previewView by rememberUpdatedState(newValue = preview)
        val compSwitchValue by rememberUpdatedState(newValue = isRecommendCompEnabled)
        val poseSwitchValue by rememberUpdatedState(newValue = isRecommendPoseEnabled)

        //외부로 부터 받은 값이 더이상 변하지 않는 경우
        val upBarSize by remember { mutableStateOf(upperBarSize) }

        val previewViewSize = rememberSaveable { mutableStateOf(SizeF(0F, 0F)) }
        val pointOffset by rememberUpdatedState(newValue = pointerOffset)

        //카메라 촬영 시, 촬영 Effect
        val flashColor by animateColorAsState(
            targetValue = if (isCaptured) Color.White else Color.Unspecified,
            animationSpec = tween(150, 0, easing = LinearEasing),
            finishedListener = { if (isCaptured) onStopCaptureAnimation() },
            label = ""
        )


        //포커스링 위치
        val focusRingState = remember { mutableStateOf<Offset?>(null) }

        //포커스링 활성화 시점
        LaunchedEffect(key1 = focusRingState.value) {
            if (focusRingState.value != null) {
                delay(400)
                focusRingState.value = null
            }
        }


        Box(modifier) {
            //미리보기
            AndroidView(
                modifier = modifier
                    .animateContentSize { _, _ -> }
                    .onSizeChanged {
                        previewViewSize.value = localDensity.run {
                            SizeF(it.width.toDp().value, it.height.toDp().value)
                        }
                    }
                    .pointerInteropFilter(RequestDisallowInterceptTouchEvent()) { motionEvent -> //여기서 포커스 링을 세팅하는데, 여기서 문제가 생긴 것 같다.
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                focusRingState.value = null
                                val untouchableArea = with(localDensity) { upBarSize.height.toPx() }
                                if (motionEvent.y > untouchableArea) {
                                    val pointer = Offset(motionEvent.x, motionEvent.y)
                                    focusRingState.value = pointer.copy()
                                    onFocusEvent(
                                        Pair(
                                            previewView.meteringPointFactory.createPoint(
                                                pointer.x, pointer.y
                                            ), 2000L
                                        )
                                    )
                                }
                                false
                            }

                            else -> {
                                false
                            }
                        }
                    },
                factory = {
                    loadLastImage()
                    initCamera()
                    previewView
                },
            ) {

            }
            //플래시 화면
            Box(
                modifier = modifier
                    .size(DpSize(previewViewSize.value.width.dp, previewViewSize.value.height.dp))
                    .background(color = flashColor)
            )

            //엣지 화면
            ShowEdgeImage(
                modifier = modifier.size(
                    DpSize(
                        previewViewSize.value.width.dp,
                        previewViewSize.value.height.dp
                    )
                ),
                capturedEdgesBitmap = edgeImageBitmap
            )
            //구도 추천
            if (compSwitchValue) CameraScreenCompScreen.CompScreen(
                modifier = modifier.size(
                    DpSize(
                        previewViewSize.value.width.dp,
                        previewViewSize.value.height.dp
                    )
                ),
                pointOffSet = pointOffset,
                triggerPoint = triggerNewPoint,
                stopToTracking = onStopTrackPoint,
                onPointMatched = onPointMatched
            )


            //포즈 화면 구성 -> 포즈 값만 있어도 됨.
            if (poseData != null && poseSwitchValue) {
                ShowingPoseScreen(
                    modifier = modifier.requiredSize(
                        DpSize(
                            previewViewSize.value.width.dp,
                            previewViewSize.value.height.dp
                        )
                    ),
                    poseData = poseData,
                    poseScale = poseScale,
                    onChangeOffset = onPoseChangeOffset,
                    inputPoseOffset = poseOffset,
                    onLimitMaxScale = onLimitMaxScale
                )
            }

            //포커스 위치
            if (focusRingState.value != null) FocusRing(
                modifier = modifier.zIndex(2F),
                color = Color.White,
                pointer = focusRingState.value!!
            )
        }


    }


    @Composable
    private fun FocusRing(
        modifier: Modifier = Modifier, color: Color, pointer: Offset
    ) {

        val animationSize = remember {
            AnimationState(150F)
        }
        val rectSize = Size(animationSize.value, animationSize.value)
        LaunchedEffect(animationSize) {
            animationSize.animateTo(
                targetValue = 100F, animationSpec = tween(durationMillis = 200)
            )
        }
        Canvas(
            modifier = modifier.size(animationSize.value.dp)
        ) {
            drawRect(
                color = color, topLeft = pointer.copy(
                    pointer.x - rectSize.width / 2, pointer.y - rectSize.height / 2
                ), size = rectSize, style = Stroke(
                    width = 3.dp.toPx()
                )
            )
            drawCircle(color, radius = 5F, center = pointer)
        }


    }

    //고정된 화면을 보여주는 컴포저블
    @Composable
    fun ShowEdgeImage(
        capturedEdgesBitmap: Bitmap?, // 고정된 이미지
        modifier: Modifier = Modifier, //수정자
    ) {
        if (capturedEdgesBitmap != null) {
            Image(
                modifier = modifier.alpha(0.5F),
                bitmap = capturedEdgesBitmap.asImageBitmap(),
                contentDescription = "Edge Image"
            )
        }

    }

    @Composable
    fun ShowingPoseScreen(
        modifier: Modifier = Modifier,
        poseData: PoseData,
        poseScale: Float,
        inputPoseOffset: SizeF?,
        onLimitMaxScale: (Float) -> Unit,
        onChangeOffset: (SizeF) -> Unit //오프셋을 이동시키면, 이 메소드가 실행됨
    ) {
        val localDensity = LocalDensity.current
        //현재 포즈 아이템
        val currentItem by rememberUpdatedState(newValue = poseData)
        //미리보기 사이즈
        val boxSize = rememberSaveable {
            mutableStateOf(SizeF(0F, 0F))
        }
        //미리보기를 채우는 박스
        Box(
            modifier =
            modifier.onSizeChanged {
                boxSize.value = SizeF(it.width.toFloat(), it.height.toFloat())
            }
        ) {
            AnimatedVisibility(
                visible = boxSize.value.width > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                //여기 내부에서 포즈아이템이 돌아다니게 됨.
                AnimatedContent(
                    targetState = currentItem,
                    transitionSpec = {
                        ContentTransform(
                            initialContentExit = fadeOut(),
                            targetContentEnter = fadeIn()
                        )
                    },
                    label = "",
                ) { poseItem ->
                    //이미지 기본크기
                    val poseItemSize = remember {
                        localDensity.run {
                            mutableStateOf(
                                Size(
                                    (boxSize.value.width * poseItem.sizeRate.width),
                                    (boxSize.value.height * poseItem.sizeRate.height)
                                )
                            )
                        }
                    }
                    val poseTopLeftOffset = remember {
                        localDensity.run {
                            mutableStateOf(
                                if (inputPoseOffset != null) Offset(
                                    inputPoseOffset.width,
                                    inputPoseOffset.height
                                )
                                else Offset(
                                    boxSize.value.width * poseItem.centerRate.width - poseItemSize.value.width / 2,
                                    boxSize.value.height * poseItem.centerRate.height - poseItemSize.value.height / 2
                                )
                            )
                        }
                    }
                    //아직 어색함
                    val poseBottomRightOffset = remember {
                        localDensity.run {
                            mutableStateOf(
                                if (inputPoseOffset != null) Offset(
                                    inputPoseOffset.width + poseItemSize.value.width,
                                    inputPoseOffset.height + poseItemSize.value.height
                                )
                                else Offset(
                                    boxSize.value.width * poseItem.centerRate.width + poseItemSize.value.width / 2,
                                    boxSize.value.height * poseItem.centerRate.height + poseItemSize.value.height / 2
                                )
                            )
                        }
                    }


                    LaunchedEffect(key1 = Unit) {
                        poseTopLeftOffset.value = poseTopLeftOffset.value.run {
                            Offset(
                                x.coerceIn(0f, boxSize.value.width),
                                y.coerceIn(0f, boxSize.value.height)
                            )
                        }
                    }

                    //포즈 이미지 페인터
                    val painter by rememberUpdatedState(newValue = poseItem.imageUri?.run {
                        rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current).data(this).size(
                                coil.size.Size(
                                    Dimension(
                                        poseItemSize.value.width.toInt()
                                    ), Dimension(
                                        poseItemSize.value.height.toInt()
                                    )
                                )
                            ).scale(Scale.FIT).build()
                        )
                    })
                    val scaleOfPose by rememberUpdatedState(newValue = poseScale)

                    val calculateMaxScale: () -> Float = {
                        val nowPoseTopLeftOffset = poseTopLeftOffset.value
                        val originPoseSize = poseItemSize.value
                        val maxSize = SizeF(
                            boxSize.value.width - nowPoseTopLeftOffset.x,
                            boxSize.value.height - nowPoseTopLeftOffset.y
                        )
                        val resultValue = floatArrayOf(
                            maxSize.width / originPoseSize.width,
                            maxSize.height / originPoseSize.height
                        ).min()
                        resultValue
                    }

                    //포즈 아이템에 대한 설정을 진행한다.
                    // 필요한 설정 : 기본 이미지 사이즈, offset 이동 처리,
                    PoseItem(modifier = Modifier
                        .size(localDensity.run {
                            DpSize(
                                poseItemSize.value.width.toDp(),
                                poseItemSize.value.height.toDp()
                            )
                        })
                        .then(
                            (painter?.state as? AsyncImagePainter.State.Success)
                                ?.painter
                                ?.intrinsicSize
                                ?.let { intrinsicSize ->
                                    Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                                } ?: Modifier
                        )
                        .onSizeChanged {
                            poseItemSize.value = Size(
                                it.width.toFloat(),
                                it.height.toFloat()
                            )
                        }
                        .offset {
                            onLimitMaxScale(calculateMaxScale())
                            poseTopLeftOffset.value.run { IntOffset(x.toInt(), y.toInt()) }
                        }
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0f)
                            localDensity.run {
                                //Top_left 기준 픽셀 오프셋 -> 이걸 가지고 중심점 값과 잘 대비 및 분류해두자.
                                //스케일 관련
                                transformOrigin = TransformOrigin(0f, 0f) //top-left 기준으로 사이즈를 늘려나가자.
                                scaleX = scaleOfPose
                                scaleY = scaleOfPose
                            }
                        }
                        .pointerInput(Unit) {
                            //드래그를 인식하고 반영한다. -> dragAmount 만큼 이동
                            detectDragGestures { change, dragAmount ->
                                val checkOffset = dragAmount * scaleOfPose + poseTopLeftOffset.value
                                poseTopLeftOffset.value = checkOffset.run {
                                    Offset(
                                        x.coerceIn(
                                            0f,
                                            boxSize.value.width - (poseItemSize.value.width * scaleOfPose)
                                        ), y.coerceIn(
                                            0f,
                                            boxSize.value.height - (poseItemSize.value.height * scaleOfPose)
                                        )
                                    )
                                }
                                onChangeOffset(poseTopLeftOffset.value.run { SizeF(x, y) })
                            }
                        },
                        painter = painter
                    )

//                    Canvas(Modifier.fillMaxSize()) {
//                        drawCircle(
//                            center = poseTopLeftOffset.value,
//                            color = Color.Black,
//                            radius = 5F
//                        )
//                    }
                }
            }
        }


    }

    @Composable
    fun PoseItem(
        modifier: Modifier = Modifier, painter: Painter? = null
    ) {
        painter.run {
            if (this == null) Box(modifier = modifier)
            else Image(
                painter = this,
                modifier = modifier,
                contentScale = ContentScale.Fit,
                contentDescription = ""
            )
        }
    }
}


