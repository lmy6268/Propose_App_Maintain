package com.hanadulset.pro_poseapp.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.SizeF
import com.hanadulset.pro_poseapp.utils.model.camera.ProPoseImageModel
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult

//이미지 저장 및 분석을 담당하는 레포지토리
interface ImageRepository {
    suspend fun getRecommendCompInfo(backgroundBitmap: Bitmap): Pair<Float, Float>
    suspend fun getRecommendPose(
        backgroundBitmap: Bitmap
    ): PoseDataResult //추천된 포즈데이터 반환하기

    fun getFixedScreen(backgroundBitmap: Bitmap): Bitmap // 고정 화면을 보여줌
    suspend fun getLatestImage(): Uri?
    suspend fun preRunModel(): Boolean
    fun getPoseFromImage(uri: Uri?): Bitmap?
    suspend fun loadAllCapturedImages(): List<ProPoseImageModel>
    suspend fun deleteCapturedImage(uri: Uri): Boolean

    suspend fun updateOffsetPoint(backgroundBitmap: Bitmap, targetOffset: SizeF): SizeF?
    fun stopPointOffset()

}