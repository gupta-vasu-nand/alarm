package com.vng.alarm.ui.screens.ringtone_picker

import android.app.Application
import android.content.ContentUris
import android.media.RingtoneManager
import android.provider.MediaStore
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vng.alarm.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RingtoneItem(
    val id: Long,
    val title: String,
    val uri: String,
    val isDefault: Boolean = false,
    val contentType: RingtoneType = RingtoneType.ALARM
)

enum class RingtoneType {
    ALARM, RINGTONE, NOTIFICATION, MUSIC
}

@HiltViewModel
class RingtonePickerViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val application: Application
) : ViewModel() {

    private val _ringtones = MutableStateFlow<List<RingtoneItem>>(emptyList())
    val ringtones: StateFlow<List<RingtoneItem>> = _ringtones

    private val _selectedRingtone = MutableStateFlow<String?>(null)
    val selectedRingtone: StateFlow<String?> = _selectedRingtone

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadRingtones()
        loadDefaultRingtone()
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            _isLoading.value = true
            val ringtoneList = mutableListOf<RingtoneItem>()

            withContext(Dispatchers.IO) {
                // 1. Add system default alarm
                ringtoneList.add(
                    RingtoneItem(
                        id = -1,
                        title = "Default alarm",
                        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString(),
                        isDefault = true,
                        contentType = RingtoneType.ALARM
                    )
                )

                // 2. Add system default ringtone
                ringtoneList.add(
                    RingtoneItem(
                        id = -2,
                        title = "Default ringtone",
                        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString(),
                        contentType = RingtoneType.RINGTONE
                    )
                )

                // 3. Add system default notification
                ringtoneList.add(
                    RingtoneItem(
                        id = -3,
                        title = "Default notification",
                        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString(),
                        contentType = RingtoneType.NOTIFICATION
                    )
                )

                // 4. Get all system ringtones from RingtoneManager
                val ringtoneManager = RingtoneManager(application)
                ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)
                val cursor = ringtoneManager.cursor

                try {
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                        val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" +
                                cursor.getLong(RingtoneManager.ID_COLUMN_INDEX)

                        ringtoneList.add(
                            RingtoneItem(
                                id = cursor.getLong(RingtoneManager.ID_COLUMN_INDEX),
                                title = title,
                                uri = uri,
                                contentType = RingtoneType.RINGTONE
                            )
                        )
                    }
                } finally {
                    cursor.close()
                }

                // 5. Get alarm sounds (specific for alarms)
                val alarmManager = RingtoneManager(application)
                alarmManager.setType(RingtoneManager.TYPE_ALARM)
                val alarmCursor = alarmManager.cursor

                try {
                    while (alarmCursor.moveToNext()) {
                        val title = alarmCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                        val uri = alarmCursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" +
                                alarmCursor.getLong(RingtoneManager.ID_COLUMN_INDEX)

                        // Avoid duplicates
                        if (!ringtoneList.any { it.uri == uri }) {
                            ringtoneList.add(
                                RingtoneItem(
                                    id = alarmCursor.getLong(RingtoneManager.ID_COLUMN_INDEX),
                                    title = title,
                                    uri = uri,
                                    contentType = RingtoneType.ALARM
                                )
                            )
                        }
                    }
                } finally {
                    alarmCursor.close()
                }

                // 6. Get notification sounds
                val notificationManager = RingtoneManager(application)
                notificationManager.setType(RingtoneManager.TYPE_NOTIFICATION)
                val notificationCursor = notificationManager.cursor

                try {
                    while (notificationCursor.moveToNext()) {
                        val title = notificationCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                        val uri = notificationCursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" +
                                notificationCursor.getLong(RingtoneManager.ID_COLUMN_INDEX)

                        if (!ringtoneList.any { it.uri == uri }) {
                            ringtoneList.add(
                                RingtoneItem(
                                    id = notificationCursor.getLong(RingtoneManager.ID_COLUMN_INDEX),
                                    title = title,
                                    uri = uri,
                                    contentType = RingtoneType.NOTIFICATION
                                )
                            )
                        }
                    }
                } finally {
                    notificationCursor.close()
                }

                // 7. Get all audio files from external storage (music files)
                try {
                    val resolver = application.contentResolver
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val projection = arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.IS_MUSIC,
                        MediaStore.Audio.Media.IS_RINGTONE,
                        MediaStore.Audio.Media.IS_ALARM,
                        MediaStore.Audio.Media.IS_NOTIFICATION
                    )

                    // Include music files and files marked as ringtones/alarms
                    val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 OR " +
                            "${MediaStore.Audio.Media.IS_RINGTONE} = 1 OR " +
                            "${MediaStore.Audio.Media.IS_ALARM} = 1 OR " +
                            "${MediaStore.Audio.Media.IS_NOTIFICATION} = 1"

                    val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                    resolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val isMusicColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                        val isRingtoneColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)
                        val isAlarmColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)
                        val isNotificationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn)
                            val musicUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            ).toString()

                            // Determine content type
                            val contentType = when {
                                cursor.getInt(isAlarmColumn) == 1 -> RingtoneType.ALARM
                                cursor.getInt(isRingtoneColumn) == 1 -> RingtoneType.RINGTONE
                                cursor.getInt(isNotificationColumn) == 1 -> RingtoneType.NOTIFICATION
                                else -> RingtoneType.MUSIC
                            }

                            // Avoid duplicates
                            if (!ringtoneList.any { it.uri == musicUri }) {
                                ringtoneList.add(
                                    RingtoneItem(
                                        id = id,
                                        title = title,
                                        uri = musicUri,
                                        contentType = contentType
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 8. Add Settings.System ringtones (additional system sounds)
                try {
                    val systemUri = Settings.System.DEFAULT_ALARM_ALERT_URI
                    if (systemUri != null) {
                        val title = "System Alarm"
                        if (!ringtoneList.any { it.uri == systemUri.toString() }) {
                            ringtoneList.add(
                                RingtoneItem(
                                    id = -4,
                                    title = title,
                                    uri = systemUri.toString(),
                                    contentType = RingtoneType.ALARM
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sort ringtones: Default first, then by title
            _ringtones.value = ringtoneList.sortedWith(
                compareByDescending<RingtoneItem> { it.isDefault }
                    .thenBy { it.title.lowercase() }
            )

            _isLoading.value = false
        }
    }

    private fun loadDefaultRingtone() {
        viewModelScope.launch {
            settingsDataStore.defaultRingtoneUri.collect { uri ->
                _selectedRingtone.value = uri
            }
        }
    }

    fun selectRingtone(uri: String?) {
        _selectedRingtone.value = uri
    }

    fun saveAsDefault(uri: String?) {
        viewModelScope.launch {
            settingsDataStore.saveDefaultRingtoneUri(uri ?: "")
        }
    }
}