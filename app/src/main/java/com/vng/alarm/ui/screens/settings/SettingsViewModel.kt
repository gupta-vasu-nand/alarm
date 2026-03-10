package com.vng.alarm.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vng.alarm.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _defaultSnooze = MutableStateFlow(5)
    val defaultSnooze: StateFlow<Int> = _defaultSnooze

    private val _defaultVibrate = MutableStateFlow(true)
    val defaultVibrate: StateFlow<Boolean> = _defaultVibrate

    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode

    init {
        viewModelScope.launch {
            settingsDataStore.defaultSnoozeDuration.collectLatest {
                _defaultSnooze.value = it
            }
        }

        viewModelScope.launch {
            settingsDataStore.defaultVibrate.collectLatest {
                _defaultVibrate.value = it
            }
        }

        viewModelScope.launch {
            settingsDataStore.themeMode.collectLatest {
                _themeMode.value = it
            }
        }
    }

    fun updateDefaultSnooze(duration: Int) {
        viewModelScope.launch {
            settingsDataStore.saveDefaultSnoozeDuration(duration)
        }
    }

    fun updateDefaultVibrate(vibrate: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDefaultVibrate(vibrate)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }
}