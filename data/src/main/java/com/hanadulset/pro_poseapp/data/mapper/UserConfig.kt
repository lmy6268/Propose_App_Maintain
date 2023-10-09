package com.hanadulset.pro_poseapp.data.mapper

import kotlinx.serialization.Serializable

@Serializable
data class UserConfig(
    val prepareMaterials: PrepareMaterials,
    val settings: Settings
)