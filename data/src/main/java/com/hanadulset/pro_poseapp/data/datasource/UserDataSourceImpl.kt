package com.hanadulset.pro_poseapp.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.gson.Gson
import com.hanadulset.pro_poseapp.data.datasource.interfaces.UserDataSource
import com.hanadulset.pro_poseapp.utils.database.UserLog
import com.hanadulset.pro_poseapp.utils.database.UserLogDatabase
import com.hanadulset.pro_poseapp.utils.eventlog.EventLog


//기기 내의 사용자 설정 및 사용자의 로그를 정리하고 기록하는 데이터 소스
@SuppressLint("HardwareIds")
class UserDataSourceImpl constructor(private val applicationContext: Context) : UserDataSource {
    private val db by lazy {
        Room.databaseBuilder(applicationContext, UserLogDatabase::class.java, "UserLog").build()
    }
    private val userLogDao by lazy {
        db.userLogDao()
    }

    val deviceID: String by lazy {
        Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private var uuid: String? = null //UID
    //이벤트 로그 내보내기


    //사용자 설정 기록하기

    //사용자 설정 불러오기

    //이벤트 로그 남기기
    override suspend fun writeEventLog(eventLog: EventLog) {
        userLogDao.insertAll(eventLog.asUserLog())
        Log.d("EventLogs in db: ", userLogDao.getAll().toString())
    }

    override fun loadEventLogs(): ArrayList<EventLog> =
        arrayListOf(*userLogDao.readyForSend().map {
            it.asEventLog()
        }.toTypedArray())




    //Mapper Method
    private fun EventLog.asUserLog(): UserLog = UserLog(
        timeStamp = this.timestamp ?: System.currentTimeMillis().toString(),
        eventId = eventId,
        poseID = poseID,
        prevRecommendPoses = Gson().toJson(prevRecommendPoses),
        backgroundId = backgroundId,
        backgroundHog = backgroundHog
    )

    private fun UserLog.asEventLog(): EventLog = EventLog(
        eventId = eventId,
        timestamp = timeStamp,
        backgroundId = backgroundId,
        prevRecommendPoses = Gson().fromJson(prevRecommendPoses, Array<Int>::class.java)?.toList(),
        backgroundHog = backgroundHog,
        poseID = poseID
    )

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userFeedBack")
    }
}