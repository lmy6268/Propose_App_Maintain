package com.hanadulset.pro_poseapp.utils.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//Int형 사이즈
@Parcelize
data class ProPoseSize(
    val width: Int,
    val height: Int
) : Parcelable
