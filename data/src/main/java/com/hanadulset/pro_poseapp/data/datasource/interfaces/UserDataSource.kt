package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.utils.eventlog.EventLog

interface UserDataSource {
    suspend fun writeEventLog(eventLog: EventLog)
    fun loadEventLogs(): ArrayList<EventLog>

}