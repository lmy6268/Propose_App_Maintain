package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ParticularZoomButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ToggledButton
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
        galleryImageUri: Uri?,
        onPoseRecommendEvent: () -> Unit,
        onEdgeDetectEvent: (Boolean) -> Unit,
        onShutterClickEvent: () -> Unit,
        onGalleryButtonClickEvent: () -> Unit,
        onSelectedPoseIndexEvent: (Int) -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        onFixedButtonClickEvent: (Boolean) -> Unit,
        upperLayerPaddingTop: Dp = 0.dp,
        lowerLayerPaddingBottom: Dp = 0.dp,
        poseList: List<PoseData>? = null
    ) {
        //필요할 때만 리컴포지션을 진행함.
        //https://kotlinworld.com/256 참고 
        val poseDataList by rememberUpdatedState(newValue = poseList)
        val poseListShowState = remember { mutableStateOf(false) }
        val disturbFixedButtonState = remember {
            mutableStateOf(false)
        }
        val disturbEdgeDetectorButtonState = remember {
            mutableStateOf(false)
        }

        val fixedBtnClickEvent = remember<(Boolean) -> Unit> {
            {
                onFixedButtonClickEvent(it)
                disturbEdgeDetectorButtonState.value = true
                disturbFixedButtonState.value = false
            }
        }
        val horizontalBetweenItemsSpace = 20.dp

        val edgeDetectBtnClickEvent = remember<(Boolean) -> Unit> {
            {
                onEdgeDetectEvent(it)
                disturbFixedButtonState.value = true
                disturbEdgeDetectorButtonState.value = false
            }
        }



        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (poseListShowState.value)
                PoseListLayer(
                    modifier = Modifier,
                    poseList = poseDataList,
                    onSelectedPoseIndexEvent = onSelectedPoseIndexEvent
                )
            UpperLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = upperLayerPaddingTop),
                onEdgeDetectEvent = edgeDetectBtnClickEvent,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent,
                disturbFromFixedButton = disturbEdgeDetectorButtonState.value,
                horizontalBetweenItemsSpace = horizontalBetweenItemsSpace + 10.dp,
                onRecommendPoseEvent = onPoseRecommendEvent
            )
            LowerLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                onFixedButtonClickEvent = fixedBtnClickEvent,
                galleryImageUri = galleryImageUri,
                disturbFromEdgeDetector = disturbFixedButtonState.value,
                horizontalBetweenItemsSpace = horizontalBetweenItemsSpace
            )
        }
    }

    @Composable
    fun PoseListLayer(
        modifier: Modifier = Modifier,
        poseList: List<PoseData>?,
        onSelectedPoseIndexEvent: (Int) -> Unit = {},
    ) {
        //리컴포지션을 방지
        //https://kotlinworld.com/256 참고
        val poseDataList by rememberUpdatedState(newValue = poseList)

        //BottomSheet + LazyRow 사용할 예정

    }


    // 포즈 추천 , 따오기 -> On/Off , 줌레벨은 선택된 버튼이 돋보이도록 사이즈 조절
    // 포즈 추천 버튼, 줌레벨 설정 버튼 , 따오기 버튼이 존재

    //따오기를 할 때, 고정을 할 수 있음.. -> 이문제는 어떻게 해결해야 할까?
    // 고정을 할 때, 따오기를 이전에 했다면, 해제해줘야하는데 어떻게 해야할 까?
    @Composable
    fun UpperLayer(
        modifier: Modifier = Modifier,
        horizontalBetweenItemsSpace: Dp = 10.dp,
        buttonSize: Dp = 40.dp,
        onEdgeDetectEvent: (Boolean) -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        onRecommendPoseEvent: () -> Unit,
        disturbFromFixedButton: Boolean,
    ) {
        val initEdgeValue = false
        val edgeDetectorState = remember { mutableStateOf(initEdgeValue) }
        val edgeDetectEvent by rememberUpdatedState<(Boolean) -> Unit>(newValue = { selected ->
            edgeDetectorState.value = selected
            onEdgeDetectEvent(selected) //선택된 경우에만 호출
        })
        val isDisturbed by rememberUpdatedState(newValue = disturbFromFixedButton)


        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(
                horizontalBetweenItemsSpace,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //포즈 추천 버튼 -> 토글 형식이 아니었던가..?
            CameraScreenButtons
                .NormalButton(
                    buttonSize = buttonSize,
                    buttonName = "포즈 추천 버튼",
                    buttonText = "포즈\n추천",
                    colorTint = Color.Black,
                    onClick = onRecommendPoseEvent
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
                onClickEvent = edgeDetectEvent,
                customEventValue = isDisturbed
            )
        }


    }

    // 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
    @Composable
    fun LowerLayer(
        modifier: Modifier = Modifier,
        galleryImageUri: Uri?,
        disturbFromEdgeDetector: Boolean,
        horizontalBetweenItemsSpace: Dp = 30.dp,
        onShutterClickEvent: () -> Unit = {},
        onGalleryButtonClickEvent: () -> Unit = {},
        onFixedButtonClickEvent: (Boolean) -> Unit = {}
    ) {

        //개별 상태변수를 가지고 있으므로써, 의도치 않은 리컴포지션을 방지
        val galleryImageState by rememberUpdatedState(newValue = galleryImageUri)
        val isFixedDisturbed by rememberUpdatedState(newValue = disturbFromEdgeDetector)

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(
                horizontalBetweenItemsSpace, Alignment.CenterHorizontally
            ), verticalAlignment = Alignment.CenterVertically
        ) {
            //갤러리 이미지 버튼
            GalleryImageButton(
                galleryImageUri = galleryImageState,
                buttonSize = 55.dp,
                onClickEvent = onGalleryButtonClickEvent
            )

            //셔터
            CameraScreenButtons.ShutterButton(
                buttonSize = 80.dp
            ) { onShutterClickEvent() }

            //고정
            CameraScreenButtons.FixedButton(
                modifier = Modifier,
                buttonSize = 55.dp,
                disturbFromEdgeDetector = isFixedDisturbed,
                fixedButtonPressedEvent = onFixedButtonClickEvent

            )
        }
    }


    @Composable
    private fun GalleryImageButton(
        modifier: Modifier = Modifier,
        galleryImageUri: Uri?,
        buttonSize: Dp,
        defaultBackgroundColor: Color = Color(0x80FAFAFA),
        inputAnimationDurationMilliSec: Int = 150,
        onClickEvent: () -> Unit,
    ) {
        GlideImage(
            imageModel = { galleryImageUri },
            modifier = modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(color = defaultBackgroundColor)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = CameraScreenButtons.CustomIndication,
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
        selectedButtonScale: Float = 1.2F,
        defaultButtonSize: Dp = 20.dp,
        spaceByEachItems: Dp = 8.dp,
        onClickEvent: (Float) -> Unit,
    ) {
        val selectedButtonIndexState = remember { mutableIntStateOf(0) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                spaceByEachItems,
                Alignment.CenterHorizontally
            )
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

        },
        galleryImageUri = null,
        disturbFromEdgeDetector = false
    )
}


@Composable
@Preview
fun PreviewUnderBar() {
    CameraScreenUnderBar.UnderBar(
        galleryImageUri = null,
        onEdgeDetectEvent = { /*TODO*/ },
        onShutterClickEvent = { /*TODO*/ },
        onGalleryButtonClickEvent = { /*TODO*/ },
        onSelectedPoseIndexEvent = {},
        onZoomLevelChangeEvent = {},
        onFixedButtonClickEvent = {},
        onPoseRecommendEvent = {}
    )
}


@Composable
@Preview
fun PreviewPoseList() {
    CameraScreenUnderBar.PoseListLayer(
        poseList = null
    )
}
