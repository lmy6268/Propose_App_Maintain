package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import com.hanadulset.pro_poseapp.data.datasource.DownloadResourcesDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.UserDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.UserSet
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.eventlog.FeedBackData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    UserRepository {
    private val userDataSourceImpl by lazy {
        UserDataSourceImpl(applicationContext)
    }
    private val fileHandleDataSourceImpl by lazy {
        FileHandleDataSourceImpl(context = applicationContext)
    }
    private val downloadSourceImpl by lazy {
        DownloadResourcesDataSourceImpl(applicationContext)
    }


    override suspend fun writeEventLog(eventLog: EventLog) {
        userDataSourceImpl.writeEventLog(eventLog)
    }

    //사용자 데이터를 서버에 보낸다.
    override suspend fun sendUserFeedback() {
        val userEventLogs = userDataSourceImpl.loadEventLogs()
        val uid = userDataSourceImpl.deviceID
        val userFeedBackData = FeedBackData(
            deviceID = uid,
            eventLogs = userEventLogs
        )
        fileHandleDataSourceImpl.sendFeedBackData(userFeedBackData)
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