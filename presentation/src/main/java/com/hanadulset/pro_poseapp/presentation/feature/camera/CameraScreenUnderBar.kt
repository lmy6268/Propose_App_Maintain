package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModuleExtension.ParticularZoomButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModuleExtension.ToggledButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.LowerLayer
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage

object CameraScreenUnderBar {

    //하단바 구성
    //총 세 개의 층이고, 추천 층을 제외한 두 층 각각은 별도의 패딩이 필요하다.

    @Composable
    fun UnderBar(
        modifier: Modifier = Modifier,
        onEdgeDetectEvent: () -> Unit,
        onShutterClickEvent: () -> Unit,
        onGalleryButtonClickEvent: () -> Unit,
        onSelectedPoseIndexEvent: (Int) -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        onFixedButtonClickEvent: (Boolean) -> Unit,
        upperLayerPaddingTop: Dp = 0.dp,
        lowerLayerPaddingBottom: Dp = 0.dp,
    ) {
        val poseListState = remember { mutableStateOf<List<PoseData>?>(null) }
        val poseListShowState = remember { mutableStateOf(false) }

        Column(
            modifier = modifier
        ) {
            if (poseListShowState.value)
                PoseListLayer(modifier = Modifier)
            UpperLayer(
                modifier = Modifier.padding(top = upperLayerPaddingTop),
                onEdgeDetectEvent = onEdgeDetectEvent,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent
            )
            LowerLayer(
                modifier = Modifier.padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                onFixedButtonClickEvent = onFixedButtonClickEvent
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun PoseListLayer(
        modifier: Modifier = Modifier
    ) {
        BottomSheetScaffold(
            modifier = modifier,
            drawerShape = ,
            sheetContent = {

            }) {

        }


    }


    // 포즈 추천 , 따오기 -> On/Off , 줌레벨은 선택된 버튼이 돋보이도록 사이즈 조절
    // 포즈 추천 버튼, 줌레벨 설정 버튼 , 따오기 버튼이 존재

    //따오기를 할 때, 고정을 할 수 있음.. -> 이문제는 어떻게 해결해야 할까?
    // 고정을 할 때, 따오기를 이전에 했다면, 해제해줘야하는데 어떻게 해야할 까?
    @Composable
    fun UpperLayer(
        modifier: Modifier = Modifier,
        horizontalBetweenItemsSpace: Dp = 10.dp,
        buttonSize: Dp = 30.dp,
        onEdgeDetectEvent: () -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
    ) {
        val initPoseValue = false
        val initEdgeValue = false
        val recommendPoseState = remember { mutableStateOf(initPoseValue) }
        val edgeDetectorState = remember { mutableStateOf(initEdgeValue) }
        val edgeDetectEvent by rememberUpdatedState<(Boolean) -> Unit>(newValue = { selected ->
            edgeDetectorState.value = selected
            if (selected) onEdgeDetectEvent() //선택된 경우에만 호출
        })


        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(
                horizontalBetweenItemsSpace,
                Alignment.CenterHorizontally
            )
        ) {
            //포즈 추천 버튼 -> 토글 형식이 아니었던가..?
            ToggledButton(
                buttonSize = buttonSize,
                initState = initPoseValue,
                buttonText = "포즈\n추천 ",
                onClickEvent = {
                    recommendPoseState.value = it
                }
            )


            //줌 레벨 설정 버튼
            ZoomButtonRow(
                modifier = Modifier.wrapContentSize(),
                defaultButtonSize = buttonSize,
                onClickEvent = onZoomLevelChangeEvent
            )

            //따오기 버튼
            ToggledButton(
                buttonSize = buttonSize,
                buttonText = "따오기",
                initState = initEdgeValue,
                onClickEvent = edgeDetectEvent
            )
        }


    }

    // 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
    @Composable
    fun LowerLayer(
        modifier: Modifier = Modifier,
        horizontalBetweenItemsSpace: Dp = 30.dp,
        onShutterClickEvent: () -> Unit = {},
        onGalleryButtonClickEvent: () -> Unit = {},
        onFixedButtonClickEvent: (Boolean) -> Unit = {}
    ) {

        //개별 상태변수를 가지고 있으므로써, 의도치 않은 리컴포지션을 방지
        val fixedButtonState = remember { mutableStateOf(false) }
        val galleryImageState = remember { mutableStateOf<ImageBitmap?>(null) }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(
                horizontalBetweenItemsSpace, Alignment.CenterHorizontally
            ), verticalAlignment = Alignment.CenterVertically
        ) {
            //갤러리 이미지 버튼
            GalleryImageButton(
                imageBitmap = galleryImageState.value,
                buttonSize = 55.dp,
                onClickEvent = onGalleryButtonClickEvent
            )

            //셔터
            CameraModuleExtension.ShutterButton(
                buttonSize = 80.dp
            ) { onShutterClickEvent() }

            //고정
            CameraModuleExtension.FixedButton(
                modifier = Modifier,
                buttonSize = 55.dp
            ) { fixedState ->
                fixedButtonState.value = fixedState
                onFixedButtonClickEvent(fixedState)
            }
        }
    }


    @Composable
    private fun GalleryImageButton(
        modifier: Modifier = Modifier,
        imageBitmap: ImageBitmap?,
        buttonSize: Dp,
        defaultBackgroundColor: Color = Color(0x80FAFAFA),
        inputAnimationDurationMilliSec: Int = 150,
        onClickEvent: () -> Unit,
    ) {
        GlideImage(
            imageModel = { imageBitmap },
            modifier = modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(color = defaultBackgroundColor)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = CameraModuleExtension.CustomIndication,
                    onClick = onClickEvent
                ),
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop
            ),
            component = rememberImageComponent {
                +CircularRevealPlugin(duration = inputAnimationDurationMilliSec)
            }
        )
    }


    @Composable
    fun ZoomButtonRow(
        modifier: Modifier,
        selectedButtonScale: Float = 1.5F,
        defaultButtonSize: Dp = 20.dp,
        onClickEvent: (Float) -> Unit,
    ) {
        val selectedButtonIndexState = remember { mutableIntStateOf(0) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            listOf(1, 2).forEachIndexed { index, value ->
                ParticularZoomButton(
                    selected = selectedButtonIndexState.intValue == index,
                    defaultButtonSize = defaultButtonSize,
                    buttonValue = value,
                    selectedButtonScale = selectedButtonScale
                ) {
                    selectedButtonIndexState.intValue = index
                    onClickEvent(value.toFloat())
                }
            }
        }
    }


}

@Composable
@Preview
fun PreviewLowerLayer() {
    LowerLayer(
        modifier = Modifier.fillMaxWidth(),
        onShutterClickEvent = {

        },
        onGalleryButtonClickEvent = {

        }
    )
}

@Composable
@Preview
fun PreviewUpperLayer() {
    CameraScreenUnderBar.UpperLayer(
        onEdgeDetectEvent = {

        }
    ) {

    }
}

@Composable
@Preview
fun PreviewZoomRow() {
    CameraScreenUnderBar.ZoomButtonRow(modifier = Modifier.fillMaxWidth()) {

    }
}


@Composable
@Preview
fun PreviewPoseList() {
    CameraScreenUnderBar.PoseListLayer()
}
