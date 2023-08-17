package com.example.proposeapplication.presentation.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//흰 박스 위에 버튼들이 배치될 때
private val overlaidPalette = Colors(
    primary = mainGreen,
    primaryVariant = mainGreen,
    secondary = subGray,
    secondaryVariant = subGray,
    background = Color.White,
    surface = Color.White,
    error = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
    isLight = true
)

//메인 색 테마
@Composable
fun MainTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = overlaidPalette,
        typography = typography,
        content = content
    )
}