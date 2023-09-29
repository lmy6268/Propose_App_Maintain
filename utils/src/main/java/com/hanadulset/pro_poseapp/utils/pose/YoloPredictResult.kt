package com.hanadulset.pro_poseapp.utils.pose

import android.graphics.Rect

data class YoloPredictResult(
    val classIdx: Int, val score: Float, val box: Rect
)
