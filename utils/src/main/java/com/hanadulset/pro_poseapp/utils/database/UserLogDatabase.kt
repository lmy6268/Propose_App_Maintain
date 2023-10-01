package com.hanadulset.pro_poseapp.utils.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserLog::class],version = 1)
abstract class UserLogDatabase : RoomDatabase() {
    abstract fun userLogDao(): UserLogDao
}