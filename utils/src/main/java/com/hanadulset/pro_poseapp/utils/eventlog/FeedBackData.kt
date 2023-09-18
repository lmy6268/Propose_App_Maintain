package com.hanadulset.pro_poseapp.utils.eventlog

import kotlinx.serialization.Serializable

@Serializable
data class FeedBackData(
    val deviceID: String,
    val eventLogs: ArrayList<EventLog>
)

