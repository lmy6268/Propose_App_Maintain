package com.hanadulset.pro_poseapp.core.designsystem.custom_component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//// 원형으로 도는 대기 바
//@Composable
//fun CircularWaitingBar(
//    modifier: Modifier = Modifier,
//    barSize: Dp = 60.dp,
//    barColor: Color = LocalColors.current.primaryGreen100,
//    backgroundColor: Color = LocalColors.current.subSecondaryGray100
//) {
//    CircularProgressIndicator(
//        modifier = modifier.size(barSize),
//        color = barColor,
//        trackColor = backgroundColor,
//        strokeCap = StrokeCap.Round,
//        strokeWidth = 8.dp
//    )
//}