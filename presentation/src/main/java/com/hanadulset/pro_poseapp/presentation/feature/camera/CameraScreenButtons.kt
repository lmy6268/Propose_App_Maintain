package com.hanadulset.pro_poseapp.presentation.feature.camera

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.LocalColors
import com.hanadulset.pro_poseapp.presentation.component.LocalTypography
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.SwitchableButton

object CameraScreenButtons {
    val pretendardFamily = FontFamily(
        Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
    )

    private object LocalButtonRippleTheme : RippleTheme {

        private var defaultColor = Color.White
        private var alphaColor = Color.Black
        fun setRippleEffect(
            defaultColor: Color = Color.Unspecified,
            alphaColor: Color = Color.Black
        ) {
            this.defaultColor = defaultColor
            this.alphaColor = alphaColor
        }

        @Composable
        override fun defaultColor() = RippleTheme.defaultRippleColor(
            defaultColor, lightTheme = true
        )

        @Composable
        override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
            alphaColor, lightTheme = true
        )
    }

    @Composable
    fun ToggledButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 30.dp,
        buttonStatus: Boolean,
        activatedColor: Color = LocalColors.current.primaryGreen100,
        inActivatedColor: Color = LocalColors.current.subSecondaryGray80,
        buttonDescription: String = "버튼",
        buttonText: String = "",
        buttonTextColor: Color = Color.White,
        iconDrawableId: Int = R.drawable.based_circle,
        innerIconDrawableId: Int? = null,
        onClickEvent: () -> Unit
    ) {
        val buttonState by rememberUpdatedState(newValue = buttonStatus)

        CompositionLocalProvider(LocalRippleTheme provides LocalButtonRippleTheme.apply {
            setRippleEffect(
                alphaColor = if (buttonState) activatedColor else inActivatedColor,
                defaultColor = Color.Black
            )
        }) {
            IconButton(
                modifier = modifier.size(buttonSize), onClick = onClickEvent
            ) {
                Icon(
                    modifier = modifier.size(buttonSize),
                    painter = painterResource(id = iconDrawableId),
                    contentDescription = buttonDescription,
                    tint = if (buttonState) activatedColor else inActivatedColor
                )
                if (buttonText != "") Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    color = if (buttonState.not()) buttonTextColor else Color.Black,
                    fontFamily = pretendardFamily,
                    fontWeight = FontWeight.Bold
                )
                else if (innerIconDrawableId != null) Icon(
                    painter = painterResource(id = innerIconDrawableId),
                    tint = if (buttonState.not()) buttonTextColor else Color.Black,
                    modifier = Modifier.size(buttonSize / 2),
                    contentDescription = "내부 아이콘"
                )

            }
        }
    }

    @Composable
    fun ParticularZoomButton(
        selected: Boolean,
        defaultButtonSize: Dp,
        buttonValue: Int,
        selectedButtonScale: Float = 2F,
        selectedButtonColor: Color = LocalColors.current.subPrimaryBlack100,
        unSelectedButtonColor: Color = LocalColors.current.secondaryWhite100,
        onClickEvent: () -> Unit
    ) {

        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(
                        color = Color(0xFF999999), bounded = true, radius = defaultButtonSize / 2
                    ), //Ripple 효과 제거,
                    onClick = onClickEvent
                )
                .size(defaultButtonSize)
                .scale(
                    if (selected) selectedButtonScale else 1F
                )

        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.based_circle),
                contentDescription = "줌버튼",
                tint = if (selected) selectedButtonColor else unSelectedButtonColor
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = if (selected) "${buttonValue}X" else buttonValue.toString(),
                fontSize = 10.sp,
                color = if (selected) LocalColors.current.primaryGreen100 else LocalColors.current.subPrimaryBlack100,
                fontFamily = pretendardFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Light

            )
        }
    }

    @Composable
    fun SwitchableButton(
        modifier: Modifier = Modifier,
        init: Boolean,
        buttonSize: DpSize = DpSize(23.dp, 15.dp),
        positiveColor: Color,
        negativeColor: Color,
        onChangeState: (Boolean) -> Unit,
        scale: Float = 2f,
        strokeWidth: Dp = (buttonSize.height / 10),
        gapBetweenThumbAndTrackEdge: Dp = (buttonSize.width / 9),
    ) {

        val switchON = rememberSaveable { mutableStateOf(init) }
        val thumbRadius = (buttonSize.height / 2) - gapBetweenThumbAndTrackEdge
        // To move thumb, we need to calculate the position (along x axis)
        val animatePosition =
            animateFloatAsState(targetValue = if (switchON.value) with(LocalDensity.current) { (buttonSize.width - thumbRadius - gapBetweenThumbAndTrackEdge).toPx() }
            else with(LocalDensity.current) { (thumbRadius + gapBetweenThumbAndTrackEdge).toPx() },
                label = ""
            )
        Column(modifier) {
            Canvas(
                modifier = Modifier
                    .size(buttonSize)
                    .scale(scale = scale)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            // This is called when the user taps on the canvas
                            switchON.value = !switchON.value
                            onChangeState(switchON.value)
                        })
                    }) {
                // Track
                drawRoundRect(
                    color = if (switchON.value) positiveColor else negativeColor,
                    cornerRadius = CornerRadius(
                        x = buttonSize.height.toPx(),
                        y = buttonSize.height.toPx()
                    ),
                    style = Stroke(width = strokeWidth.toPx())
                )

                // Thumb
                drawCircle(
                    color = if (switchON.value) positiveColor else negativeColor,
                    radius = thumbRadius.toPx(),
                    center = Offset(
                        x = animatePosition.value, y = size.height / 2
                    )
                )
            }

        }


    }


    @Composable
    fun FixedButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 80.dp,
        onFixedButtonPressedEvent: () -> Unit,
        fixedBtnStatus: Boolean,
    ) {
        val isFixedBtnPressed by rememberUpdatedState(newValue = fixedBtnStatus)

        val fixedBtnImage = if (isFixedBtnPressed) R.drawable.fixbutton_fixed
        else R.drawable.fixbutton_unfixed

        val activatedColor = LocalColors.current.primaryGreen100
        val inActivatedColor = LocalColors.current.subSecondaryGray100



        CompositionLocalProvider(LocalRippleTheme provides LocalButtonRippleTheme.apply {
            setRippleEffect(
                defaultColor = if (isFixedBtnPressed) activatedColor
                else inActivatedColor, alphaColor = Color.Black
            )
        }) {

            Surface(
                modifier = modifier
                    .wrapContentSize()
                    .clickable(
                        indication = rememberRipple(
                            color = if (isFixedBtnPressed) activatedColor.compositeOver(
                                Color.Black
                            ) else inActivatedColor.compositeOver(Color.Black),
                            bounded = true,
                            radius = buttonSize / 2
                        ), //Ripple 효과 제거
                        interactionSource = MutableInteractionSource(),
                        onClick = onFixedButtonPressedEvent
                    ), shape = CircleShape
            ) {
                Icon(
                    modifier = Modifier.size(buttonSize),
                    painter = painterResource(id = fixedBtnImage),
                    tint = Color.Unspecified,
                    contentDescription = "고정버튼"
                )
            }
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
        val imageDrawable = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current).data(buttonImg)
                .size(LocalDensity.current.run {
                    buttonSize.toPx().toInt()
                }) //뷰 사이즈의 크기 만큼 이미지 리사이징
                .build()
        )

        Surface(
            modifier = modifier
                .clickable(
                    indication = rememberRipple(
                        color = LocalColors.current.subSecondaryGray100,
                        bounded = true,
                        radius = buttonSize / 2
                    ), //Ripple 효과 제거
                    interactionSource = interactionSource, onClick = onClickEvent
                ), shape = CircleShape
        ) {
            Icon(
                modifier = modifier.size(buttonSize),
                tint = Color.Unspecified,
                painter = imageDrawable,
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
                    drawCircle(color = Color.Gray.copy(alpha = 0.3f))
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
    @Composable
    fun ExpandableButton(
        itemList: List<String>, // 내부에 들어갈 값
        type: String,  // 속성 값
        modifier: Modifier = Modifier,
        onSelectedItemEvent: (Int) -> Unit,
        isExpanded: (Boolean) -> Unit,
        defaultButtonSize: Dp = 44.dp,
        defaultButtonColor: Color = LocalColors.current.subPrimaryBlack100,
        triggerClose: Boolean,
    ) {
        val isExpandedState = remember {
            mutableStateOf(false)
        }
        val selectedIndexState = rememberSaveable { mutableIntStateOf(0) }
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


        if (triggerClose) closeExpandedWindow()



        Box(modifier = modifier
            .animateContentSize(
                //크기 변경이 감지되면 애니메이션을 추가해준다.
                animationSpec = tween(
                    durationMillis = 200, easing = LinearEasing
                )
            )
            .onGloballyPositioned { coordinates ->
                coordinates.size.let {
                    expandableBtnSize.value = DpSize(it.width.dp, it.height.dp)
                }
            }

        ) {
            if (isExpandedState.value) Box(
                modifier
                    .height(defaultButtonSize)
                    .fillMaxWidth()
                    .background(
                        color = defaultButtonColor.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                //닫는 버튼
                IconButton(modifier = Modifier
                    .size(defaultButtonSize)
                    .align(Alignment.CenterStart),
                    onClick = {
                        closeExpandedWindow()
                    }) {
                    Icon(
                        painterResource(id = R.drawable.based_circle),
                        modifier = Modifier.border(
                            width = 3.dp,
                            shape = CircleShape,
                            color = defaultButtonColor.copy(alpha = 0.8f)
                        ),
                        tint = Color.White,
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
                        .wrapContentHeight()
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (idx in itemList.indices) {
                        Box(modifier = Modifier
                            .clickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                selectedIndexState.intValue = idx
                                onSelectedItemEvent(idx)
                            }
                            .wrapContentSize()
                            .padding(10.dp)) {
                            Text(
                                text = itemList[idx],
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (selectedIndexState.intValue == idx) FontWeight.Bold else FontWeight.Light,
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }
            }
            else IconButton(
                modifier = modifier, onClick = onBtnClicked
            ) {
                Icon(
                    modifier = Modifier
                        .size(defaultButtonSize)
                        .border(
                            BorderStroke(2.dp, LocalColors.current.subPrimaryBlack100),
                            shape = CircleShape
                        ),
                    painter = painterResource(id = R.drawable.based_circle),
                    tint = LocalColors.current.secondaryWhite100,
                    contentDescription = type
                )
                Text(
                    color = LocalColors.current.subPrimaryBlack100,
                    text = itemList[selectedIndexState.intValue], //화면 비 글씨 표기
                    fontWeight = FontWeight(FontWeight.Bold.weight),
                    fontSize = 14.sp
                )
            }

        }
    }

    @Composable
    fun NormalButton(
        modifier: Modifier = Modifier,
        buttonSize: Dp = 20.dp,
        buttonName: String,
        innerIconDrawableId: Int? = null,
        innerIconDrawableSize: Dp = 10.dp,
        innerIconColorTint: Color = Color.White,
        buttonText: String? = null,
        buttonTextSize: Int = 10,
        buttonTextColor: Color = Color.White,
        colorTint: Color = Color(0x80FAFAFA),
        onClick: () -> Unit
    ) {
        IconButton(
            modifier = modifier.size(buttonSize),
            onClick = onClick,
        ) {
            Icon(
                modifier = modifier.size(buttonSize),
                painter = painterResource(id = R.drawable.based_circle),
                tint = colorTint,
                contentDescription = buttonName
            )
            if (buttonText != null) Text(
                text = buttonText,
                fontSize = buttonTextSize.sp,
                color = buttonTextColor,
                fontFamily = pretendardFamily,
                fontWeight = FontWeight.Bold
            )
            if (innerIconDrawableId != null) Icon(
                modifier = Modifier.size(innerIconDrawableSize), painter = painterResource(
                    id = innerIconDrawableId
                ), contentDescription = "$buttonName 아이콘", tint = innerIconColorTint
            )
        }
    }

}

@Composable
@Preview
fun TestSwitch() {
    SwitchableButton(init = false,
        positiveColor = Color(0x99999999),
        negativeColor = Color(0xFFFFFF00),
        modifier = Modifier,
        onChangeState = {

        })
}

//@Composable
//@Preview
//fun TestToggleBtn() {
//    ToggledButton(modifier = Modifier.sizeIn(10.dp, 10.dp),
//        initState = true,
//        activatedColor = Color(0xFF95FA99),
//        inActivatedColor = Color(0x80999999),
//        buttonText = "테스트용",
//        onClickEvent = {
//
//        })
//}