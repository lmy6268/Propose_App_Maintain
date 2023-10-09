package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadResponse

interface DownloadResourcesDataSource {
    suspend fun checkForDownload(): CheckResponse

    suspend fun startToDownload(): DownloadResponse
}