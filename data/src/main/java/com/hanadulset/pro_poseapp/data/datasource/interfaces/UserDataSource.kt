package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings

interface UserDataSource {
    suspend fun saveUserSet(proPoseAppSettings: ProPoseAppSettings)
    suspend fun loadUserSet(): ProPoseAppSettings

    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}