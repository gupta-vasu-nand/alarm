package com.vng.alarm.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val defaultSnooze by viewModel.defaultSnooze.collectAsStateWithLifecycle()
    val defaultVibrate by viewModel.defaultVibrate.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val snoozeOptions = listOf(5, 10, 15, 20, 30)
    val themeOptions = listOf("light", "dark", "system")

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Defaults Section
            item {
                SettingsSectionTitle("Alarm Defaults")
            }

            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        // Snooze
                        ListItem(
                            headlineContent = { Text("Default snooze duration") },
                            supportingContent = { Text("$defaultSnooze minutes") }
                        )

                        FlowRowChips(
                            options = snoozeOptions.map { "$it min" },
                            selectedIndex = snoozeOptions.indexOf(defaultSnooze),
                            onSelected = { index ->
                                viewModel.updateDefaultSnooze(snoozeOptions[index])
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                        // Vibration
                        ListItem(
                            headlineContent = { Text("Default vibration") },
                            trailingContent = {
                                Switch(
                                    checked = defaultVibrate,
                                    onCheckedChange = viewModel::updateDefaultVibrate
                                )
                            }
                        )
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSectionTitle("Appearance")
            }

            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        ListItem(
                            headlineContent = { Text("Theme") },
                            supportingContent = {
                                Text(themeMode.replaceFirstChar { it.uppercase() })
                            }
                        )

                        FlowRowChips(
                            options = themeOptions.map {
                                it.replaceFirstChar { c -> c.uppercase() }
                            },
                            selectedIndex = themeOptions.indexOf(themeMode),
                            onSelected = { index ->
                                viewModel.updateThemeMode(themeOptions[index])
                            }
                        )
                    }
                }
            }

            // About Section
            item {
                SettingsSectionTitle("About")
            }

            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Alarms",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "A modern alarm scheduling and management application designed with a clean and intuitive experience.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowChips(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                label = { Text(label) }
            )
        }
    }
}