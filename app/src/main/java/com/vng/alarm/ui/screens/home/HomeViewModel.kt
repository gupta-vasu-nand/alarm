package com.vng.alarm.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vng.alarm.domain.model.Alarm
import com.vng.alarm.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = getAlarmsUseCase()
        .map { list -> list.sortedBy { it.hour * 60 + it.minute } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var deletedAlarm by mutableStateOf<Alarm?>(null)
        private set

    fun toggleAlarm(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(id, enabled)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)
            deletedAlarm = alarm
        }
    }

    fun restoreDeletedAlarm() {
        deletedAlarm?.let { alarm ->
            viewModelScope.launch {
                // Re-insert the alarm
                // This would require an insert use case
            }
            deletedAlarm = null
        }
    }

    fun clearDeletedAlarm() {
        deletedAlarm = null
    }
}