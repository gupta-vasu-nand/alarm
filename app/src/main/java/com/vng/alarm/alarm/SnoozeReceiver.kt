package com.vng.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vng.alarm.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(Constants.EXTRA_ALARM_ID, 0)
        val snoozeDuration = intent.getIntExtra(Constants.EXTRA_SNOOZE_DURATION, 5)

        // Stop current alarm
        val stopIntent = Intent(context, AlarmService::class.java)
        context.stopService(stopIntent)

        // Schedule snooze
        alarmScheduler.snoozeAlarm(alarmId, snoozeDuration)
    }
}

