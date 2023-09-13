package com.example.proposeapplication.presentation.view.camera

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proposeapplication.presentation.R
import com.example.proposeapplication.presentation.ui.PretendardFamily
import com.example.proposeapplication.presentation.view.camera.CameraModules.CompositionArrow
import com.example.proposeapplication.presentation.view.camera.CameraModules.ExpandableButton
import com.example.proposeapplication.utils.pose.PoseData
import kotlinx.coroutines.delay


object CameraModules {
    @Composable
    fun ModeMenu(selectedIdx: MutableIntState, modifier: Modifier = Modifier) {
        val cameraModeList = stringArrayResource(id = R.array.camera_modes)
        Box(
            modifier = modifier
        ) {
            Box(
                Modifier
                    .background(
                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(18.dp)
                    )
                    .heightIn(30.dp)
                    .widthIn(210.dp)
                    .align(alignment = Alignment.CenterStart)
            ) {}
            Row(
                modifier = Modifier.align(alignment = Alignment.CenterStart),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in cameraModeList.indices) {
                    Box(modifier = Modifier
                        .heightIn(50.dp)
                        .widthIn(20.dp)
                        .background(
                            color = if (i == selectedIdx.intValue) MaterialTheme.colors.primary else Color(
                                0x00FFFFFF
                            ), shape = RoundedCornerShape(30.dp)
                        )
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            selectedIdx.intValue = i
                            Log.d("현재 모드 : ", cameraModeList[selectedIdx.intValue])
                        }

                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            text = cameraModeList[i],
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = PretendardFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (i == selectedIdx.intValue) Color(0xFF999999) else Color(
                                0xFF000000
                            ),
                        )
                    }
                }
            }
        }
    }

    //확장가능한 버튼
    @Composable
    fun ExpandableButton(
        text: List<String>,
        type: String,
        isExpandedState: MutableState<Boolean>,
        modifier: Modifier = Modifier,
        viewRateIdx: Int,
        onClick: (Int) -> Unit,
    ) {
        Box(modifier = modifier
            .apply {
                if (isExpandedState.value) fillMaxWidth()
                else widthIn(44.dp)
            }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200, easing = LinearEasing
                )
            ) //크기 변경이 감지되면 애니메이션을 추가해준다.

        ) {

            if (isExpandedState.value) Row(
                Modifier
                    .fillMaxWidth()
                    .offset(x = 20.dp)
            ) {
                Box(
                    Modifier.background(
                        color = Color(0x80FAFAFA), shape = RoundedCornerShape(30.dp)
                    )
                ) {
                    //닫는 버튼
                    IconButton(modifier = Modifier.heightIn(30.dp),
                        onClick = { isExpandedState.value = false }) {
                        Icon(
                            painterResource(id = R.drawable.based_circle),
                            tint = Color.Unspecified,
                            contentDescription = "background",

                            )
                        Icon(
                            painterResource(id = R.drawable.close),
                            contentDescription = "close",
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth(0.9F)
                            .align(Alignment.Center)
                            .padding(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        for (idx in text.indices) {
                            Text(modifier = Modifier.clickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                onClick(idx)
                            },
                                text = text[idx],
                                fontSize = 12.sp,
                                fontWeight = if (viewRateIdx == idx) FontWeight.Bold else FontWeight.Light,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
            }
            else IconButton(onClick = {
                isExpandedState.value = true
            }) {
                Icon(
                    modifier = Modifier
                        .widthIn(44.dp)
                        .heightIn(44.dp),
                    painter = painterResource(id = R.drawable.based_circle),
                    tint = Color.Unspecified,
                    contentDescription = type
                )
                Text(
                    text = text[viewRateIdx], //화면 비 글씨 표기
                    fontWeight = FontWeight(FontWeight.Bold.weight), fontSize = 12.sp
                )

            }

        }
    }

    //상단 부분
    @Composable
    fun UpperButtons(
        modifier: Modifier = Modifier,
        navController: NavHostController,
        viewRateIdx: Int,
        mainColor: Color = MaterialTheme.colors.primary,
        mainTextColor: Color = MaterialTheme.colors.onPrimary,
        subColor: Color = MaterialTheme.colors.onSecondary,
        subTextColor: Color = MaterialTheme.colors.onSecondary,
        selectedModeIdxState: MutableIntState,
        viewRateClickEvent: (Int) -> Unit
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val viewRateList = stringArrayResource(id = R.array.view_rates)
        Row(
            modifier = modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            //오버레이 되는 확장 가능한 버튼
            ExpandableButton(
                text = viewRateList.toList(),
                type = "비율",
                viewRateIdx = viewRateIdx,
                isExpandedState = isExpandedState,
                onClick = viewRateClickEvent

            )
            //확장 가능한 버튼이 확장 되지 않은 경우
            if (!isExpandedState.value) {
                ModeMenu(
                    selectedModeIdxState,
                )
                NormalButton(
                    buttonName = "설정", iconDrawableId = R.drawable.settings
                ) {
                    //설정화면으로 이동
                }

            }
        }


    }


    @Composable
    fun NormalButton(
        buttonName: String, modifier: Modifier = Modifier, iconDrawableId: Int, onClick: () -> Unit
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.based_circle),
                tint = Color.Unspecified,
                contentDescription = buttonName
            )
            Icon(
                painter = painterResource(
                    id = iconDrawableId
                ), contentDescription = "$buttonName 아이콘"
            )
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    @Composable
    fun LowerButtons(
        selectedModeIdxState: MutableIntState,
        modifier: Modifier = Modifier,
        capturedImageBitmap: Bitmap, //캡쳐된 이미지의 썸네일을 받아옴.
        fixedButtonPressedEvent: () -> Unit = {}, //고정 버튼 누름 이벤트 인식
        poseBtnClickEvent: () -> Unit = {},
        captureImageEvent: () -> Unit = { },
        zoomInOutEvent: (Float) -> Unit = {}
    ) {
        val isFixedBtnPressed = remember {
            mutableStateOf(false)
        }
        val fixedBtnImage =
            if (isFixedBtnPressed.value) R.drawable.fixbutton_fixed
            else R.drawable.fixbutton_unfixed
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {

        }


        //현재 줌 상태
        val zoomState = remember {
            mutableStateOf("")
        }

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else if (isFocused) R.drawable.ic_shutter_focused
        else R.drawable.ic_shutter_normal

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically)
        ) {
            //첫번째 단
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedModeIdxState.intValue == 0 || selectedModeIdxState.intValue == 1) IconButton(
                    modifier = Modifier.heightIn(15.dp),
                    onClick = {
                        poseBtnClickEvent()
                    }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(
                            0x80000000
                        ),
                        contentDescription = "background",
                    )
                    Text(
                        style = TextStyle(
                            color = Color.White, fontSize = 10.sp
                        ), text = "포즈\n추천"
                    )
                }
                else IconButton(modifier = Modifier.heightIn(15.dp), onClick = {

                }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(
                            0x00000000
                        ),
                        modifier = Modifier.size(15.dp),
                        contentDescription = "background",
                    )
                    Text(
                        style = TextStyle(
                            color = Color(
                                0x00000000
                            ), fontSize = 10.sp
                        ), text = "포즈\n추천"
                    )
                }


                Row {
                    listOf("1", "2").forEach { str ->
                        IconButton(
                            onClick = {
                                zoomState.value = str
                                zoomInOutEvent(str.toFloat())
                            },
                        ) {
                            Icon(
                                modifier = Modifier.apply {
                                    if (str == zoomState.value) sizeIn(50.dp)
                                    else sizeIn(10.dp)
                                },
                                painter = painterResource(id = R.drawable.based_circle),
                                tint = if (str == zoomState.value) Color(0xFF000000) else Color.Unspecified,
                                contentDescription = "줌버튼"
                            )
                            Text(
                                text = if (str == zoomState.value) "${str}X" else str,
                                style = MaterialTheme.typography.h2,
                                fontWeight = if (str == zoomState.value) FontWeight.Bold else FontWeight.Light,
                                color = if (str == zoomState.value) Color(0xFFFFFFFF) else Color(
                                    0xFF000000
                                ),
                            )
                        }

                    }
                }
                IconButton(modifier = Modifier.heightIn(15.dp), onClick = {

                }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        tint = Color(0x80000000),
                        contentDescription = "background",
                    )
                    Text(
                        style = TextStyle(
                            color = Color.White, fontSize = 10.sp
                        ), text = "따오기"
                    )
                }


            }
            //두번쨰 단
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    30.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //캡쳐 썸네일 이미지 뷰 -> 원형으로 표사
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFA))
                        .paint(
                            painter = painterResource(R.drawable.based_circle),
                            contentScale = ContentScale.FillBounds
                        )
                        .clickable(
                            interactionSource = MutableInteractionSource(), indication = null
                        ) {
                            openGallery(launcher)
                        },
                    contentScale = ContentScale.Crop,
                    bitmap = capturedImageBitmap.asImageBitmap(),
                    contentDescription = "캡쳐된 이미지"
                )
                //캡쳐 버튼
                Box(modifier = Modifier.clickable(
                    indication = null, //Ripple 효과 제거
                    interactionSource = interactionSource
                ) {
                    captureImageEvent()
                }) {
                    Icon(
                        modifier = Modifier.size(80.dp),
                        painter = painterResource(id = buttonImg),
                        tint = if (!isFocused && !isPressed) MaterialTheme.colors.secondary
                        else Color.Unspecified,
                        contentDescription = "촬영버튼"
                    )
                }

                Box(Modifier.clickable(
                    indication = null, // Remove ripple effect
                    interactionSource = MutableInteractionSource()
                ) {
                    isFixedBtnPressed.value = !isFixedBtnPressed.value
                    fixedButtonPressedEvent()
                }) {
                    Icon(
                        modifier = Modifier.size(60.dp),
                        painter = painterResource(id = fixedBtnImage),
                        tint = Color.Unspecified,
                        contentDescription = "고정버튼"
                    )
                }


            }
        }

    }


    //구도 추천 관련 UI
    @Composable
    fun CompositionArrow(arrowDirection: String, modifier: Modifier = Modifier) {
        val tint = Color.Unspecified
        val size = 50.dp
        Box(modifier = modifier) {
            when (arrowDirection) {
                "L" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 10.dp),
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "왼쪽 화살표 ",
                        tint = tint
                    )
                }

                "R" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.CenterEnd)
                            .padding(horizontal = 10.dp),
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "오른쪽 화살표 ",
                        tint = tint
                    )
                }

                "U" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.TopCenter)
                            .offset(y = 80.dp)
                            .padding(vertical = 10.dp),
                        painter = painterResource(id = R.drawable.up_arrow),
                        contentDescription = "위쪽 화살표 ",
                        tint = tint
                    )
                }

                "D" -> {
                    Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-70).dp)
                            .padding(vertical = 10.dp),
                        painter = painterResource(id = R.drawable.down_arrow),
                        contentDescription = "아래쪽 화살표 ",
                        tint = tint
                    )
                }

                "S" -> {
                    var show by remember { mutableStateOf(true) }

                    LaunchedEffect(key1 = Unit) {
                        delay(2000) //2초만 보여줌
                        show = false
                    }
                    if (show) Icon(
                        modifier = Modifier
                            .size(size)
                            .align(Alignment.Center),
                        painter = painterResource(id = R.drawable.good_comp),
                        contentDescription = "좋은 구도",
                        tint = tint
                    )
                }

                else -> {}
            }

        }

    }

    @Composable
    fun PoseResultScreen(
        modifier: Modifier = Modifier,
        cameraDisplaySize: State<IntSize>,
        cameraDisplayPxSize: State<IntSize>,
        lowerBarDisplayPxSize: State<IntSize>,
        upperButtonsRowSize: State<IntSize>,
        poseResultData: Pair<DoubleArray?, List<PoseData>?>?,
        onVisibilityEvent: () -> Unit
    ) {
        val selectedPoseState = remember {
            mutableIntStateOf(0)
        }

        val offset = remember {
            mutableStateOf(
                Offset(
                    cameraDisplayPxSize.value.width / 2f,
                    cameraDisplayPxSize.value.height / 2f
                )
            )
        }
        val zoom = remember {
            mutableFloatStateOf(1F)
        }

        val transformState =
            rememberTransformableState { zoomChange, offsetChange, _ ->
                if (zoom.floatValue * zoomChange in 0.5f..2f) zoom.floatValue *= zoomChange
                val tmp = offset.value + offsetChange
                if (tmp.x in -1f..cameraDisplayPxSize.value.width.toFloat() - 1
                    && tmp.y in -1f..cameraDisplayPxSize.value.height.toFloat() - lowerBarDisplayPxSize.value.height.toFloat() + 20F
                )
                    offset.value += offsetChange
            }

        Box(modifier = modifier) {
            //만약에 포즈를 추천 중이라면
            if (poseResultData == null) {
                CircularProgressIndicator(
                    modifier = Modifier.sizeIn(30.dp).align(Alignment.Center)
                )
            }
            //만약에 포즈 추천이 완료되었다면
            else {
                Box(
                    Modifier.size(
                        cameraDisplaySize.value.width.dp,
                        cameraDisplaySize.value.height.dp
                    )
                ) {
                    IconButton(
                        onClick = {
                            onVisibilityEvent()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .offset(x = 0.dp, y = upperButtonsRowSize.value.height.dp)
                            .align(Alignment.TopEnd)

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.based_circle),
                            contentDescription = "배경",
                            tint = Color.White
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "포즈 추천 닫기"
                        )
                    }

                    IconButton(modifier = Modifier
                        .size(50.dp)
                        .offset(x = (-20).dp)
                        .align(
                            Alignment.CenterEnd
                        ), onClick = {
                        if (selectedPoseState.intValue in 0 until poseResultData.second!!.size) selectedPoseState.intValue += 1
                        else selectedPoseState.intValue = 0
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.based_circle),
                            contentDescription = "배경",
                            tint = Color.White
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.refresh),
                            contentDescription = "배경"
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = zoom.floatValue,
                            scaleY = zoom.floatValue,
                            translationX = offset.value.x,
                            translationY = offset.value.y
                        )
                        .size(cameraDisplaySize.value.width.dp, cameraDisplaySize.value.height.dp)
                        .transformable(state = transformState)
                ) {
                    if (poseResultData.first!!.isNotEmpty()) {
//                        offset = offset.copy(
//                            (poseRecPair!!.first!![0] * size.width).toFloat(),
//                            (poseRecPair!!.first!![0] * size.height).toFloat()
//                        )
//                    }

//                    drawImage(
//                        image = BitmapFactory.decodeResource(
//                            context.resources,
//                            poseRecPair!!.second!![nextRecomPoseState.intValue].poseDrawableId
//                        ).asImageBitmap(),
//                    )
                        drawRect(
                            Color.White,
                            size = Size(200F, 200F)
                        )
                    }
                }
            }

        }
    }


    private fun openGallery(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        //실 기기에서는 안됨..
        launcher.launch(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}


//Test Preview
@Preview
@Composable
fun PreviewExpandableBtn() {
    ExpandableButton(
        text = listOf("Test", "TT"),
        type = "테스트",
        isExpandedState = remember { mutableStateOf(false) },
        viewRateIdx = 0,
    ) {}
}

@Preview
@Composable
fun PreviewMode() {
    CameraModules.ModeMenu(selectedIdx = remember { mutableIntStateOf(1) })
}


@Preview
@Composable
fun PreviewLower() {
//    LowerButtons(isPressedFixedBtn =,capturedImageBitmap =, mainViewModel =)
}


@Preview(widthDp = 250, heightDp = 150)
@Composable
fun PreviewArrow() {
    CompositionArrow("R")
}





