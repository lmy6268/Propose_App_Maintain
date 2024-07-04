package com.hanadulset.pro_poseapp.utils.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProPoseOffSet(
    val x: Float,
    val y: Float
) : Parcelable

