package com.vng.alarm.data.repository

import com.vng.alarm.alarm.AlarmScheduler
import com.vng.alarm.data.local.dao.AlarmDao
import com.vng.alarm.data.local.entity.AlarmEntity
import com.vng.alarm.domain.model.Alarm
import com.vng.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> =
        alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getAlarmById(id: Int): Alarm? =
        alarmDao.getAlarmById(id)?.toDomain()

    override suspend fun insertAlarm(alarm: Alarm): Long {
        val id = alarmDao.insertAlarm(AlarmEntity.fromDomain(alarm))

        // Schedule the alarm if enabled
        if (alarm.isEnabled) {
            alarmScheduler.scheduleAlarm(alarm.copy(id = id.toInt()))
        }

        return id
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmEntity.fromDomain(alarm))

        // Reschedule the alarm
        alarmScheduler.cancelAlarm(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.scheduleAlarm(alarm)
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(alarm))
        alarmScheduler.cancelAlarm(alarm)
    }

    override suspend fun toggleAlarm(id: Int, enabled: Boolean) {
        val alarm = alarmDao.getAlarmById(id)?.toDomain()
        alarm?.let {
            val updatedAlarm = it.copy(isEnabled = enabled)
            updateAlarm(updatedAlarm)
        }
    }

    override suspend fun rescheduleAllAlarms() {
        val enabledAlarms = alarmDao.getEnabledAlarms()
        enabledAlarms.forEach { entity ->
            alarmScheduler.scheduleAlarm(entity.toDomain())
        }
    }
}