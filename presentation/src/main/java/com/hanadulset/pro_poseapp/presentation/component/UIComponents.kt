package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

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
            modifier = modifier
                .size(barSize),
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
            enter = slideInHorizontally(
                animationSpec = tween(
                    durationMillis = enterDuration,
                    easing = LinearOutSlowInEasing
                ),
                initialOffsetX = { fullHeight -> -fullHeight }
            ).plus(fadeIn()),
            exit = slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = exitDuration,
                    easing = LinearOutSlowInEasing
                ),
                targetOffsetX = { fullHeight -> -fullHeight }
            ).plus(fadeOut()),
            content = content
        )
    }
}

