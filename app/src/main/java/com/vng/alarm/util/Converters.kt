package com.vng.alarm.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromRepeatDays(days: List<Int>): String {
        return Gson().toJson(days)
    }

    @TypeConverter
    fun toRepeatDays(daysString: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(daysString, type)
    }
}