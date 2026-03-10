package com.vng.alarm.domain.model

import java.util.Calendar

data class Alarm(
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val repeatDays: List<Int> = emptyList(),
    val isEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val snoozeDuration: Int = 5,
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getTimeInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time has passed today, set for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    fun getFormattedTime(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hour12 = if (calendar.get(Calendar.HOUR) == 0) 12 else calendar.get(Calendar.HOUR)
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }

    fun getRepeatDaysText(): String {
        if (repeatDays.isEmpty()) return "Once"

        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return repeatDays.sorted().joinToString(", ") { days[it] }
    }

    fun isRecurring(): Boolean = repeatDays.isNotEmpty()
}