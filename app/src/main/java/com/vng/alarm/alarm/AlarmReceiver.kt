package com.vng.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vng.alarm.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(Constants.EXTRA_ALARM_ID, 0)
        val hour = intent.getIntExtra(Constants.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(Constants.EXTRA_ALARM_MINUTE, 0)
        val label = intent.getStringExtra(Constants.EXTRA_ALARM_LABEL) ?: "Alarm"
        val ringtoneUri = intent.getStringExtra(Constants.EXTRA_RINGTONE_URI)
        val snoozeDuration = intent.getIntExtra(Constants.EXTRA_SNOOZE_DURATION, 5)
        val vibrate = intent.getBooleanExtra(Constants.EXTRA_VIBRATE, true)
        val isSnooze = intent.getBooleanExtra(Constants.EXTRA_IS_SNOOZE, false)

        if (isSnooze) {
            // Handle snooze
            startAlarmService(context, alarmId, hour, minute, label, ringtoneUri, snoozeDuration, vibrate)
        } else {
            // Start foreground service to ring alarm
            startAlarmService(context, alarmId, hour, minute, label, ringtoneUri, snoozeDuration, vibrate)

            // For recurring alarms, schedule next occurrence
            // This would need to fetch the alarm from database to check repeatDays
        }
    }

    private fun startAlarmService(
        context: Context,
        alarmId: Int,
        hour: Int,
        minute: Int,
        label: String,
        ringtoneUri: String?,
        snoozeDuration: Int,
        vibrate: Boolean
    ) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
            putExtra(Constants.EXTRA_ALARM_HOUR, hour)
            putExtra(Constants.EXTRA_ALARM_MINUTE, minute)
            putExtra(Constants.EXTRA_ALARM_LABEL, label)
            putExtra(Constants.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDuration)
            putExtra(Constants.EXTRA_VIBRATE, vibrate)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}