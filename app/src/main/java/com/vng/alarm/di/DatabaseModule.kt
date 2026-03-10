package com.vng.alarm.di

import android.content.Context
import androidx.room.Room
import com.vng.alarm.data.local.database.AlarmDatabase
import com.vng.alarm.data.local.dao.AlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context
    ): AlarmDatabase {
        return Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            "alarm_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(
        database: AlarmDatabase
    ): AlarmDao {
        return database.alarmDao()
    }
}