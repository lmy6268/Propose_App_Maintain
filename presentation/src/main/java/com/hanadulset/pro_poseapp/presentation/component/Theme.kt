package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

//https://velog.io/@vov3616/Compose-Custom-Theme-%EB%A7%8C%EB%93%A4%EA%B8%B0

//메인 색 테마
@Composable
fun ProPoseTheme(
    colors: ProPoseColors = ProPoseTheme.colors,
    typography: ProPoseTypography = ProPoseTheme.typography,
    darkColors: ProPoseColors? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val currentColor = remember {
        if (darkColors != null && darkTheme) darkColors else colors
    }
    //변경에 recomposition이 최소화되기위해 부분적인 업데이트가 필요하다.
    val rememberedColors = remember { currentColor.copy() }

    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalTypography provides typography
    ) {
//        //Typography의 TextStyle은 조금 다르게 선언된다.
        ProvideTextStyle(typography.heading01, content = content)
    }

}

val LocalColors = staticCompositionLocalOf { ProPoseColors(isLight = true) }
val LocalTypography = staticCompositionLocalOf { ProPoseTypography() }

object ProPoseTheme {
    val colors: ProPoseColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: ProPoseTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

//    val spaces: CustomSpaces
//        @Composable
//        @ReadOnlyComposable
//        get() = LocalSpaces.current
}

