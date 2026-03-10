package com.vng.alarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vng.alarm.domain.model.Alarm

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: List<Int>,
    val isEnabled: Boolean,
    val ringtoneUri: String?,
    val snoozeDuration: Int,
    val vibrate: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Alarm = Alarm(
        id = id,
        hour = hour,
        minute = minute,
        label = label,
        repeatDays = repeatDays,
        isEnabled = isEnabled,
        ringtoneUri = ringtoneUri,
        snoozeDuration = snoozeDuration,
        vibrate = vibrate,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity = AlarmEntity(
            id = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            label = alarm.label,
            repeatDays = alarm.repeatDays,
            isEnabled = alarm.isEnabled,
            ringtoneUri = alarm.ringtoneUri,
            snoozeDuration = alarm.snoozeDuration,
            vibrate = alarm.vibrate,
            createdAt = alarm.createdAt
        )
    }
}