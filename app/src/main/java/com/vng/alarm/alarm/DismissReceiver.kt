package com.vng.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Stop alarm service
        val stopIntent = Intent(context, AlarmService::class.java)
        context.stopService(stopIntent)
    }
}