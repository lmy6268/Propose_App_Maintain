package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.UIComponents.CircularWaitingBar
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ParticularZoomButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ToggledButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.defaultButtonSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.galleryButtonSize
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenUnderBar.shutterButtonSize
import com.hanadulset.pro_poseapp.utils.pose.PoseData

object CameraScreenUnderBar {

    //하단바 구성
    //총 세 개의 층이고, 추천 층을 제외한 두 층 각각은 별도의 패딩이 필요하다.

    //버튼 사이즈
    val shutterButtonSize = 70.dp
    val defaultButtonSize = shutterButtonSize - 20.dp
    val galleryButtonSize = shutterButtonSize - 20.dp

    @Composable
    fun UnderBar(
        modifier: Modifier = Modifier,
        galleryImageUri: () -> Uri?,
        onPoseRecommendEvent: () -> Unit,
        onShutterClickEvent: () -> Unit,
        onGalleryButtonClickEvent: () -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        lowerLayerPaddingBottom: Dp = 0.dp,
        zoomLevelState: () -> Float,
        userEdgeDetectionValue: () -> Boolean,
        systemEdgeDetectionValue: () -> Boolean,
        onSystemEdgeDetectionClicked: () -> Unit,
        onUserEdgeDetectionClicked: () -> Unit,
        isRecommendPoseEnabled: () -> Boolean
    ) {

        val galleryThumbUri by rememberUpdatedState(newValue = galleryImageUri)

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UpperLayer(
                modifier = Modifier.fillMaxWidth(),
                onUserEdgeDetectionClicked = onUserEdgeDetectionClicked,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent,
                onFixedButtonClickEvent = onSystemEdgeDetectionClicked,
                userEdgeDetectionValue = userEdgeDetectionValue,
                systemEdgeDetectionValue = systemEdgeDetectionValue,
                zoomLevelState = zoomLevelState,
            )
            LowerLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                galleryImageUri = galleryThumbUri,
                isRecommendPoseEnabled = isRecommendPoseEnabled,
                onRecommendPoseEvent = onPoseRecommendEvent
            )
        }

    }

}


// 포즈 추천 , 따오기 -> On/Off , 줌레벨은 선택된 버튼이 돋보이도록 사이즈 조절
// 포즈 추천 버튼, 줌레벨 설정 버튼 , 따오기 버튼이 존재

@Composable
fun UpperLayer(
    modifier: Modifier = Modifier,
    systemEdgeDetectionValue: () -> Boolean,
    onUserEdgeDetectionClicked: () -> Unit,
    userEdgeDetectionValue: () -> Boolean,
    onZoomLevelChangeEvent: (Float) -> Unit,
    onFixedButtonClickEvent: () -> Unit = {},
    zoomLevelState: () -> Float
) {
    val edgeDetectorState by rememberUpdatedState(newValue = userEdgeDetectionValue)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        val fixedBtnValue by rememberUpdatedState(newValue = systemEdgeDetectionValue)
        //따오기 버튼
        ToggledButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            onClickEvent = onUserEdgeDetectionClicked,
            buttonStatus = edgeDetectorState(),
            buttonText = "따오기",
            inActivatedColor = LocalColors.current.secondaryWhite100,
            buttonTextColor = LocalColors.current.subPrimaryBlack100
        )
        //줌 레벨 설정 버튼

        ZoomButtonRow(
            modifier = Modifier.wrapContentSize(),
            onClickEvent = onZoomLevelChangeEvent,
            zoomLevelState = zoomLevelState()
        )
        //고정
        CameraScreenButtons.FixedButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            onFixedButtonPressedEvent = onFixedButtonClickEvent,
            fixedBtnStatus = fixedBtnValue()
        )

    }


}

// 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
@Composable
fun LowerLayer(
    modifier: Modifier = Modifier,
    galleryImageUri: () -> Uri?,
    onRecommendPoseEvent: () -> Unit,
    onShutterClickEvent: () -> Unit = {},
    isRecommendPoseEnabled: () -> Boolean,
    onGalleryButtonClickEvent: () -> Unit = {},
) {


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //갤러리 이미지 버튼
        GalleryImageButton(
            galleryImageUri = galleryImageUri,
            buttonSize = galleryButtonSize,
            onClickEvent = onGalleryButtonClickEvent
        )

        //셔터
        CameraScreenButtons.ShutterButton(
            buttonSize = shutterButtonSize
        ) { onShutterClickEvent() }


        if (isRecommendPoseEnabled()) CameraScreenButtons.NormalButton(
            modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
            buttonSize = defaultButtonSize,
            buttonName = "포즈 추천 버튼",
            buttonText = "포즈",
            buttonTextColor = LocalColors.current.subPrimaryBlack100,
            colorTint = LocalColors.current.secondaryWhite100,
            onClick = onRecommendPoseEvent,
            buttonTextSize = 12
        )
        else CameraScreenButtons.NormalButton(
            buttonName = "비어있는 공간",
            isButtonEnable = false,
            buttonSize = defaultButtonSize,
            colorTint = Color.Transparent,
            onClick = {})
    }
}


