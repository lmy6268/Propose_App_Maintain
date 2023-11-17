package com.hanadulset.pro_poseapp.utils

import kotlinx.serialization.Serializable

@Serializable
data class UserSet(
    var isCompOn: Boolean = true,
    val isPoseOn: Boolean = true,
    val poseCnt: Int = 10
)
