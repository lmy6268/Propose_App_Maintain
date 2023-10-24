package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.util.Log
import android.util.SizeF
import android.view.MotionEvent
import androidx.camera.core.MeteringPoint
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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
        isRecommendCompEnabled: Boolean,
        loadLastImage: () -> Unit,
        upperBarSize: DpSize,
        pointerOffset: Offset?,
        initCamera: () -> Unit,
        onFocusEvent: (Pair<MeteringPoint, Long>) -> Unit,
        triggerNewPoint: (DpSize) -> Unit,
        onStopCaptureAnimation: () -> Unit,
        onStopTrackPoint: () -> Unit
    ) {
        val localDensity = LocalDensity.current

        val previewView by rememberUpdatedState(newValue = preview)
        val compSwitchValue by rememberUpdatedState(newValue = isRecommendCompEnabled)


        //외부로 부터 받은 값이 더이상 변하지 않는 경우
        val upBarSize by remember {
            mutableStateOf(upperBarSize)
        }

        val previewViewSize = remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
        val pointOffset by rememberUpdatedState(
            newValue = pointerOffset
        )

        //카메라 촬영 시, 촬영 Effect
        val flashColor by animateColorAsState(
            targetValue = if (isCaptured) Color.White else Color.Unspecified,
            animationSpec = tween(150, 0, easing = LinearEasing),
            finishedListener = { if (isCaptured) onStopCaptureAnimation() }, label = ""
        )


        //포커스링 위치
        val focusRingState = remember { mutableStateOf<Offset?>(null) }

        //포커스링 활성화 시점
        LaunchedEffect(key1 = focusRingState.value)
        {
            if (focusRingState.value != null) {
                delay(400)
                focusRingState.value = null
            }
        }

        Box(
            modifier = modifier
        )
        {
            //미리보기
            AndroidView(
                modifier = modifier
                    .animateContentSize { _, _ -> }
                    .onSizeChanged {
                        previewViewSize.value = localDensity.run {
                            DpSize(it.width.toDp(), it.height.toDp())
                        }
                    }
                    .pointerInteropFilter(RequestDisallowInterceptTouchEvent()) { motionEvent -> //여기서 포커스 링을 세팅하는데, 여기서 문제가 생긴 것 같다.
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                focusRingState.value = null

                                val untouchableArea =
                                    with(localDensity) { upBarSize.height.toPx() }
                                if (motionEvent.y > untouchableArea) {
                                    val pointer = Offset(motionEvent.x, motionEvent.y)
                                    focusRingState.value = pointer.copy()
                                    onFocusEvent(
                                        Pair(
                                            previewView.meteringPointFactory.createPoint(
                                                pointer.x,
                                                pointer.y
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
                    .size(previewViewSize.value)
                    .background(color = flashColor)
            )

            //엣지 화면
            ShowEdgeImage(
                modifier = modifier
                    .size(previewViewSize.value),
                capturedEdgesBitmap = edgeImageBitmap
            )
            //구도 추천
            if (compSwitchValue)
                CameraScreenCompScreen.CompScreen(
                    modifier = modifier.size(previewViewSize.value),
                    pointOffSet = pointOffset,
                    triggerPoint = triggerNewPoint,
                    stopToTracking = onStopTrackPoint
                )


            //포즈 화면 구성 -> 포즈 값만 있어도 됨.
            if (poseData != null) {
                PoseScreen(
                    modifier = modifier.size(previewViewSize.value),
                    parentSize = previewViewSize.value,
                    poseData = poseData,
                    poseScale = poseScale,
                )
            }
            //포커스 위치
            if (focusRingState.value != null)
                FocusRing(
                    modifier = modifier.zIndex(2F),
                    color = Color.White,
                    pointer = focusRingState.value!!
                )

        }


    }


    @Composable
    private fun FocusRing(
        modifier: Modifier = Modifier,
        color: Color, pointer: Offset
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
    fun PoseScreen(
        modifier: Modifier = Modifier,
        parentSize: DpSize,
        poseData: PoseData,
        poseScale: Float
    ) {
        val currentItem by rememberUpdatedState(newValue = poseData)
        val itemSizeRate by rememberUpdatedState(newValue = currentItem.sizeRate)
        val stdSize = DpSize(200.dp, 200.dp)

        val localDensity = LocalDensity.current
        val painter by rememberUpdatedState(
            newValue = currentItem.imageUri?.run {
                rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(this)
                        .size(with(LocalDensity.current) {
                            coil.size.Size(
                                Dimension(stdSize.width.toPx().toInt()),
                                Dimension(stdSize.height.toPx().toInt())
                            )
                        }) //현재 버튼의 크기만큼 리사이징한다.
                        .build()
                )
            })

        val scale by rememberUpdatedState(newValue = poseScale)

        val itemCenterRate by rememberUpdatedState(newValue = currentItem.centerRate)
        val poseOffset = rememberSaveable {
            mutableStateOf(
                parentSize.center.run {
                    with(localDensity) {
                        SizeF(
                            x.toPx() * itemCenterRate.width,
                            y.toPx() * itemCenterRate.height
                        )
                    }
                })
        }


        LaunchedEffect(currentItem) {
            poseOffset.value = parentSize.center.run {
                with(localDensity) {
                    SizeF(
                        x.toPx() * itemCenterRate.width,
                        y.toPx() * itemCenterRate.height
                    )
                }
            }
        }

        // 각 아이템별 이동 정보를 부모가 알 수 있을까? -> 이 화면에서 이동 처리를 하게 되면 어떻게 되지?
        var boxSize by remember {
            mutableStateOf(IntSize.Zero)
        }

        Box(modifier = modifier.onSizeChanged {
            boxSize = it
        })
        {
            PoseItem(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            poseOffset.value.width.roundToInt(),
                            poseOffset.value.height.roundToInt()

                        )
                    }
                    .pointerInput(currentItem) {
                        detectDragGestures { change, dragAmount ->
                            //이 값은 현재 포즈의 중심점 기준,  가로 세로 사이즈를 포함한 offset이 바운더리를 넘어서면 안된다.
                            val poseImageSize = stdSize * scale
                            val offset = poseOffset.value.let { Offset(it.width, it.height) }

                            val modifiedCenterOffset = (offset + dragAmount).let {
                                Offset(
                                    x = it.x.coerceIn(
                                        0F,
                                        parentSize.width.toPx()-poseImageSize.width.toPx()
                                    ),
                                    y = it.y.coerceIn(
                                        0F,
                                        parentSize.height.toPx()-poseImageSize.height.toPx()

                                    )
                                )
                            }
                            Log.d("new offset: ", modifiedCenterOffset.toString())
                            poseOffset.value = modifiedCenterOffset.let { SizeF(it.x, it.y) }
                        }
                    }
                    .scale(scale)
                    .size(stdSize),
                painter = painter
            )


        }
    }

    @Composable
    fun PoseItem(
        modifier: Modifier = Modifier,
//        poseData: PoseData,
        painter: Painter? = null
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

//        val resizablePoseSize = remember {
//            mutableStateOf(poseSize)
//        }
//        val nowPoseData by rememberUpdatedState(newValue = poseData)
//        val initOffset by rememberUpdatedState(
//            newValue = Offset(
//                boundary.center.x * nowPoseData.centerRate.width,
//                boundary.center.y * nowPoseData.centerRate.height
//            )
//        )
//
//        val offset = remember { mutableStateOf(initOffset) }
//
//
//        //포즈 이동에 문제가 있고, 사이즈를 줄이면 이동이 제대로 되지 않는 문제가 있음.
//        if (nowPoseData.imageUri != null) {
//
//
//            Log.d("location : ", "$offset / $nowPoseData / $boundary")
//            val zoom = rememberSaveable {
//                mutableFloatStateOf(1F)
//            }
//            val locDensity = LocalDensity.current
//
//            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
//
//                if (zoom.floatValue * zoomChange in 0.5f..2f) {
//                    zoom.floatValue *= zoomChange
//                }
//                val tmp = offset.value + offsetChange
//                if (tmp.x in 0F..(boundary.width - with(locDensity) { resizablePoseSize.value.toPx() * zoom.floatValue }) // 포즈 이미지가 바운더리 내에 있을 떄만 이동가능하게 함.
//                    && tmp.y in 0F..(boundary.height - with(locDensity) { resizablePoseSize.value.toPx() * zoom.floatValue })
//                ) {
//                    Log.d("움직인 정도: ", offsetChange.toString())
//                    //오프셋 값을 변경한다.
//                    offset.value = tmp
//                }
//
//            }


//@Composable
//@Preview
//fun Pre() {
//    PoseScreen(
//        modifier = Modifier.fillMaxSize(),
//        parentSize = DpSize(500.dp, 500.dp),
//        poseIndex = 2,
//        poseList = listOf(
//            PoseData(-1, -1, -1),
//            PoseData(
//                poseCat = 0,
//                poseDrawableId = R.drawable.key_image_0,
//                poseId = 1,
//            ), PoseData(
//                poseCat = 0,
//                poseDrawableId = R.drawable.key_image_1,
//                poseId = 1,
//            )
//        )
//    )
//}

