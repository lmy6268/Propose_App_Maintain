package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import android.net.Uri
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import com.hanadulset.pro_poseapp.utils.eventlog.FeedBackData


/**파일 생성 및 저장 등을 관리하는 데이터소스*/
interface FileHandleDataSource {

    /**촬영된 이미지를 갤러리에 저장한다.*/
    suspend fun saveImageToGallery(bitmap: Bitmap): Uri



    suspend fun loadCapturedImages(isReadAllImage: Boolean): List<ImageResult>
    fun deleteCapturedImage(uri: Uri): Boolean

    /**모델을 다운로드하고 파일로 저장한다.
     * 1. 현재 저장되어있는 각 파일의 버전 정보를 확인한다.
     * 1. 서버에 있는 값과 비교를 하여 차이가 있는 경우에는 각각에 대해 hasNewUpdate 값을 true로 변경한다.
     * 1. 만약 업데이트가 필요하다면, 혹은 파일이 없다면 사용자에게 다운로드에 대한 요청을 진행한다.
     * 1.  사용자가 허가한 경우, 모델을 다운로드 받는다.
     * 1. 다운로드가 완료되면, 버전 정보를 업데이트 한다.
     * 1. 기존의 파일을 삭제하고, 다운로드 된 파일을 사용한다. ( 다운로드 된 파일명을 처음엔 new_를 붙여서 저장하여 분리한다. )
     * 1. 업데이트를 완료한다.
     * */



    suspend fun sendFeedBackData(feedBackData: FeedBackData) //피드백 데이터를 서버로 보냄
}