@Composable
private fun GalleryImageButton(
    modifier: Modifier = Modifier,
    galleryImageUri: () -> Uri?,
    buttonSize: Dp,
    defaultBackgroundColor: Color = Color(0x80FAFAFA),
    onClickEvent: () -> Unit,
) {

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current).data(galleryImageUri())
            .size(with(LocalDensity.current) { buttonSize.toPx().toInt() }) //현재 버튼의 크기만큼 리사이징한다.
            .build()
    )

    Surface(
        shadowElevation = 2.dp,
        shape = CircleShape,
        modifier = Modifier.wrapContentSize(),
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "갤러리 이미지",
            modifier = modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(color = defaultBackgroundColor)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = CameraScreenButtons.CustomIndication,
                    onClick = onClickEvent
                ),
            contentScale = ContentScale.Crop,
        )
    }

}


@Composable
fun ZoomButtonRow(
    modifier: Modifier,
    selectedButtonScale: Float = 1.2F,
    defaultButtonSize: Dp = 40.dp,
    spaceByEachItems: Dp = 10.dp,
    onClickEvent: (Float) -> Unit,
    zoomLevelState: Float
) {
    val zoomLevel by rememberUpdatedState(newValue = zoomLevelState)

    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(
                shape = RoundedCornerShape(100.dp),
                color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.5F)
            )
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(
                spaceByEachItems, Alignment.CenterHorizontally
            )
        ) {
            listOf(1, 2).forEachIndexed { index, value ->
                ParticularZoomButton(
                    selected = (index + 1) == zoomLevel.toInt(),
                    defaultButtonSize = defaultButtonSize / selectedButtonScale,
                    buttonValue = value,
                    selectedButtonScale = selectedButtonScale
                ) {
                    onClickEvent(value.toFloat())
                }
            }
        }
    }


//    }


}


