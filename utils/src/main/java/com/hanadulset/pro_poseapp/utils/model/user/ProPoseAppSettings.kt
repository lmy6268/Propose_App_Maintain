package com.hanadulset.pro_poseapp.utils.model.user

import kotlinx.serialization.Serializable

@Serializable
data class ProPoseAppSettings(
    val isCompRecommendationEnabled: Boolean = true,
    val isPoseRecommendationEnabled: Boolean = true,
    val maxRecommendedPoseCnt: Int = 10
)
