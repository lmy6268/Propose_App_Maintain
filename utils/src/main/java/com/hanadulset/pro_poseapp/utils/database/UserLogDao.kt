package com.hanadulset.pro_poseapp.utils.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserLogDao {
    @Transaction
    fun readyForSend(): List<UserLog> {
        return getAll().apply {
            deleteAll()
        }
    }

    @Query("SELECT * FROM UserLog")
    fun getAll(): List<UserLog> //저장되어있는 이벤트로그를 불러온다.

    @Insert
    fun insertAll(vararg userLog: UserLog)

    @Query("Delete From UserLog")
    fun deleteAll()

    @Delete
    fun delete(userLog: UserLog)
}