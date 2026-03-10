package com.vng.alarm.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Uri?.getRingtoneName(context: Context): String {
    if (this == null) return "Default"
    return RingtoneManager.getRingtone(context, this)?.getTitle(context) ?: "Unknown"
}

fun Long.formatTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

@Composable
fun rememberFeedback() {
    val context = LocalContext.current
    // Haptic feedback implementation
}