package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.launch

object CameraScreenUnderBar {

    //하단바 구성
    //총 세 개의 층이고, 추천 층을 제외한 두 층 각각은 별도의 패딩이 필요하다.

    @Composable
    fun UnderBar(
        modifier: Modifier = Modifier,
        galleryImageUri: Uri?,
        onPoseRecommendEvent: () -> Unit,
        onShutterClickEvent: () -> Unit,
        onGalleryButtonClickEvent: () -> Unit,
        onZoomLevelChangeEvent: (Float) -> Unit,
        lowerLayerPaddingBottom: Dp = 0.dp,
        userEdgeDetectionValue: Boolean,
        systemEdgeDetectionValue: Boolean,
        onSystemEdgeDetectionClicked: () -> Unit,
        onUserEdgeDetectionClicked: () -> Unit
    ) {

        val galleryThumbUri by rememberUpdatedState(newValue = galleryImageUri)

        Column(
            modifier = modifier.padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UpperLayer(
                modifier = Modifier.fillMaxWidth(),
                onUserEdgeDetectionClicked = onUserEdgeDetectionClicked,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent,
                onRecommendPoseEvent = onPoseRecommendEvent,
                userEdgeDetectionValue = userEdgeDetectionValue
            )
            LowerLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                onFixedButtonClickEvent = onSystemEdgeDetectionClicked,
                galleryImageUri = galleryThumbUri,
                systemEdgeDetectionValue = systemEdgeDetectionValue
            )
        }

    }

}


// 포즈 추천 , 따오기 -> On/Off , 줌레벨은 선택된 버튼이 돋보이도록 사이즈 조절
// 포즈 추천 버튼, 줌레벨 설정 버튼 , 따오기 버튼이 존재

//따오기를 할 때, 고정을 할 수 있음.. -> 이문제는 어떻게 해결해야 할까?
// 고정을 할 때, 따오기를 이전에 했다면, 해제해줘야하는데 어떻게 해야할 까?
@Composable
fun UpperLayer(
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    onUserEdgeDetectionClicked: () -> Unit,
    userEdgeDetectionValue: Boolean,
    onZoomLevelChangeEvent: (Float) -> Unit,
    onRecommendPoseEvent: () -> Unit,
) {
    val edgeDetectorState by rememberUpdatedState(newValue = userEdgeDetectionValue)


    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        CameraScreenButtons.NormalButton(
            buttonSize = buttonSize,
            buttonName = "포즈 추천 버튼",
            buttonText = "포즈",
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
            onClickEvent = onUserEdgeDetectionClicked,
            buttonStatus = edgeDetectorState
        )
    }


}

// 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
@Composable
fun LowerLayer(
    modifier: Modifier = Modifier,
    galleryImageUri: Uri?,
    systemEdgeDetectionValue: Boolean,
    onShutterClickEvent: () -> Unit = {},
    onGalleryButtonClickEvent: () -> Unit = {},
    onFixedButtonClickEvent: () -> Unit = {}
) {

    //개별 상태변수를 가지고 있으므로써, 의도치 않은 리컴포지션을 방지
    val galleryImageState by rememberUpdatedState(newValue = galleryImageUri)
    val fixedBtnValue by rememberUpdatedState(newValue = systemEdgeDetectionValue)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //갤러리 이미지 버튼
        GalleryImageButton(
            galleryImageUri = galleryImageState,
            buttonSize = 60.dp,
            onClickEvent = onGalleryButtonClickEvent
        )

        //셔터
        CameraScreenButtons.ShutterButton(
            buttonSize = 80.dp
        ) { onShutterClickEvent() }

        //고정
        CameraScreenButtons.FixedButton(
            modifier = Modifier,
            buttonSize = 60.dp,
            onFixedButtonPressedEvent = onFixedButtonClickEvent,
            fixedBtnStatus = fixedBtnValue
        )
    }
}


