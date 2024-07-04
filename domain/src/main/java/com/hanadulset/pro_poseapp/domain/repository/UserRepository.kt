package com.hanadulset.pro_poseapp.domain.repository

import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings

//사용자 흔적을 기록하는 역할을 담당하는 레포지토리
interface UserRepository {
    suspend fun loadUserSet(): ProPoseAppSettings
    suspend fun saveUserSet(proPoseAppSettings: ProPoseAppSettings)
    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}