package com.hanadulset.pro_poseapp.domain.repository

import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.util.Size
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.utils.CheckResponse
import com.hanadulset.pro_poseapp.utils.DownloadResponse
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import com.hanadulset.pro_poseapp.utils.pose.PoseData

//이미지 저장 및 분석을 담당하는 레포지토리
interface ImageRepository {
    suspend fun getRecommendCompInfo(image: Image, rotation: Int): Pair<String, Int>
    suspend fun getRecommendPose(
        image: Image, rotation: Int
    ): List<PoseData> //추천된 포즈데이터 반환하기

    fun getFixedScreen(backgroundBitmap: Bitmap): Bitmap // 고정 화면을 보여줌
    fun getFixedScreen(imageProxy: ImageProxy): Bitmap
    suspend fun getLatestImage(): Uri?
    suspend fun downloadResources(): DownloadResponse
    suspend fun checkForDownloadResources(): CheckResponse
    suspend fun preRunModel(): Boolean
    fun getPoseFromImage(uri: Uri?): Bitmap?
    suspend fun loadAllCapturedImages(): List<ImageResult>
    suspend fun deleteCapturedImage(uri: Uri): Boolean

    suspend fun updateOffsetPoint(image: Image, targetOffset: SizeF, rotation: Int): SizeF?

}