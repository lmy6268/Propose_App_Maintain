package com.hanadulset.pro_poseapp.utils

import java.lang.Exception


/**
 *
 * [DownloadInfo] : 다운로드 상태를 가지고 있는 객체
 *
 *  <**_variables_**>
 * - [state] : 새롭게 다운로드 받는지, 아니면 업데이트 중인지 ( 0:Skip, 1: DownLoad, 2: Update)
 * - [nowIndex] : 현재 다운로드 중인 파일의 인덱스
 * - [totalLength] : 다운로드 받아야 할 전체 파일 수
 * - [byteCurrent] : 현재 다운로드 된 파일의 바이트 수
 * - [byteTotal] : 현재 다운도드 중인 파일의 총 용향
 * */
data class DownloadInfo(
    val state: Int = ON_SKIP,
    val nowIndex: Int = 0,
    val totalLength: Int = 0,
    val byteCurrent: Long = 0,
    val byteTotal: Long = 0,
    val errorException: Exception? = null
) {
    companion object {
        const val ON_SKIP = 0
        const val ON_DOWNLOAD = 1
        const val ON_UPDATE = 2
        const val ON_REQUEST = 3
        const val ON_COMPLETE = 4
        const val ON_ERROR = 5

    }
}
