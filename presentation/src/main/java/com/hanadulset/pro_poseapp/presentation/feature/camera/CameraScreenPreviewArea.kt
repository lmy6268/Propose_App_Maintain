package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
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
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenPreviewArea.PoseScreen
import com.hanadulset.pro_poseapp.utils.R
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.delay

object CameraScreenPreviewArea {

    //미리보기 영역
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PreviewArea(
        modifier: Modifier = Modifier,
        poseList: List<PoseData>?,
        padding: Dp,
        selectedPoseIndex: Int,
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

        //외부로부터 값을 계속적으로 업데이트 받고 싶을 때 사용
        val poseIdx by rememberUpdatedState(newValue = selectedPoseIndex)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding)
        )
        {
            //미리보기
            AndroidView(
                modifier = modifier
                    .animateContentSize { _, _ -> }
                    .onGloballyPositioned { coordinates ->
                        coordinates.size.let {
                            with(localDensity) {
                                previewViewSize.value = DpSize(it.width.toDp(), it.height.toDp())
                            }
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


            //포즈 화면 구성
            if (poseList != null) {
                PoseScreen(
                    modifier.size(previewViewSize.value),
                    parentSize = previewViewSize.value,
                    poseList = poseList,
                    poseIndex = poseIdx
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
        poseList: List<PoseData>,
        poseIndex: Int
    ) {
        val localDensity = LocalDensity.current
        val poseIdx by rememberUpdatedState(newValue = poseIndex)

        Box(modifier = modifier) {
            PoseItem(
                drawableId = poseList[poseIdx].poseDrawableId,
                boundary = with(localDensity) {
                    parentSize.let {
                        Size(it.width.toPx(), it.height.toPx())
                    }
                },
                poseSize = 200.dp
            )
        }


    }

    @Composable
    fun PoseItem(
        drawableId: Int,
        poseSize: Dp,
        boundary: Size,
    ) {
        val resizablePoseSize = remember {
            mutableStateOf(poseSize)
        }


        if (drawableId != -1) {
            val offset = rememberSaveable {
                mutableStateOf(
                    Pair(boundary.width / 4, boundary.height / 4)
                )
            }
            val zoom = rememberSaveable {
                mutableFloatStateOf(1F)
            }
            val locDensity = LocalDensity.current


            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                if (zoom.floatValue * zoomChange in 0.5f..2f) {
                    zoom.floatValue *= zoomChange
                }
                val tmp = offset.value.let { Offset(it.first, it.second) } + offsetChange
                if (tmp.x in 0F..(boundary.width - with(locDensity) {
                        resizablePoseSize.value.toPx() * zoom.floatValue
                    })
                    && tmp.y in 0F..(boundary.height - with(locDensity) {
                        resizablePoseSize.value.toPx() * zoom.floatValue
                    })
                )
                    offset.value.let { offsetValue ->
                        val changed = Offset(offsetValue.first, offsetValue.second) + offsetChange
                        offset.value = changed.let { Pair(it.x, it.y) }
                    }
            }

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(drawableId)
                    .size(with(LocalDensity.current) {
                        poseSize.toPx().toInt()
                    }) //현재 버튼의 크기만큼 리사이징한다.
                    .build()
            )

            Image(
                painter = painter,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = zoom.floatValue,
                        scaleY = zoom.floatValue,
                        translationX = offset.value.first,
                        translationY = offset.value.second
                    )
                    .transformable(state = transformState)
                    .size(poseSize),
                contentScale = ContentScale.Fit,
                contentDescription = ""
            )
        } else Box(
            modifier = Modifier
                .size(200.dp)
        )

    }


}

@Composable
@Preview
fun Pre() {
    PoseScreen(
        modifier = Modifier.fillMaxSize(),
        parentSize = DpSize(500.dp, 500.dp),
        poseIndex = 2,
        poseList = listOf(
            PoseData(-1, -1, -1),
            PoseData(
                poseCat = 0,
                poseDrawableId = R.drawable.key_image_0,
                poseId = 1,
            ), PoseData(
                poseCat = 0,
                poseDrawableId = R.drawable.key_image_1,
                poseId = 1,
            )
        )
    )
}