//포즈 추천 버튼을 누르면 나오는 버튼 배열
@Composable
fun ClickPoseBtnUnderBar(
    modifier: Modifier = Modifier,
    poseList: () -> List<PoseData>?,
    galleryImageUri: () -> Uri?,
    initPoseItemScale: () -> Float = { 1F },
    currentSelectedPoseItemIdx: () -> Int,
    onRefreshPoseData: () -> Unit,
    onClickShutterBtn: () -> Unit,
    onGalleryButtonClickEvent: () -> Unit,
    onClickCloseBtn: () -> Unit,
    onSelectedPoseIndexEvent: (Int) -> Unit,
    onChangeScale: (Float) -> Unit,
    is16By9AspectRatio: () -> Boolean,
    maxScale: () -> Float,
) {
    BackHandler(onBack = onClickCloseBtn) //뒤로가기 버튼을 누르면 이전 화면으로 돌아감.


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val localColor = LocalColors.current
        val sliderBackgroundColor =
            rememberUpdatedState(newValue = if (is16By9AspectRatio()) localColor.secondaryWhite100 else Color.Unspecified)
        val backgroundColor =
            rememberUpdatedState(newValue = if (is16By9AspectRatio()) Color.Unspecified else localColor.secondaryWhite100)
        val itemTextColor =
            rememberUpdatedState(
                newValue = if (is16By9AspectRatio()) localColor.secondaryWhite100
                else localColor.subPrimaryBlack100
            )



        if (poseList() != null && currentSelectedPoseItemIdx() > 0) {
            val trackedPoseScaleValue =
                remember { mutableFloatStateOf(initPoseItemScale()) } //현재 상태의 스케일 값을 추적
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .padding(start = 10.dp),
                    painter = painterResource(id = R.drawable.icon_zoom_out),
                    contentDescription = "",
                    tint = sliderBackgroundColor.value
                )

                Slider(colors = SliderDefaults.colors(
                    thumbColor = localColor.primaryGreen100,
                    activeTrackColor = localColor.primaryGreen100,
                    inactiveTrackColor = localColor.secondaryWhite80,
                    activeTickColor = localColor.primaryGreen100,
                    inactiveTickColor = Color.Transparent
                ),
                    modifier = Modifier
                        .width(width = (LocalConfiguration.current.screenWidthDp / 1.5).dp)
                        .height(50.dp),
                    value = trackedPoseScaleValue.floatValue,
                    steps = 10,
                    valueRange = 0.5F.rangeTo(2F),
                    onValueChange = {
                        it.coerceIn(
                            maximumValue = maxScale(),
                            minimumValue = 0.5F
                        ).run {
                            trackedPoseScaleValue.floatValue = this
                            onChangeScale(this)
                        }
                    })

                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 10.dp),
                    painter = painterResource(id = R.drawable.icon_zoom_in),
                    contentDescription = "",
                    tint = sliderBackgroundColor.value

                )
            }

        } else {
            Spacer(
                modifier = Modifier
                    .height(50.dp)
            )
        }
        //포즈 선택 할 수 있는 Row -> 선택된 포즈를 가지고 스케일 변경 진행
        PoseSelectRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor.value)
                .padding(top = 10.dp),
            currentSelectedIdx = currentSelectedPoseItemIdx,
            inputPosedDataList = poseList,
            onSelectedPoseIndexEvent = { onSelectedPoseIndexEvent(it) },
            itemTextColor = itemTextColor.value
        )

        PoseSelectLowerMenu(
            modifier = Modifier
                .background(backgroundColor.value)
                .padding(horizontal = 50.dp)
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //창 닫는 버튼
            GalleryImageButton(
                galleryImageUri = galleryImageUri,
                buttonSize = galleryButtonSize,
                onClickEvent = onGalleryButtonClickEvent
            )

            //셔터 버튼
            CameraScreenButtons.ShutterButton(
                onClickEvent = onClickShutterBtn,
                buttonSize = shutterButtonSize
            )

            //포즈 새로고침 버튼
            CameraScreenButtons.NormalButton(
                modifier = Modifier.shadow(elevation = 2.dp, shape = CircleShape),
                buttonName = "포즈 새로고침",
                innerIconDrawableSize = defaultButtonSize / 3,
                colorTint = localColor.secondaryWhite100,
                innerIconDrawableId = R.drawable.refresh,
                onClick = { onRefreshPoseData() },
                buttonSize = defaultButtonSize,
                innerIconColorTint = localColor.subPrimaryBlack100
            )
        }
    }

}

