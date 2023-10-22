package com.hanadulset.pro_poseapp.utils


//다운로드 여부 체크 후 응답값
data class CheckResponse(
    val needToDownload: Boolean = false, //다운로드 필요 여부
    val downloadType: Int = TYPE_MUST_DOWNLOAD, // 다운로드의 경우
    val totalSize: Long = 0, //총 다운로드 해야하는 데이터의 바이트 수
    val hasRemainStorage: Boolean = false, // 잔여 공간의 여부
) {
    companion object {
        const val TYPE_NEED_CONNECTION = 10
        const val TYPE_MUST_DOWNLOAD = 0 //필수 요소가 없어 다운로드가 필요한 경우
        const val TYPE_ADDITIONAL_DOWNLOAD = 1 //업데이트 등과 같은 경우
        const val TYPE_ERROR = -1
    }
}
