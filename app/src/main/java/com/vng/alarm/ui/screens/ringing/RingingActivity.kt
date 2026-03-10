package com.vng.alarm.ui.screens.ringing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vng.alarm.alarm.AlarmService
import com.vng.alarm.ui.theme.AlarmTheme
import com.vng.alarm.util.Constants

class RingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmId = intent.getIntExtra(Constants.EXTRA_ALARM_ID, 0)
        val hour = intent.getIntExtra(Constants.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(Constants.EXTRA_ALARM_MINUTE, 0)
        val label = intent.getStringExtra(Constants.EXTRA_ALARM_LABEL) ?: "Alarm"
        val snoozeDuration = intent.getIntExtra(Constants.EXTRA_SNOOZE_DURATION, 5)

        setContent {
            AlarmTheme {
                RingingScreen(
                    alarmId = alarmId,
                    hour = hour,
                    minute = minute,
                    label = label,
                    snoozeDuration = snoozeDuration,
                    onDismiss = {
                        stopAlarmService()
                        finish()
                    },
                    onSnooze = {
                        snoozeAlarm(alarmId, snoozeDuration)
                        stopAlarmService()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopAlarmService() {
        val intent = Intent(this, AlarmService::class.java)
        stopService(intent)
    }

    private fun snoozeAlarm(alarmId: Int, snoozeDuration: Int) {
        val intent = Intent(this, AlarmService::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
            putExtra(Constants.EXTRA_IS_SNOOZE, true)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDuration)
        }
        startService(intent)
    }
}

@Composable
fun RingingScreen(
    alarmId: Int,
    hour: Int,
    minute: Int,
    label: String,
    snoozeDuration: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val formattedTime = remember(hour, minute) {
        val amPm = if (hour < 12) "AM" else "PM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        String.format("%d:%02d %s", hour12, minute, amPm)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Time + Label Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "Alarm",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (label.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                FilledTonalButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Snooze • $snoozeDuration min",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}