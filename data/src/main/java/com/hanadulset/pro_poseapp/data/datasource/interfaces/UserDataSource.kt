package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.utils.UserSet

interface UserDataSource {
    suspend fun saveUserSet(userSet: UserSet)
    suspend fun loadUserSet(): UserSet

    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}