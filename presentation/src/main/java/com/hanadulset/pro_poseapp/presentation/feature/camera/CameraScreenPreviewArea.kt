package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object CameraScreenPreviewArea {

    //미리보기 영역
    @Composable
    fun PreviewArea(
        modifier: Modifier = Modifier
    ) {

    }


    //미리보기


    //구도 추천


    //포즈 추천

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