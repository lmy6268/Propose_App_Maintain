package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hanadulset.pro_poseapp.presentation.R


data class ProPoseTypography(
    val heading01: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    val heading02: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    val heading03: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    ),
    val sub01: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp
    ),
    val sub02: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )


) {
    companion object {
        val PretendardFamily = FontFamily(
            Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
            Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
        )
    }

}
