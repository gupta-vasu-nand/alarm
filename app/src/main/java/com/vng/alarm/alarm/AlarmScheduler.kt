package com.vng.alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vng.alarm.domain.model.Alarm
import com.vng.alarm.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarm.id)
            putExtra(Constants.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(Constants.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(Constants.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(Constants.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, alarm.snoozeDuration)
            putExtra(Constants.EXTRA_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarm.isRecurring()) {
            scheduleRecurringAlarm(alarm, pendingIntent)
        } else {
            scheduleOneTimeAlarm(alarm, pendingIntent)
        }
    }

    private fun scheduleOneTimeAlarm(alarm: Alarm, pendingIntent: PendingIntent) {
        val triggerTime = alarm.getTimeInMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun scheduleRecurringAlarm(alarm: Alarm, pendingIntent: PendingIntent) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Find next valid day
        var daysToAdd = 0
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

        while (daysToAdd < 7) {
            val checkDay = (currentDay + daysToAdd) % 7
            if (alarm.repeatDays.contains(checkDay)) {
                if (daysToAdd == 0 && calendar.timeInMillis > System.currentTimeMillis()) {
                    // Today and time hasn't passed
                    break
                } else {
                    // Future day or time has passed today
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    break
                }
            }
            daysToAdd++
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun snoozeAlarm(alarmId: Int, snoozeDuration: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
            putExtra(Constants.EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 1000, // Different ID for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + (snoozeDuration * 60 * 1000)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }
}