@Composable
private fun GalleryImageButton(
    modifier: Modifier = Modifier,
    galleryImageUri: Uri?,
    buttonSize: Dp,
    defaultBackgroundColor: Color = Color(0x80FAFAFA),
    onClickEvent: () -> Unit,
) {
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(galleryImageUri)
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
    defaultButtonSize: Dp = 20.dp,
    spaceByEachItems: Dp = 8.dp,
    onClickEvent: (Float) -> Unit,
) {
    val selectedButtonIndexState = rememberSaveable { mutableIntStateOf(0) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            spaceByEachItems, Alignment.CenterHorizontally
        )
    ) {
        listOf(1, 2).forEachIndexed { index, value ->
            ParticularZoomButton(
                selected = selectedButtonIndexState.intValue == index,
                defaultButtonSize = defaultButtonSize / selectedButtonScale,
                buttonValue = value,
                selectedButtonScale = selectedButtonScale
            ) {
                selectedButtonIndexState.intValue = index
                onClickEvent(value.toFloat())
            }
        }
    }
}


//포즈 추천 버튼을 누르면 나오는 버튼 배열
@Composable
fun ClickPoseBtnUnderBar(
    modifier: Modifier = Modifier,
    poseList: List<PoseData>?,
    galleryImageUri: Uri?,
    initScale: Float = 1F,
    currentSelectedIdx: Int,
    onRefreshPoseData: () -> Unit,
    onClickShutterBtn: () -> Unit,
    onGalleryButtonClickEvent: () -> Unit,
    onClickCloseBtn: () -> Unit,
    onSelectedPoseIndexEvent: (Int) -> Unit,
    onChangeScale: (Float) -> Unit
) {

    val galleryImageState by rememberUpdatedState(newValue = galleryImageUri)

    BackHandler(onBack = onClickCloseBtn) //뒤로가기 버튼을 누르면 이전 화면으로 돌아감.

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        if (poseList != null && currentSelectedIdx > 0) {
            val trackingValue = remember {
                mutableFloatStateOf(initScale)
            }
            LaunchedEffect(initScale) {
                trackingValue.floatValue = initScale
            }

            Slider(
                colors = SliderDefaults.colors(
                    thumbColor = LocalColors.current.primaryGreen100,
                    activeTrackColor = LocalColors.current.primaryGreen100,
                    inactiveTrackColor = LocalColors.current.secondaryWhite80,
                    activeTickColor = LocalColors.current.primaryGreen100,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(width = (LocalConfiguration.current.screenWidthDp / 1.5).dp)
                    .padding(bottom = 10.dp),
                value = trackingValue.floatValue,
                steps = 10,
                valueRange = 0.5F.rangeTo(2F),
                onValueChange = {
                    trackingValue.floatValue = it
                    onChangeScale(it)
                })
        }


        //포즈 선택 할 수 있는 Row -> 선택된 포즈를 가지고 스케일 변경 진행
        PoseSelectRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            currentSelectedIdx = currentSelectedIdx,
            poseList = poseList,
            onSelectedPoseIndexEvent = {
                onSelectedPoseIndexEvent(it)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //창 닫는 버튼
            GalleryImageButton(
                galleryImageUri = galleryImageState,
                buttonSize = 60.dp,
                onClickEvent = onGalleryButtonClickEvent
            )

            //셔터 버튼
            CameraScreenButtons.ShutterButton(
                onClickEvent = onClickShutterBtn,
                buttonSize = 80.dp
            )

            //포즈 새로고침 버튼
            CameraScreenButtons.NormalButton(
                buttonName = "포즈 새로고침",
                innerIconDrawableSize = 20.dp,
                colorTint = Color.Black,
                innerIconDrawableId = R.drawable.refresh,
                onClick = onRefreshPoseData,
                buttonSize = 60.dp
            )
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PoseSelectRow(
    modifier: Modifier = Modifier,
    currentSelectedIdx: Int,
    poseList: List<PoseData>?,
    onSelectedPoseIndexEvent: (Int) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val nowSelected by rememberUpdatedState(newValue = currentSelectedIdx)
    val rememberCoroutineScope = rememberCoroutineScope()
    val scrollBySnap = remember { mutableStateOf(false) }
    val scrollByClick = remember { mutableStateOf(false) }
    val localDensity = LocalDensity.current
    val rowWidth =
        remember { derivedStateOf { with(localDensity) { scrollState.layoutInfo.viewportSize.width.toDp() } } }

    val padding by rememberUpdatedState(newValue = rowWidth.value / 3F + 10.dp)


    fun onClickedItem(idx: Int) {
        /*코루틴을 컴포저블에서 사용하기 위해서는 rememberCoroutineScope()를 사용해야 함.*/
        rememberCoroutineScope.launch {

            if (scrollBySnap.value.not()) {
                scrollByClick.value = true //클릭으로 스크롤 했다고 알림.
                val itemInfo =
                    scrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == idx }
                if (itemInfo != null) {
                    val center = scrollState.layoutInfo.viewportEndOffset / 2
                    val childCenter = itemInfo.offset + (itemInfo.size / 2)
                    scrollState.animateScrollBy((childCenter - center).toFloat())
                } else {
                    scrollState.animateScrollToItem(idx)
                }
                onSelectedPoseIndexEvent(idx)
            } else scrollBySnap.value = false

        }
    }





    LazyRow(
        modifier = modifier.background(LocalColors.current.secondaryWhite80), state = scrollState,
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(
            horizontal = padding,
            vertical = 10.dp
        ),
        flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(scrollState)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (poseList != null) {
            itemsIndexed(poseList) { idx, posedata ->
                PoseSelectionItem(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    isSelected = idx == nowSelected,
                    imageUri = posedata.imageUri,
                    poseIndex = idx,
                    onClickEvent = {
                        onClickedItem(idx)
                    }, poseSize = 70
                )
            }
        } else { //아직 포즈를 찾고 있는 중 일 때
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularWaitingBar()
                    Text(
                        text = "포즈 추천 중..",
                        textAlign = TextAlign.Center,
                        fontFamily = CameraScreenButtons.pretendardFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .padding(top = 5.dp)
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
    poseSize: Int,
    onClickEvent: () -> Unit
) {
    val colorTheme = LocalColors.current

    val unSelectedColor = colorTheme.secondaryWhite100
    val selectedColor = colorTheme.primaryGreen100
    val stateColor = if (isSelected) selectedColor else unSelectedColor
    val defaultModifier = Modifier
        .wrapContentSize()
        .clickable(
            interactionSource = MutableInteractionSource(),
            indication = rememberRipple(
                color = if (isSelected) selectedColor
                else unSelectedColor,
                bounded = true,
                radius = poseSize.dp / 2
            )
        ) { onClickEvent() }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                imageUri.run {
                    this ?: R.drawable.impossible_icon
                }
            )
            .size(with(LocalDensity.current) {
                poseSize.dp.toPx().toInt()
            }) //현재 버튼의 크기만큼 리사이징한다.
            .build()
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val checkedPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.selected_icon)
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
                            .size(poseSize.dp)
                            .zIndex(1F)
                    ) {
                        Image(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size((poseSize / 2).dp),
                            painter = checkedPainter, contentDescription = "",
                            colorFilter = ColorFilter.tint(color = Color.White)
                        )
                    }
                }
                Card(
                    modifier = defaultModifier
                ) {
                    Image(
                        modifier = Modifier
                            .background(color = stateColor)
                            .padding(2.dp)
                            .size(poseSize.dp),
                        painter = painter,
                        contentDescription = "이미지",
                        contentScale = ContentScale.Fit,
                        colorFilter = if (imageUri == null) ColorFilter.tint(color = Color.Black) else null
                    )
                }
            }
        }

        Text(
            text = if (imageUri == null) "없음" else "포즈 #$poseIndex",
            textAlign = TextAlign.Center,
            fontFamily = CameraScreenButtons.pretendardFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier
                .width(poseSize.dp)
                .padding(top = 5.dp)
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
            poseId = 0,
            imageUri = null,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseCat = 1
        )


    )
    ClickPoseBtnUnderBar(
        modifier = Modifier.fillMaxWidth(),
        poseList = poseList,
        onSelectedPoseIndexEvent = {},
        onClickCloseBtn = {},
        onClickShutterBtn = {},
        onRefreshPoseData = {},
        currentSelectedIdx = 0,
        galleryImageUri = null,
        onGalleryButtonClickEvent = {

        },
        onChangeScale = {

        }
    )
}
