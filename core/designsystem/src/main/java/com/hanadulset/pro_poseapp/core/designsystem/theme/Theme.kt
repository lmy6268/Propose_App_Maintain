package com.hanadulset.pro_poseapp.core.designsystem.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


@VisibleForTesting
val LightDefaultColorScheme = customColorScheme(
    isDarkTheme = false,
    primary = Blue40,
    onPrimary = White100,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = DarkNavy40,
    onSecondary = White100,
    secondaryContainer = DarkNavy90,
    error = Red40,
    onError = White100,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = White98,
    onBackground = White10,
    surface = White100,
    onSurface = White10,
    surfaceVariant = White90,
    onSurfaceVariant = White30,
    inverseSurface = White20,
    inverseOnSurface = White95,
    outline = White50
)

@VisibleForTesting
val DarkDefaultColorScheme = customColorScheme(
    isDarkTheme = true,
    primary = Blue80,
    onPrimary = White20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = DarkNavy80,
    onSecondary = DarkNavy20,
    secondaryContainer = DarkNavy30,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = White10,
    onBackground = White90,
    surface = White10,
    onSurface = White90,
    surfaceVariant = White30,
    onSurfaceVariant = White80,
    inverseSurface = White90,
    inverseOnSurface = White20,
    outline = White60
)


@Composable
fun ProposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isEnabledDynamicTheme: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = if (isEnabledDynamicTheme && supportsDynamicTheming()) {
        with(LocalContext.current) {
            if (darkTheme) dynamicDarkColorScheme(this)
            else dynamicLightColorScheme(this)
        }
    } else {
        if (darkTheme) DarkDefaultColorScheme
        else LightDefaultColorScheme
    }

    CompositionLocalProvider() {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ProposeTypography,
            content = content
        )
    }

}


/** [customColorScheme]
 * @param isDarkTheme 다크 테마 여부를 판단.
 * @param primary 앱의 주요 부분에서 사용되며, 앱의 브랜드 색상
 * @param onPrimary 주요 색상 위에 그려지는 콘텐츠(텍스트, 아이콘 등)의 색상
 * @param primaryContainer 주요 색상을 사용하는 컨테이너의 배경 색상 (Text 컴포넌트를 포함하는 Box 컴포넌트는 "컨테이너")
 * @param onPrimaryContainer primaryContainer 색상 위에 그려지는 콘텐츠의 색상
 * @param secondary 보조 색상
 * @param onSecondary 보조 색상 위에 그려지는 콘텐츠의 색상
 * @param error 오류 메시지나 '실패' 상태를 나타내는데 사용
 * @param background 앱의 배경 색상
 * @param onBackground 배경 색상 위에 그려지는 콘텐츠의 색상
 * @param surface UI 요소(카드, 시트 등)의 배경 색상
 * @param onSurface surface 위에 그려지는 콘텐츠의 색상
 *  @param inverseSurface Surface 색상의 반대색
 *  @param inverseOnSurface onSurface 색상의 반대색
 *  @param outline 외곽선 색상
 * @return [ColorScheme] - 색 스키마를 반환.
 * */
private fun customColorScheme(
    isDarkTheme: Boolean,
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    error: Color,
    onError: Color,
    errorContainer: Color,
    onErrorContainer: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    inverseSurface: Color,
    inverseOnSurface: Color,
    outline: Color,
): ColorScheme {
    return if (isDarkTheme) darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        outline = outline,
    )
    else lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        outline = outline,
    )
}

// supportsDynamicTheming() 함수는 애플리케이션이 동적 테마를 지원하는지 여부를 결정하는 함수
// Android S 이상의 버전에서만 동적 테마가 지원되므로, 이 함수는 현재 실행 중인 Android 버전이 Android S 이상인지 확인
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
