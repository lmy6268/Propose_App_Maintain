package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import com.hanadulset.pro_poseapp.data.datasource.DownloadResourcesDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.UserDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.UserSet
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    UserRepository {
    private val userDataSourceImpl by lazy {
        UserDataSourceImpl(applicationContext)
    }
    private val downloadSourceImpl by lazy {
        DownloadResourcesDataSourceImpl(applicationContext)
    }



    override suspend fun userDeviceInternetConnection(): Flow<Boolean> =
        downloadSourceImpl.checkInternetConnection()

    override suspend fun loadUserSet(): UserSet = userDataSourceImpl.loadUserSet()

    override suspend fun saveUserSet(userSet: UserSet) = userDataSourceImpl.saveUserSet(userSet)
    override suspend fun saveUserSuccessToTermOfUse() {
        userDataSourceImpl.saveUserSuccessToTermOfUse()
    }

    override suspend fun checkUserSuccessToTermOfUse() =
        userDataSourceImpl.checkUserSuccessToTermOfUse()

}