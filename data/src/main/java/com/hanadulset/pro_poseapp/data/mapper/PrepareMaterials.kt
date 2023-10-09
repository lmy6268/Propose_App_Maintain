package com.hanadulset.pro_poseapp.data.mapper

import kotlinx.serialization.Serializable

@Serializable
data class PrepareMaterials(
    val keyImages: KeyImages,
    val models: List<String>
)