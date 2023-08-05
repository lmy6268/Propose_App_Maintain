package com.example.proposeapplication.presentation

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

class CustomFont {
    companion object {
        val font_pretendard = FontFamily(
            Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
            Font(R.font.pretendard_light, FontWeight.Light, FontStyle.Normal),
        )
    }
}