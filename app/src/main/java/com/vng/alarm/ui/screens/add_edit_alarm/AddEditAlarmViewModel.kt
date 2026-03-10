package com.vng.alarm.ui.screens.add_edit_alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vng.alarm.data.datastore.SettingsDataStore
import com.vng.alarm.domain.model.Alarm
import com.vng.alarm.domain.usecase.AddAlarmUseCase
import com.vng.alarm.domain.usecase.GetAlarmByIdUseCase
import com.vng.alarm.domain.usecase.UpdateAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val addAlarmUseCase: AddAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val alarmId = savedStateHandle.get<Int>("alarmId") ?: -1
    private val isEditMode = alarmId != -1

    private val _alarm = MutableStateFlow(
        Alarm(
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            minute = Calendar.getInstance().get(Calendar.MINUTE) + 1,
            label = ""
        )
    )
    val alarm: StateFlow<Alarm> = _alarm

    private val _selectedRingtoneUri = MutableStateFlow<String?>(null)
    val selectedRingtoneUri: StateFlow<String?> = _selectedRingtoneUri

    init {
        if (isEditMode) {
            loadAlarm()
        } else {
            loadDefaultSettings()
        }
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            val existingAlarm = getAlarmByIdUseCase(alarmId)
            existingAlarm?.let {
                _alarm.value = it
                _selectedRingtoneUri.value = it.ringtoneUri
            }
        }
    }

    private fun loadDefaultSettings() {
        viewModelScope.launch {
            settingsDataStore.defaultSnoozeDuration.collectLatest { duration ->
                _alarm.value = _alarm.value.copy(snoozeDuration = duration)
            }
        }

        viewModelScope.launch {
            settingsDataStore.defaultRingtoneUri.collectLatest { uri ->
                _selectedRingtoneUri.value = uri
                _alarm.value = _alarm.value.copy(ringtoneUri = uri)
            }
        }

        viewModelScope.launch {
            settingsDataStore.defaultVibrate.collectLatest { vibrate ->
                _alarm.value = _alarm.value.copy(vibrate = vibrate)
            }
        }
    }

    fun updateHour(hour: Int) {
        _alarm.value = _alarm.value.copy(hour = hour)
    }

    fun updateMinute(minute: Int) {
        _alarm.value = _alarm.value.copy(minute = minute)
    }

    fun updateLabel(label: String) {
        _alarm.value = _alarm.value.copy(label = label)
    }

    fun updateRepeatDays(days: List<Int>) {
        _alarm.value = _alarm.value.copy(repeatDays = days)
    }

    fun updateSnoozeDuration(duration: Int) {
        _alarm.value = _alarm.value.copy(snoozeDuration = duration)
    }

    fun updateVibrate(vibrate: Boolean) {
        _alarm.value = _alarm.value.copy(vibrate = vibrate)
    }

    fun setRingtoneUri(uri: String?) {
        _selectedRingtoneUri.value = uri
        _alarm.value = _alarm.value.copy(ringtoneUri = uri)
    }

    fun saveAlarm(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (isEditMode) {
                updateAlarmUseCase(_alarm.value)
                alarmId.toLong()
            } else {
                addAlarmUseCase(_alarm.value)
            }
            onComplete(id)
        }
    }
}