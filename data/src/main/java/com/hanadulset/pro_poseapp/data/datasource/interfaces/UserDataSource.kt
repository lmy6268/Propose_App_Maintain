package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.utils.UserSet
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog

interface UserDataSource {
    suspend fun writeEventLog(eventLog: EventLog)
    fun loadEventLogs(): ArrayList<EventLog>
    suspend fun saveUserSet(userSet: UserSet)
    suspend fun loadUserSet(): UserSet

    suspend fun saveUserSuccessToTermOfUse()
    suspend fun checkUserSuccessToTermOfUse(): Boolean

}