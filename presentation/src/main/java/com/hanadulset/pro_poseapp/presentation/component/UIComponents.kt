package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hanadulset.pro_poseapp.presentation.R
import com.hanadulset.pro_poseapp.presentation.component.UIComponents.SettingBoxItem
import com.hanadulset.pro_poseapp.presentation.feature.camera.CameraScreenButtons.SwitchableButton
import kotlin.reflect.KProperty
import kotlin.reflect.KType

object UIComponents {

    // 원형으로 도는 대기 바
    @Composable
    fun CircularWaitingBar(
        modifier: Modifier = Modifier,
        barSize: Dp = 60.dp,
        barColor: Color = LocalColors.current.primaryGreen100,
        backgroundColor: Color = LocalColors.current.subSecondaryGray100
    ) {
        CircularProgressIndicator(
            modifier = modifier.size(barSize),
            color = barColor,
            backgroundColor = backgroundColor,
            strokeCap = StrokeCap.Round,
            strokeWidth = 8.dp
        )
    }


    //구도 버튼 아이콘
    @Composable
    fun CompIconButton(

    ) {

    }


    //슬라이드 되는 애니메이션 적용할 컴포넌트
    @Composable
    fun AnimatedSlideToLeft(
        modifier: Modifier = Modifier,
        isVisible: Boolean,
        enterDuration: Int = 150,
        exitDuration: Int = 250,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = isVisible,
            enter = slideInHorizontally(animationSpec = tween(
                durationMillis = enterDuration, easing = LinearOutSlowInEasing
            ), initialOffsetX = { fullHeight -> -fullHeight }).plus(fadeIn()),
            exit = slideOutHorizontally(animationSpec = tween(
                durationMillis = exitDuration, easing = LinearOutSlowInEasing
            ), targetOffsetX = { fullHeight -> -fullHeight }).plus(fadeOut()),
            content = content
        )
    }

    //슬라이드 되는 애니메이션 적용할 컴포넌트
    @Composable
    fun AnimatedSlideToRight(
        modifier: Modifier = Modifier,
        isVisible: Boolean,
        enterDuration: Int = 150,
        exitDuration: Int = 250,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = isVisible,
            enter = slideInHorizontally(animationSpec = tween(
                durationMillis = enterDuration, easing = LinearOutSlowInEasing
            ), initialOffsetX = { fullHeight -> fullHeight }).plus(fadeIn()),
            exit = slideOutHorizontally(animationSpec = tween(
                durationMillis = exitDuration, easing = LinearOutSlowInEasing
            ), targetOffsetX = { fullHeight -> fullHeight }).plus(fadeOut()),
            content = content
        )
    }


    //앱 정보 및 기능 설정 화면에서 사용할 박스 디자인
    @Composable
    fun SettingBoxItem(
        modifier: Modifier = Modifier,
        innerText: String,
        innerTextSize: Dp = 23.dp,
        onClick: (() -> Unit)? = null
    ) {
        val iconPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.right_arrow)
                .placeholder(R.drawable.right_arrow)
                .build()
        )


        Box(
            modifier = modifier
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                        ) {
                            onClick()
                        }
                    } else Modifier
                )
                .background(
                    shape = RoundedCornerShape(50.dp),
                    color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.2F)
                )
                .padding(start = 20.dp, end = 10.dp)
                .padding(vertical = 10.dp)

        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = innerText,
                style = LocalTypography.current.sub01,
                fontSize = LocalDensity.current.run {
                    (innerTextSize * 0.7F).toSp()
                }
            )

            Icon(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(innerTextSize),
                painter = iconPainter,
                contentDescription = "화살표"
            )
        }

    }

