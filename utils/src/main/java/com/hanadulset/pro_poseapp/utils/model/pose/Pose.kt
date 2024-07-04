package com.hanadulset.pro_poseapp.utils.model.pose

import android.net.Uri
import com.hanadulset.pro_poseapp.utils.model.common.ProPoseSizeF

//포즈 데이터 클래스
data class Pose(
    val id: Int = 0,
    val category: Int = 0,
    val bottomCenterAspect: ProPoseSizeF = ProPoseSizeF(0F, 0F), //중심점 비율
    val aspect: ProPoseSizeF = ProPoseSizeF(0F, 0F),
    val imageSrcUri: Uri? = null,
    val imageScale: Float = 1F,
)


