package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.scale
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ParticularZoomButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.ToggledButton
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
        lowerLayerPaddingBottom: Dp = 0.dp,
        aboveSize: Dp,
    ) {
        //필요할 때만 리컴포지션을 진행함.
        //https://kotlinworld.com/256 참고
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

        val edgeDetectBtnClickEvent = remember<(Boolean) -> Unit> {
            {
                onEdgeDetectEvent(it)
                disturbFixedButtonState.value = true
                disturbEdgeDetectorButtonState.value = false
            }
        }

        val galleryThumbUri by rememberUpdatedState(newValue = galleryImageUri)

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            UpperLayer(
                modifier = Modifier.fillMaxWidth(),
                onEdgeDetectEvent = edgeDetectBtnClickEvent,
                onZoomLevelChangeEvent = onZoomLevelChangeEvent,
                disturbFromFixedButton = disturbEdgeDetectorButtonState.value,
                onRecommendPoseEvent = onPoseRecommendEvent
            )
            LowerLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = lowerLayerPaddingBottom),
                onShutterClickEvent = onShutterClickEvent,
                onGalleryButtonClickEvent = onGalleryButtonClickEvent,
                onFixedButtonClickEvent = fixedBtnClickEvent,
                galleryImageUri = galleryThumbUri,
                disturbFromEdgeDetector = disturbFixedButtonState.value,
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
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //포즈 추천 버튼 -> 토글 형식이 아니었던가..?
        CameraScreenButtons.NormalButton(
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
            customEventValue = isDisturbed,
        )
    }


}

// 갤러리 버튼, 셔터 버튼, 고정 버튼이 위치
@Composable
fun LowerLayer(
    modifier: Modifier = Modifier,
    galleryImageUri: Uri?,
    disturbFromEdgeDetector: Boolean,
    onShutterClickEvent: () -> Unit = {},
    onGalleryButtonClickEvent: () -> Unit = {},
    onFixedButtonClickEvent: (Boolean) -> Unit = {}
) {

    //개별 상태변수를 가지고 있으므로써, 의도치 않은 리컴포지션을 방지
    val galleryImageState by rememberUpdatedState(newValue = galleryImageUri)
    val isFixedDisturbed by rememberUpdatedState(newValue = disturbFromEdgeDetector)

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
            buttonSize = 100.dp
        ) { onShutterClickEvent() }

        //고정
        CameraScreenButtons.FixedButton(
            modifier = Modifier,
            buttonSize = 60.dp,
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
    GlideImage(imageModel = { galleryImageUri },
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
        })
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

@Composable
fun ClickPoseBtnUnderBar(
    modifier: Modifier = Modifier,
    poseList: List<PoseData>?,
    onRefreshPoseData: () -> Unit,
    onClickShutterBtn: () -> Unit,
    onClickCloseBtn: () -> Unit,
    onSelectedPoseIndexEvent: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //포즈 선택 할 수 있는 Row
        PoseSelectRow(
            modifier = Modifier.fillMaxWidth(),
            poseList = poseList,
            onSelectedPoseIndexEvent = onSelectedPoseIndexEvent
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //창 닫는 버튼
            CameraScreenButtons.NormalButton(
                buttonName = "닫기",
                onClick = onClickCloseBtn,
                buttonText = "˅",
                buttonSize = 44.dp,
                colorTint = Color.Black,
                buttonTextSize = 30
            )

            //셔터 버튼
            CameraScreenButtons.ShutterButton(
                onClickEvent = onClickShutterBtn, buttonSize = 100.dp
            )

            //포즈 새로고침 버튼
            CameraScreenButtons.NormalButton(
                buttonName = "포즈 새로고침",
                innerIconDrawableSize = 20.dp,
                colorTint = Color.Black,
                innerIconDrawableId = R.drawable.refresh,
                onClick = onRefreshPoseData,
                buttonSize = 44.dp
            )
        }
    }


}