//    @OptIn(ExperimentalMaterialApi::class)
//    @Composable
//    fun SettingBoxItemWithDropMenu(
//        modifier: Modifier = Modifier,
//        innerText: String,
//        innerTextSize: Dp = 10.dp,
//        onClick: (() -> Unit)? = null,
//        selectedIdx: Int,
//        itemList: List<Int>,
//        onChangeIndex: (Int) -> Unit = {}
//    ) {
//        Box(
//            modifier = modifier
//                .then(
//                    if (onClick != null) {
//                        Modifier.clickable(
//                            interactionSource = MutableInteractionSource(),
//                            indication = null,
//                        ) {
//                            onClick()
//                        }
//                    } else Modifier
//                )
//                .background(
//                    shape = RoundedCornerShape(50.dp),
//                    color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.2F)
//                )
//                .padding(start = 20.dp, end = 10.dp)
//                .padding(vertical = 10.dp)
//
//        ) {
//            Text(
//                modifier = Modifier.align(Alignment.CenterStart),
//                text = innerText,
//                style = LocalTypography.current.sub01,
//                fontSize = LocalDensity.current.run {
//                    (innerTextSize * 0.7F).toSp()
//                }
//            )
//            var expanded by remember { mutableStateOf(false) }
//            var selectedOptionText by remember { mutableIntStateOf(itemList[selectedIdx]) }
//            ExposedDropdownMenuBox(
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    .width(70.dp)
//                    .height(innerTextSize + 30.dp),
//                expanded = expanded,
//                onExpandedChange = {
//                    expanded = !expanded
//                }
//            ) {
//                TextField(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .height(innerTextSize + 10.dp),
//                    readOnly = true,
//                    textStyle = LocalTypography.current.sub02.copy(
//                        fontSize = LocalDensity.current.run { innerTextSize.toSp() },
//                        lineHeight = LocalDensity.current.run { innerTextSize.toSp() }),
//                    value = "$selectedOptionText",
//                    onValueChange = { },
//                    trailingIcon = {
//                        ExposedDropdownMenuDefaults.TrailingIcon(
//                            expanded = expanded
//                        )
//                    },
//                    colors = ExposedDropdownMenuDefaults.textFieldColors(
//                        backgroundColor = LocalColors.current.secondaryWhite100,
//                        trailingIconColor = LocalColors.current.subPrimaryBlack100,
//                        focusedTrailingIconColor = LocalColors.current.subPrimaryBlack100,
//                        focusedIndicatorColor = LocalColors.current.subPrimaryBlack100,
//                        unfocusedIndicatorColor = LocalColors.current.subPrimaryBlack100,
//
//                        )
//                )
//                ExposedDropdownMenu(
//                    modifier = Modifier
//                        .align(Alignment.Center),
//                    expanded = expanded,
//                    onDismissRequest = {
//                        expanded = false
//                    }
//                ) {
//                    itemList.forEachIndexed { idx, selectionOption ->
//                        DropdownMenuItem(
//                            onClick = {
//                                selectedOptionText = selectionOption
//                                onChangeIndex(idx)
//                                expanded = false
//                            }
//                        ) {
//                            Text(
//                                text = "$selectionOption",
//                                style = LocalTypography.current.sub02.copy(fontSize = LocalDensity.current.run { innerTextSize.toSp() })
//                            )
//                        }
//                    }
//                }
//            }
//        }

//    }

    @Composable
    fun SettingBoxItemWithToggle(
        modifier: Modifier = Modifier,
        innerText: String,
        innerTextSize: Dp = 23.dp,
        isToggled: Boolean,
        onToggleEvent: (Boolean) -> Unit
    ) {
        Box(
            modifier = modifier
                .background(
                    shape = RoundedCornerShape(50.dp),
                    color = LocalColors.current.subSecondaryGray100.copy(alpha = 0.2F)
                )
                .padding(horizontal = 20.dp)
                .padding(vertical = 10.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = innerText,
                style = LocalTypography.current.sub01,
                fontSize = LocalDensity.current.run {
                    (innerTextSize * 0.7F).toSp()
                }
            )
            SwitchableButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                init = isToggled,
                negativeColor = LocalColors.current.subPrimaryBlack100,
                positiveColor = LocalColors.current.primaryGreen100,
                onChangeState = onToggleEvent,
                scale = 1F,
                buttonSize = DpSize(width = innerTextSize * 1.7F, height = innerTextSize)
            )
        }
    }


}

@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
@Composable
fun TestSettingBoxItem() {
    SettingBoxItem(
        modifier = Modifier.fillMaxWidth(),
        innerText = "문의",
        onClick = {

        }
    )
}

//@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
//@Composable
//fun TestSettingBoxItemList() {
//    UIComponents.SettingBoxItemWithDropMenu(
//        modifier = Modifier.fillMaxWidth(),
//        innerText = "문의",
//        onClick = {
//
//        },
//        itemList = listOf(1, 2, 3, 4, 5),
//        selectedIdx = 0
//    )
//}
