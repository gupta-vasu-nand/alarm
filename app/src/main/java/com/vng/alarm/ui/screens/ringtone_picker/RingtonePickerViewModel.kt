package com.vng.alarm.ui.screens.ringtone_picker

import android.app.Application
import android.content.ContentUris
import android.media.RingtoneManager
import android.provider.MediaStore
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
    val isDefault: Boolean = false
)

@HiltViewModel
class RingtonePickerViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val application: Application
) : ViewModel(){

    private val _ringtones = MutableStateFlow<List<RingtoneItem>>(emptyList())
    val ringtones: StateFlow<List<RingtoneItem>> = _ringtones

    private val _selectedRingtone = MutableStateFlow<String?>(null)
    val selectedRingtone: StateFlow<String?> = _selectedRingtone

    init {
        loadRingtones()
        loadDefaultRingtone()
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            val ringtoneList = mutableListOf<RingtoneItem>()

            // Add default alarm
            ringtoneList.add(
                RingtoneItem(
                    id = -1,
                    title = "Default alarm",
                    uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString(),
                    isDefault = true
                )
            )

            withContext(Dispatchers.IO) {
                val resolver = application.contentResolver
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                resolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val musicUri = ContentUris.withAppendedId(uri, id).toString()

                        ringtoneList.add(
                            RingtoneItem(
                                id = id,
                                title = title,
                                uri = musicUri
                            )
                        )
                    }
                }
            }

            _ringtones.value = ringtoneList
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