package com.hanadulset.pro_poseapp.utils.camera

import android.graphics.Color
import androidx.core.graphics.toColor

data class FocusRingState(
    val offset: Pair<Float, Float>? = null,
    val size: Int = 40,
    val color: Color = Color.WHITE.toColor(),
    val durationMill: Long = 3000 //ms
)
