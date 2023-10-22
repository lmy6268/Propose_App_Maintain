package com.hanadulset.pro_poseapp.utils


data class DownloadState(
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFileCnt: Int = 0,
    val currentBytes: Long = 0L,
    val totalBytes: Long = 0,
)