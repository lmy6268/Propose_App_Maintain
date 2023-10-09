package com.hanadulset.pro_poseapp.utils


data class DownloadState(
    val state: Int = STATE_ON_PROGRESS,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFileCnt: Int = 0,
    val currentBytes: Long = 0L,
    val totalBytes: Long = 0,
) {
    companion object {
        const val STATE_COMPLETE = 0
        const val STATE_ON_PROGRESS = 1
    }
}
