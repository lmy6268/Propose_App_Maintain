package com.example.proposeapplication.presentation.ui

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.proposeapplication.presentation.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val PretendardFamily = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
)
val typography = Typography(
    defaultFontFamily = PretendardFamily,
    h1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
//    button = TextStyle(
//        fontWeight = FontWeight()
//    )
)