@Composable
fun PoseSelectLowerMenu(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        content = content,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PoseSelectRow(
    modifier: Modifier = Modifier,
    currentSelectedIdx: () -> Int,
    inputPosedDataList: () -> List<PoseData>?,
    onSelectedPoseIndexEvent: (Int) -> Unit,
    itemTextColor: Color
) {
    val scrollState = rememberLazyListState()
    val nowSelected = rememberUpdatedState(newValue = currentSelectedIdx())
    val rowWidth = LocalConfiguration.current.screenWidthDp.dp
    val poseItemSize = DpSize(80.dp, 80.dp)
    val textSize = 10.dp
    val flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(scrollState))
    val padding by rememberUpdatedState(newValue = (rowWidth - poseItemSize.width) / 2)
    val immutableList = rememberUpdatedState(newValue = inputPosedDataList())

    LaunchedEffect(immutableList) {
        scrollState.scrollToItem(nowSelected.value)
    }

    LaunchedEffect(nowSelected.value) {
        scrollState.animateScrollToItem(nowSelected.value)
    }
    immutableList.value.run {
        if (this != null) {
            LazyRow(
                modifier = modifier,
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(
                    horizontal = padding
                ),
                flingBehavior = flingBehavior,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(
                    count = this@run.size,
                    key = {
                        this@run[it].poseId
                    },
                    itemContent = { idx ->
                        val poseItem = this@run[idx]
                        PoseSelectionItem(
                            modifier = Modifier.size(poseItemSize),
                            isSelected = idx == nowSelected.value,
                            imageUri = poseItem.imageUri,
                            poseIndex = idx,
                            onClickEvent = {
                                onSelectedPoseIndexEvent(idx)
                            },
                            poseSize = poseItemSize.height - textSize * 2,
                            textSize = textSize,
                            itemTextColor = itemTextColor
                        )
                    }
                )
            }
        } else {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(poseItemSize.height),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularWaitingBar()
                    Text(
                        text = "포즈 추천 중..",
                        textAlign = TextAlign.Center,
                        color = itemTextColor,
                        fontFamily = CameraScreenButtons.pretendardFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }


}

//클릭 할 수 있는 포즈 아이템 카드
@Composable
fun PoseSelectionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    imageUri: Uri?,
    poseIndex: Int,
    poseSize: Dp,
    textSize: Dp,
    itemTextColor: Color,
    onClickEvent: () -> Unit
) {
    val colorTheme = LocalColors.current

    val unSelectedColor = colorTheme.secondaryWhite100
    val selectedColor = colorTheme.primaryGreen100
    val stateColor = if (isSelected) selectedColor else unSelectedColor
    val defaultModifier = Modifier
        .padding(2.dp)
        .wrapContentSize()
        .clickable(
            interactionSource = MutableInteractionSource(), indication = rememberRipple(
                color = if (isSelected) selectedColor
                else unSelectedColor, bounded = true, radius = poseSize / 2
            )
        ) { onClickEvent() }
    val painter = rememberAsyncImagePainter(model = ImageRequest.Builder(LocalContext.current)
        .data(imageUri.run {
            this ?: R.drawable.impossible_icon
        }).size(with(LocalDensity.current) {
            poseSize.toPx().toInt()
        }) //현재 버튼의 크기만큼 리사이징한다.
        .build()
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val checkedPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.selected_icon)
                .build()
        )

        Surface(
            shadowElevation = 4.dp,
            color = stateColor,
            shape = ShapeDefaults.Medium,
            modifier = Modifier.wrapContentSize()
        ) {
            Box(modifier = Modifier.wrapContentSize()) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(poseSize)
                            .zIndex(1F)
                    ) {
                        Image(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(poseSize / 2),
                            painter = checkedPainter,
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(color = LocalColors.current.secondaryWhite100)
                        )
                    }
                }
                Card(
                    modifier = defaultModifier
                ) {
                    Image(
                        modifier = Modifier
                            .background(color = stateColor)
                            .size(poseSize),
                        painter = painter,
                        contentDescription = "이미지",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(color = LocalColors.current.subPrimaryBlack100)
                    )
                }
            }
        }

        Text(
            text = if (imageUri == null) "없음" else "포즈 #$poseIndex",
            textAlign = TextAlign.Center,
            fontFamily = CameraScreenButtons.pretendardFamily,
            fontWeight = FontWeight.Bold,
            fontSize = with(LocalDensity.current) { textSize.toSp() },
            color = itemTextColor,
            modifier = Modifier
                .padding(top = 3.dp)
                .width(poseSize)
                .wrapContentHeight()

        )
    }


}


//@Composable
//@Preview
//fun PreviewLowerLayer() {
//    LowerLayer(modifier = Modifier.fillMaxWidth(), onShutterClickEvent = {
//
//    }, onGalleryButtonClickEvent = {
//
//    }, galleryImageUri = null, disturbFromEdgeDetector = false
//    )
//}
//
//
//@Composable
//@Preview
//fun PreviewUnderBar() {
//    CameraScreenUnderBar.UnderBar(
//        galleryImageUri = null,
//        onPoseRecommendEvent = {},
//        onEdgeDetectEvent = { false },
//        onShutterClickEvent = { /*TODO*/ },
//        onGalleryButtonClickEvent = { /*TODO*/ },
//        onZoomLevelChangeEvent = {},
//        onFixedButtonClickEvent = {}
//    )
//}


@Composable
@Preview
fun PreviewSelector() {
    val poseList = listOf(
        PoseData(
            poseId = 0, imageUri = null, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        ), PoseData(
            poseId = 0, poseCat = 1
        )


    )
    ClickPoseBtnUnderBar(
        modifier = Modifier.fillMaxWidth(),
        poseList = { poseList },
        onSelectedPoseIndexEvent = {},
        onClickCloseBtn = {},
        onClickShutterBtn = {},
        onRefreshPoseData = {},
        currentSelectedPoseItemIdx = { 0 },
        galleryImageUri = { null },
        onGalleryButtonClickEvent = {

        },
        onChangeScale = {

        },
        is16By9AspectRatio = { true },
        maxScale = { 2F }
    )
}

@Preview
@Composable
fun TestZoomRow() {
    ZoomButtonRow(modifier = Modifier.wrapContentSize(), onClickEvent = { }, zoomLevelState = 1F)

}
