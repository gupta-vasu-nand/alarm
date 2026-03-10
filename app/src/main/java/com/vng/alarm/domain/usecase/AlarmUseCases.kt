package com.vng.alarm.domain.usecase

import com.vng.alarm.domain.model.Alarm
import com.vng.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> = repository.getAllAlarms()
}

@Singleton
class GetAlarmByIdUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(id: Int): Alarm? = repository.getAlarmById(id)
}

@Singleton
class AddAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Long = repository.insertAlarm(alarm)
}

@Singleton
class UpdateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) = repository.updateAlarm(alarm)
}

@Singleton
class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) = repository.deleteAlarm(alarm)
}

@Singleton
class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(id: Int, enabled: Boolean) =
        repository.toggleAlarm(id, enabled)
}