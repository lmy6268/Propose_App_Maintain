package com.hanadulset.pro_poseapp.utils

import kotlinx.coroutines.flow.Flow

data class DownloadResponse(
    val state: Int,
    val data: Flow<DownloadState>? = null,
) {
    companion object {
        const val STATE_GOOD_TO_DOWNLOAD = 0
        const val STATE_ERROR_ON_DOWNLOAD = 1
    }
}