//package com.hanadulset.pro_poseapp.presentation.ui_components
//
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material.Colors
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Shapes
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.staticCompositionLocalOf
//import androidx.compose.ui.platform.LocalConfiguration
//import com.hanadulset.pro_poseapp.presentation.ui_components.Dimensions.Companion.smallDimensions
//
//@Composable
//fun ProvideDimens(
//    dimensions: Dimensions,
//    content: @Composable () -> Unit
//) {
//    val dimensionSet = remember { dimensions }
//    CompositionLocalProvider(localAppDimens provides dimensionSet, content = content)
//}
//
//@Composable
//fun ProvideColors(
//    palette: ColorPalette,
//    content: @Composable () -> Unit
//) {
//    val colorSet = remember { palette }
//    CompositionLocalProvider() {
//
//    }
//}
//
//
//private val localAppDimens = staticCompositionLocalOf { smallDimensions }
//private val localAppColorPalette = staticCompositionLocalOf {  }
//
//@Composable
//fun AppTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable () -> Unit
//) {
//    val colors = if (darkTheme) DarkThemeColors else LightThemeColors
//    val configuration = LocalConfiguration.current
//    val dimensions = if (configuration.screenWidthDp <= 360) smallDimensions else sw360Dimensions
//    val typography = if (configuration.screenWidthDp <= 360) smallTypography else sw360Typography
//
//    ProvideDimens(dimensions = dimensions) {
//        ProvideColors(colors = colors) {
//            MaterialTheme(
//                colors = colors,
//                shapes = Shapes,
//                typography = typography,
//                content = content,
//            )
//        }
//    }
//}
//
//object AppTheme {
//    val colors: Colors
//        @Composable
//        get() = LocalAppCol.current
//
//    val dimens: Dimensions
//        @Composable
//        get() = localAppDimens.current
//}
//
//val Dimens: Dimensions
//    @Composable
//    get() = AppTheme.dimens