@Composable
fun PoseSelectRow(
    modifier: Modifier = Modifier,
    poseList: List<PoseData>?,
    onSelectedPoseIndexEvent: (Int) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val nowSelected = remember { mutableIntStateOf(0) }

    LazyRow(
        modifier = modifier,
        state = scrollState,
        horizontalArrangement = Arrangement.Center
    ) {
        if (poseList != null) itemsIndexed(poseList) { idx, posedata ->
            PoseSelectionItem(
                isSelected = idx == nowSelected.intValue,
                drawableId = posedata.poseDrawableId,
                poseIndex = idx,
                onClickEvent = {
                    nowSelected.intValue = idx
                    onSelectedPoseIndexEvent(idx)
                }
            )
        }
        else { //아직 포즈를 찾고 있는 중 일 때
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 10.dp),
                        color = Color(0xFF95FA99)
                    )
                }
            }
        }

    }
}

@Composable
fun PoseSelectionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    drawableId: Int,
    poseIndex: Int,
    onClickEvent: () -> Unit
) {
    val resources = LocalContext.current.resources
    val drawableIDSaved = rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    val localBitmap = rememberSaveable { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .clickable {
                onClickEvent()
            }
            .size(100.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = if (drawableId == -1) "없음" else "포즈 #$poseIndex",
            textAlign = TextAlign.Center,
            fontSize = 7.sp,
            modifier = Modifier
                .border(1.dp, Color.Black)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 5.dp)
        )

        if (drawableId == -1) {
            Box(
                modifier = Modifier
                    .background(color = Color(0x80999999))
                    .border(1.dp, Color.Black)
                    .fillMaxSize()
            )
        } else {
            LaunchedEffect(Unit) {
                if (drawableIDSaved.value != drawableId) {
                    var bitMap: Bitmap?
                    BitmapFactory.Options().run {
                        BitmapFactory.decodeResource(resources, drawableId)
                        calculateInSampleSize(this)
                        bitMap = BitmapFactory.decodeResource(resources, drawableId, this)
                    }
                    drawableIDSaved.value = drawableId
                    localBitmap.value = bitMap
                }
            }

            GlideImage(
                modifier = Modifier
                    .background(color = Color(0x80999999))
                    .border(1.dp, Color.Black)
                    .fillMaxSize(),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Fit,
                ),
                imageModel = { localBitmap.value },
//                clearTarget = true
            )
        }


    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    val MAX_HEIGHT = 10
    val MAX_WIDTH = 10

    var inSampleSize = 1

    if (height > MAX_HEIGHT || width > MAX_WIDTH) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= MAX_HEIGHT && halfWidth / inSampleSize >= MAX_WIDTH) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

@Composable
@Preview
fun PreviewLowerLayer() {
    LowerLayer(modifier = Modifier.fillMaxWidth(), onShutterClickEvent = {

    }, onGalleryButtonClickEvent = {

    }, galleryImageUri = null, disturbFromEdgeDetector = false
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
        onPoseRecommendEvent = {},
        aboveSize = 0.dp
    )
}


@Composable
@Preview
fun PrePoseSelector() {
    val poseList = listOf(
        PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_1,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_2,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_3,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_4,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_5,
            poseCat = 1
        )

    )

    PoseSelectRow(
        modifier = Modifier.fillMaxWidth(),
        poseList = poseList,
        onSelectedPoseIndexEvent = {}
    )
}


@Composable
@Preview
fun PreviewSelector() {
    val poseList = listOf(
        PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_1,
            poseCat = 1
        ),
        PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_2,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_3,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_4,
            poseCat = 1
        ), PoseData(
            poseId = 0,
            poseDrawableId = com.hanadulset.pro_poseapp.utils.R.drawable.key_image_5,
            poseCat = 1
        )

    )
    ClickPoseBtnUnderBar(
        Modifier.fillMaxWidth(),
        poseList = poseList,
        onSelectedPoseIndexEvent = {},
        onClickCloseBtn = {},
        onClickShutterBtn = {},
        onRefreshPoseData = {}
    )
}
