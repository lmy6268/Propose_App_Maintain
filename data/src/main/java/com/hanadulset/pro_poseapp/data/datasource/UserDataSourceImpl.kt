package com.hanadulset.pro_poseapp.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hanadulset.pro_poseapp.data.datasource.interfaces.UserDataSource
import com.hanadulset.pro_poseapp.utils.model.user.ProPoseAppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


//기기 내의 사용자 설정 및 사용자의 로그를 정리하고 기록하는 데이터 소스
@SuppressLint("HardwareIds")
class UserDataSourceImpl(private val applicationContext: Context) : UserDataSource {

    override suspend fun saveUserSet(proPoseAppSettings: ProPoseAppSettings) {
        applicationContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(ProPoseAppSettings::class.simpleName ?: "UserSet")] =
                Json.encodeToString(proPoseAppSettings)
        }

    }

    override suspend fun loadUserSet(): ProPoseAppSettings {
        val value = applicationContext.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(ProPoseAppSettings::class.simpleName ?: "UserSet")]
        }.first()
        return if (value != null)
            Json.decodeFromString(value)
        else {
            val proPoseAppSettings = ProPoseAppSettings()
            saveUserSet(proPoseAppSettings)
            proPoseAppSettings
        }
    }


    override suspend fun saveUserSuccessToTermOfUse() {
        applicationContext.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("userSuccessToUse")] = "True"
        }
    }

    override suspend fun checkUserSuccessToTermOfUse(): Boolean =
        applicationContext.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("userSuccessToUse")] == "True"
        }.first()


    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userSet")
    }
}