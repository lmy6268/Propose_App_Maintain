package com.hanadulset.pro_poseapp.presentation.component

import androidx.compose.ui.graphics.Color


class ProPoseColors(
    private val primary100: Color = ppMainGreen,
    private val primary80: Color = ppMainGreen.copy(alpha = 0.8f),
    private val primary50: Color = ppMainGreen.copy(alpha = 0.5f),
    private val secondary100: Color = ppMainWhite,
    private val secondary80: Color = ppMainWhite.copy(alpha = 0.8f),
    private val background100: Color = ppMainWhite,
    private val subPrimary100: Color = ppMainBlack,
    private val subPrimary80: Color = ppMainBlack.copy(alpha = 0.8f),
    private val subSecondary100: Color = ppMainGray,
    private val subSecondary80: Color = ppMainGray.copy(alpha = 0.8f),
    val isLight: Boolean
) {
    fun copy(
        primary100: Color = this.primary100,
        primary80: Color = this.primary80,
        primary50: Color = this.primary50,
        secondary100: Color = this.secondary100,
        secondary80: Color = this.secondary80,
        background100: Color = this.background100,
        subPrimary100: Color = this.subPrimary100,
        subPrimary80: Color = this.subPrimary80,
        subSecondary100: Color = this.subSecondary100,
        subSecondary80: Color = this.subSecondary80,
        isLight: Boolean = this.isLight
    ) = ProPoseColors(
        primary100,
        primary80,
        primary50,
        secondary100,
        secondary80,
        background100,
        subPrimary100,
        subPrimary80,
        subSecondary100,
        subSecondary80,
        isLight
    )

    companion object {
        val ppMainGreen = Color(0xFF95FFA7)
        val ppMainBlack = Color(0xFF212121)
        val ppMainGray = Color(0xFF999999)
        val ppMainWhite = Color(0xFFFAFAFA)
    }
}
