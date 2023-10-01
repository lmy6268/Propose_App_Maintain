package com.hanadulset.pro_poseapp.data.repository

import android.content.Context
import com.hanadulset.pro_poseapp.data.datasource.FileHandleDataSourceImpl
import com.hanadulset.pro_poseapp.data.datasource.UserDataSourceImpl
import com.hanadulset.pro_poseapp.domain.repository.UserRepository
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog
import com.hanadulset.pro_poseapp.utils.eventlog.FeedBackData
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val applicationContext: Context) :
    UserRepository {
    private val userDataSourceImpl by lazy {
        UserDataSourceImpl(applicationContext)
    }
    private val fileHandleDataSourceImpl by lazy {
        FileHandleDataSourceImpl(context = applicationContext)
    }

    override  suspend fun writeEventLog(eventLog: EventLog) {
        userDataSourceImpl.writeEventLog(eventLog)
    }

    //사용자 데이터를 서버에 보낸다.
    override suspend fun sendUserFeedback() {
        val userEventLogs = userDataSourceImpl.loadEventLogs()
        val uid = userDataSourceImpl.getUserId()
        val userFeedBackData = FeedBackData(
            deviceID = uid,
            eventLogs = userEventLogs
        )
        fileHandleDataSourceImpl.sendFeedBackData(userFeedBackData)
    }
}