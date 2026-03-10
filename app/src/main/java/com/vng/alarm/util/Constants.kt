package com.vng.alarm.util

object Constants {
    const val SETTINGS_PREFERENCES = "settings_preferences"

    // Intent extras
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
    const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
    const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"
    const val EXTRA_VIBRATE = "extra_vibrate"
    const val EXTRA_IS_SNOOZE = "extra_is_snooze"

    // Notification channels
    const val CHANNEL_ALARM = "alarm_channel"
    const val CHANNEL_SNOOZE = "snooze_channel"

    // Notification IDs
    const val NOTIFICATION_ID_ALARM = 1001
    const val NOTIFICATION_ID_SNOOZE = 1002

    // Request codes
    const val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1000
}