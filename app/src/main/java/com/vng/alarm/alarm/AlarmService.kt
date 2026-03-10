package com.vng.alarm.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.vng.alarm.R
import com.vng.alarm.ui.screens.ringing.RingingActivity
import com.vng.alarm.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private var alarmId: Int = 0
    private var snoozeDuration: Int = 5
    private var vibrate: Boolean = true

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            alarmId = it.getIntExtra(Constants.EXTRA_ALARM_ID, 0)
            val hour = it.getIntExtra(Constants.EXTRA_ALARM_HOUR, 0)
            val minute = it.getIntExtra(Constants.EXTRA_ALARM_MINUTE, 0)
            val label = it.getStringExtra(Constants.EXTRA_ALARM_LABEL) ?: "Alarm"
            val ringtoneUri = it.getStringExtra(Constants.EXTRA_RINGTONE_URI)
            snoozeDuration = it.getIntExtra(Constants.EXTRA_SNOOZE_DURATION, 5)
            vibrate = it.getBooleanExtra(Constants.EXTRA_VIBRATE, true)

            startForeground(alarmId, createNotification(label))

            // Start ringing
            startRinging(ringtoneUri)

            // Launch full screen activity
            launchRingingActivity(hour, minute, label)
        }

        return START_NOT_STICKY
    }

    private fun createNotification(label: String): Notification {
        val channelId = "alarm_channel"
        val notificationId = alarmId

        val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDuration)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, DismissReceiver::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm")
            .setContentText(label)
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, RingingActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ),
                true
            )
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_close, "Dismiss", dismissPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startRinging(ringtoneUri: String?) {
        try {
            val uri = if (!ringtoneUri.isNullOrEmpty()) {
                Uri.parse(ringtoneUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
                this@AlarmService.isPlaying = true
            }

            if (vibrate) {
                startVibration()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 1000, 1000),
                intArrayOf(0, 255, 0),
                -1
            )
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
    }

    private fun launchRingingActivity(hour: Int, minute: Int, label: String) {
        val intent = Intent(this, RingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
            putExtra(Constants.EXTRA_ALARM_HOUR, hour)
            putExtra(Constants.EXTRA_ALARM_MINUTE, minute)
            putExtra(Constants.EXTRA_ALARM_LABEL, label)
            putExtra(Constants.EXTRA_SNOOZE_DURATION, snoozeDuration)
        }
        startActivity(intent)
    }

    fun stopRinging() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
            }
        }
        mediaPlayer = null
        vibrator?.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }
}