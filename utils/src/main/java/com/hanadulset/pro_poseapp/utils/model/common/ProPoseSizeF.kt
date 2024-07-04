package com.hanadulset.pro_poseapp.utils.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProPoseSizeF(
    val width: Float,
    val height: Float,
    val center: ProPoseOffSet = ProPoseOffSet(width / 2, height / 2)
) : Parcelable



