package com.vng.alarm.alarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vng.alarm.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms after boot
            CoroutineScope(Dispatchers.IO).launch {
                alarmRepository.rescheduleAllAlarms()
            }
        }
    }
}
