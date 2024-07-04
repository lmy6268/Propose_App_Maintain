package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSizeF


//이미지 저장 등과 같은 처리 담당
interface ImageProcessDataSource {
    fun getFixedImage(bitmap: Bitmap): Bitmap

    fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: org.opencv.core.Size): Bitmap
    suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: ProPoseSizeF): ProPoseSizeF?

    fun stopToUseOpticalFlow()

}