package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.ui.graphics.Color


class ProPoseColors(
    val primaryGreen100: Color = ppMainGreen,
    val primaryGreen80: Color = ppMainGreen.copy(alpha = 0.8f),
    val primaryGreen50: Color = ppMainGreen.copy(alpha = 0.5f),
    val secondaryWhite100: Color = ppMainWhite,
    val secondaryWhite80: Color = ppMainWhite.copy(alpha = 0.8f),
    val subPrimaryBlack100: Color = ppMainBlack,
    val subPrimaryBlack80: Color = ppMainBlack.copy(alpha = 0.8f),
    val subSecondaryGray100: Color = ppMainGray,
    val subSecondaryGray80: Color = ppMainGray.copy(alpha = 0.8f),
    val isLight: Boolean
) {
    fun copy(
        primary100: Color = this.primaryGreen100,
        primary80: Color = this.primaryGreen80,
        primary50: Color = this.primaryGreen50,
        secondary100: Color = this.secondaryWhite100,
        secondary80: Color = this.secondaryWhite80,
        subPrimary100: Color = this.subPrimaryBlack100,
        subPrimary80: Color = this.subPrimaryBlack80,
        subSecondary100: Color = this.subSecondaryGray100,
        subSecondary80: Color = this.subSecondaryGray80,
        isLight: Boolean = this.isLight
    ) = ProPoseColors(
        primary100,
        primary80,
        primary50,
        secondary100,
        secondary80,
        subPrimary100,
        subPrimary80,
        subSecondary100,
        subSecondary80,
        isLight
    )

    companion object {
            private val ppMainGreen = Color(0xFF98C0FF)
        private val ppMainBlack = Color(0xFF212121)
        private val ppMainGray = Color(0xFF999999)
        private val ppMainWhite = Color(0xFFFAFAFA)
    }
}
