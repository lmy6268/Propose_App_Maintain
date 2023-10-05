package com.hanadulset.pro_poseapp.domain.repository

import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.hanadulset.pro_poseapp.utils.DownloadInfo
import com.hanadulset.pro_poseapp.utils.camera.ImageResult
import com.hanadulset.pro_poseapp.utils.pose.PoseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//이미지 저장 및 분석을 담당하는 레포지토리
interface ImageRepository {
    suspend fun getRecommendCompInfo(image: Image, rotation: Int): Pair<String, Int>?
    suspend fun getRecommendPose(
        image: Image, rotation: Int
    ): Pair<DoubleArray, List<PoseData>> //추천된 포즈데이터 반환하기

    fun getFixedScreen(backgroundBitmap: Bitmap): Bitmap // 고정 화면을 보여줌
    fun getFixedScreen(imageProxy: ImageProxy): Bitmap
    suspend fun getLatestImage(): Uri?
    suspend fun downloadAiModel()
    fun getDownloadInfoFlow(): StateFlow<DownloadInfo>
    suspend fun checkForDownloadModel(downloadInfo: DownloadInfo): DownloadInfo
    suspend fun preRunModel(): Boolean
    fun getPoseFromImage(uri: Uri?): Bitmap?
    suspend fun loadAllCapturedImages(): List<ImageResult>
    suspend fun deleteCapturedImage(uri: Uri): Boolean

}