package com.hanadulset.pro_poseapp.presentation.feature.camera

import android.view.MotionEvent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModuleExtension.SwitchableButton
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraModuleExtension.ToggledButton

object CameraModuleExtension {
    private val pretendardFamily = FontFamily(
        Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
    )

    @Composable
    fun ToggledButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 30.dp,
        initState: Boolean,
        activatedColor: Color = Color(0xFF95FA99),
        inActivatedColor: Color = Color(0x80999999),
        buttonDescription: String = "버튼",
        buttonText: String = "",
        buttonTextColor: Color = Color.White,
        iconDrawableId: Int = R.drawable.based_circle,
        onClickEvent: (Boolean) -> Unit
    ) {
        val buttonState = remember {
            mutableStateOf(initState)
        }
        val onClicked by rememberUpdatedState(newValue = {
            buttonState.value = buttonState.value.not()
            onClickEvent(buttonState.value)
        })

        IconButton(
            onClick = onClicked,
            modifier = modifier
                .size(buttonSize)
        ) {
            if (buttonText != "") Text(
                text = buttonText,
                fontSize = (buttonSize.value.toInt() / 5).sp, color = buttonTextColor
            )
            Icon(
                painter = painterResource(id = iconDrawableId),
                contentDescription = buttonDescription,
                tint = if (buttonState.value) activatedColor else inActivatedColor
            )
        }


    }

    @Composable
    fun ParticularZoomButton(
        selected: Boolean,
        defaultButtonSize: Dp,
        buttonValue: Int,
        selectedButtonScale: Float = 2F,
        selectedButtonColor: Color = Color(0xFF000000),
        unSelectedButtonColor: Color = Color.Unspecified,
        onClickEvent: () -> Unit
    ) {

        IconButton(
            onClick = onClickEvent,
            modifier = Modifier
                .size(defaultButtonSize)
                .scale(
                    if (selected) selectedButtonScale else 1F
                )
        ) {

            Icon(
                painter = painterResource(id = R.drawable.based_circle),
                contentDescription = "줌버튼",
                tint = if (selected) selectedButtonColor else unSelectedButtonColor
            )
            Text(
                text = "${buttonValue}X",
                fontSize = 6.sp,
                color = if (selected) Color.White else Color.Black,
                fontFamily = pretendardFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Light

            )
        }
    }

    @Composable
    fun SwitchableButton(
        modifier: Modifier = Modifier,
        innerText: String,
        init: Boolean,
        buttonSize: DpSize = DpSize(23.dp, 15.dp),
        positiveColor: Color,
        negativeColor: Color,
        onChangeState: () -> Unit,
        scale: Float = 2f,
        strokeWidth: Dp = (buttonSize.height / 10),
        gapBetweenThumbAndTrackEdge: Dp = (buttonSize.width / 9),
    ) {

        val switchON = remember { mutableStateOf(init) }
        val thumbRadius = (buttonSize.height / 2) - gapBetweenThumbAndTrackEdge
        // To move thumb, we need to calculate the position (along x axis)
        val animatePosition = animateFloatAsState(
            targetValue = if (switchON.value)
                with(LocalDensity.current) { (buttonSize.width - thumbRadius - gapBetweenThumbAndTrackEdge).toPx() }
            else
                with(LocalDensity.current) { (thumbRadius + gapBetweenThumbAndTrackEdge).toPx() },
            label = ""
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                fontSize = 10.sp,
                fontFamily = pretendardFamily,
                fontWeight = FontWeight.Bold,
                text = "$innerText ${if (switchON.value) "On" else "Off"}"
            )
            Column {
                Canvas(
                    modifier = Modifier
                        .size(buttonSize)
                        .scale(scale = scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    // This is called when the user taps on the canvas
                                    switchON.value = !switchON.value
                                    onChangeState()
                                }
                            )
                        }
                ) {
                    // Track
                    drawRoundRect(
                        color = if (switchON.value) positiveColor else negativeColor,
                        cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx()),
                        style = Stroke(width = strokeWidth.toPx())
                    )

                    // Thumb
                    drawCircle(
                        color = if (switchON.value) positiveColor else negativeColor,
                        radius = thumbRadius.toPx(),
                        center = Offset(
                            x = animatePosition.value,
                            y = size.height / 2
                        )
                    )
                }

            }
        }


    }


    @Composable
    fun FixedButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 80.dp,
        fixedButtonPressedEvent: (Boolean) -> Unit
    ) {
        val isFixedBtnPressed = remember {
            mutableStateOf(false)
        }
        val onClicked by rememberUpdatedState(newValue = {
            isFixedBtnPressed.value = !isFixedBtnPressed.value
            fixedButtonPressedEvent(isFixedBtnPressed.value)
        })

        val fixedBtnImage = if (isFixedBtnPressed.value) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed

        Box(
            modifier.clickable(
                indication = CustomIndication, // Remove ripple effect
                interactionSource = MutableInteractionSource(),
                onClick = onClicked
            )
        ) {
            Icon(
                modifier = Modifier.size(buttonSize),
                painter = painterResource(id = fixedBtnImage),
                tint = Color.Unspecified,
                contentDescription = "고정버튼"
            )
        }
    }

    @Composable
    fun ShutterButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 80.dp,
        onClickEvent: () -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        //버튼 이미지 배치
        val buttonImg = if (isPressed) R.drawable.ic_shutter_pressed
        else R.drawable.ic_shutter_normal

        Box(
            modifier = modifier.clickable(
                indication = null, //Ripple 효과 제거
                interactionSource = interactionSource,
                onClick = onClickEvent
            )

        ) {
            Icon(
                modifier = modifier.size(buttonSize),
                tint = Color.Unspecified,
                painter = painterResource(id = buttonImg),
                contentDescription = "촬영버튼"
            )
        }

    }

    object CustomIndication : Indication {
        private class DefaultDebugIndicationInstance(
            private val isPressed: State<Boolean>,
        ) : IndicationInstance {
            override fun ContentDrawScope.drawIndication() {
                drawContent()
                if (isPressed.value) {
                    drawCircle(color = Color.Gray.copy(alpha = 0.1f))
                }
            }
        }

        @Composable
        override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
            val isPressed = interactionSource.collectIsPressedAsState()
            return remember(interactionSource) {
                DefaultDebugIndicationInstance(isPressed)
            }
        }
    }

    //확장가능한 버튼
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ExpandableButton(
        itemList: List<String>, // 내부에 들어갈 값
        type: String,  // 속성 값
        modifier: Modifier = Modifier,
        onSelectedItemEvent: (Int) -> Unit,
        isExpanded: (Boolean) -> Unit
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val selectedIndexState = remember {
            mutableIntStateOf(0)
        }
        val closeExpandedWindow by rememberUpdatedState(newValue = {
            isExpandedState.value = false
            isExpanded(false)
        })
        val onBtnClicked by rememberUpdatedState(newValue = {
            isExpandedState.value = true
            isExpanded(true)
        })

        val expandableBtnSize = remember {
            mutableStateOf(DpSize(100.dp, 100.dp))
        }
        val localDensity = LocalDensity.current


        Box(modifier = modifier
            .animateContentSize(
                //크기 변경이 감지되면 애니메이션을 추가해준다.
                animationSpec = tween(
                    durationMillis = 200, easing = LinearEasing
                )
            )
            .apply {
                if (isExpandedState.value) fillMaxWidth()
                else widthIn(44.dp)
            }
            .onGloballyPositioned { coordinates ->
                coordinates.size.let {
                    expandableBtnSize.value = DpSize(it.width.dp, it.height.dp)
                }
            }
            .pointerInteropFilter {
                //만약 외부 선택시 현재 화면을 닫음.
                if (it.action == MotionEvent.ACTION_DOWN) {
                    with(localDensity) {
                        if (it.x > expandableBtnSize.value.width.toPx() || it.y > expandableBtnSize.value.height.toPx()) {
                            closeExpandedWindow()
                            true
                        } else false
                    }
                } else false

            }
        ) {
            if (isExpandedState.value)
                Row(
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
                            onClick = {
                                closeExpandedWindow()
                            }) {
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
                        //선택지 화면
                        Row(
                            Modifier
                                .fillMaxWidth(0.9F)
                                .align(Alignment.Center)
                                .padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            for (idx in itemList.indices) {
                                Box(modifier = Modifier
                                    .wrapContentSize()
                                    .padding(10.dp)
                                    .clickable(indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        selectedIndexState.intValue = idx
                                        onSelectedItemEvent(idx)
                                    }) {
                                    Text(
                                        text = itemList[idx],
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedIndexState.intValue == idx) FontWeight.Bold else FontWeight.Light,
                                        textAlign = TextAlign.Center
                                    )
                                }

                            }
                        }
                    }
                }
            else IconButton(
                onClick = onBtnClicked
            ) {
                Icon(
                    modifier = Modifier
                        .widthIn(44.dp)
                        .heightIn(44.dp),
                    painter = painterResource(id = R.drawable.based_circle),
                    tint = Color.Unspecified,
                    contentDescription = type
                )
                Text(
                    text = itemList[selectedIndexState.intValue], //화면 비 글씨 표기
                    fontWeight = FontWeight(FontWeight.Bold.weight), fontSize = 12.sp
                )
            }

        }
    }


}

@Composable
@Preview
fun TestSwitch() {
    SwitchableButton(
        init = false,
        positiveColor = Color(0x99999999),
        negativeColor = Color(0xFFFFFF00),
        innerText = "구도",
        modifier = Modifier,
        onChangeState = {

        }
    )
}

@Composable
@Preview
fun TestToggleBtn() {
    ToggledButton(
        modifier = Modifier.sizeIn(10.dp, 10.dp),
        initState = true,
        activatedColor = Color(0xFF95FA99),
        inActivatedColor = Color(0x80999999),
        buttonText = "테스트용",
        onClickEvent = {

        }
    )
}