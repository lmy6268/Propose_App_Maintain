package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.viewinterop.AndroidView
import com.hanadulset.pro_poseapp.utils.pose.PoseData

object CameraScreenPreviewArea {

    //미리보기 영역
    @Composable
    fun PreviewArea(
        modifier: Modifier = Modifier,
        poseList: List<PoseData>?,
        selectedPoseIndex: Int,
        preview: PreviewView,
        isRecommendCompEnabled: Boolean,
        loadLastImage: () -> Unit,
        upperBarSize: DpSize,
        lowerBarSize: DpSize
    ) {
        val localDensity = LocalDensity.current

        //외부로부터 값을 계속적으로 업데이트 받고 싶을 때 사용
        val poseIdx by rememberUpdatedState(newValue = selectedPoseIndex)
        val previewView by rememberUpdatedState(newValue = preview)
        val compSwitchValue by rememberUpdatedState(newValue = isRecommendCompEnabled)

        //외부로 부터 받은 값이 더이상 변하지 않는 경우
        val loBarSize by remember { mutableStateOf(lowerBarSize) }
        val upBarSize by remember { mutableStateOf(upperBarSize) }

        //미리보기
        AndroidView(
            modifier = modifier,
            factory = {
                loadLastImage()
                previewView
            },

            ) {

        }
        //구도 추천
//        PoseScreen.ScrollableRecommendPoseScreen(
//            onDownActionEvent =,
//            poseDataList = ,
//            clickedItemIndexState =,
//            lowerBarDisplayPxSize =,
//            cameraDisplayPxSize =,
//            onPoseChangeEvent =,
//            onPageChangeEvent =
//        )


        //포즈 추천

    }
}

/*.pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            focusRingState.value = null
                            val untouchableArea =
                                with(localDensity) { upperButtonsRowSize.value.height.dp.toPx() }
                            if (it.y > untouchableArea) {
                                val pointer = Offset(it.x, it.y)
                                focusRingState.value = pointer.copy()
                                cameraViewModel.setFocus(
                                    previewView.meteringPointFactory.createPoint(
                                        it.x, it.y
                                    ), 2000L
                                )
                            }
                            return@pointerInteropFilter true
                        }

                        else -> {
                            false
                        }
                    }
                }
* */