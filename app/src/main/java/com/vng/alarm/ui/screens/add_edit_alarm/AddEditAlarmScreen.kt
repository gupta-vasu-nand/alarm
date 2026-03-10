package com.vng.alarm.ui.screens.add_edit_alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vng.alarm.ui.components.DaySelector
import com.vng.alarm.ui.components.LabelInput
import com.vng.alarm.ui.components.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    alarmId: Int?,
    onNavigateBack: () -> Unit,
    onNavigateToRingtonePicker: () -> Unit,
    viewModel: AddEditAlarmViewModel = hiltViewModel()
) {
    val alarm by viewModel.alarm.collectAsStateWithLifecycle()
    val selectedRingtoneUri by viewModel.selectedRingtoneUri.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf(false) }
    var snoozeExpanded by remember { mutableStateOf(false) }

    val snoozeOptions = listOf(5, 10, 15, 20, 30)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (alarmId == null) "New Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveAlarm { onNavigateBack() } }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Time Hero Card
            item {
                ElevatedCard(
                    onClick = { showTimePicker = true },
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alarm.getFormattedTime(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    }
                }
            }

            // Label
            item {
                LabelInput(
                    label = alarm.label,
                    onLabelChange = viewModel::updateLabel
                )
            }

            // Repeat Days
            item {
                DaySelector(
                    selectedDays = alarm.repeatDays,
                    onDaysSelected = viewModel::updateRepeatDays
                )
            }

            // Settings Card
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        // Ringtone
                        ListItem(
                            headlineContent = { Text("Ringtone") },
                            supportingContent = {
                                Text(
                                    if (selectedRingtoneUri != null)
                                        "Custom ringtone"
                                    else
                                        "Default alarm"
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Default.Audiotrack, contentDescription = null)
                            },
                            modifier = Modifier.clickable { onNavigateToRingtonePicker() }
                        )

                        HorizontalDivider()

                        // Snooze
                        ExposedDropdownMenuBox(
                            expanded = snoozeExpanded,
                            onExpandedChange = { snoozeExpanded = it }
                        ) {
                            ListItem(
                                headlineContent = { Text("Snooze duration") },
                                supportingContent = {
                                    Text("${alarm.snoozeDuration} minutes")
                                },
                                leadingContent = {
                                    Icon(Icons.Default.Snooze, contentDescription = null)
                                },
                                trailingContent = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = snoozeExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .clickable { snoozeExpanded = true }
                            )

                            DropdownMenu(
                                expanded = snoozeExpanded,
                                onDismissRequest = { snoozeExpanded = false }
                            ) {
                                snoozeOptions.forEach { duration ->
                                    DropdownMenuItem(
                                        text = { Text("$duration minutes") },
                                        onClick = {
                                            viewModel.updateSnoozeDuration(duration)
                                            snoozeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // Vibrate
                        ListItem(
                            headlineContent = { Text("Vibrate") },
                            leadingContent = {
                                Icon(Icons.Default.Vibration, contentDescription = null)
                            },
                            trailingContent = {
                                Switch(
                                    checked = alarm.vibrate,
                                    onCheckedChange = viewModel::updateVibrate
                                )
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = alarm.hour,
            initialMinute = alarm.minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                viewModel.updateHour(h)
                viewModel.updateMinute(m)
                showTimePicker = false
            }
        )
    }
}