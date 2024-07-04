package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import com.hanadulset.pro_poseapp.data.datasource.UserDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings
import com.hanadulset.pro_poseapp.utils.eventlog.AnalyticsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(@ApplicationContext private val applicationContext: Context) :
    UserRepository {
    private val userDataSourceImpl by lazy {
        UserDataSourceImpl(applicationContext)
    }


    override suspend fun loadUserSet(): ProPoseAppSettings = userDataSourceImpl.loadUserSet()

    override suspend fun saveUserSet(proPoseAppSettings: ProPoseAppSettings) = userDataSourceImpl.saveUserSet(proPoseAppSettings)
    override suspend fun saveUserSuccessToTermOfUse() {
        userDataSourceImpl.saveUserSuccessToTermOfUse()
        AnalyticsManager(applicationContext.contentResolver).saveUserAgreeToUseEvent()
    }

    override suspend fun checkUserSuccessToTermOfUse() =
        userDataSourceImpl.checkUserSuccessToTermOfUse()
}