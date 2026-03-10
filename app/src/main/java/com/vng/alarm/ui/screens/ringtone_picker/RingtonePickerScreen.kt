package com.vng.alarm.ui.screens.ringtone_picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vng.alarm.util.RingtonePlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonePickerScreen(
    onRingtoneSelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RingtonePickerViewModel = hiltViewModel()
) {
    val ringtones by viewModel.ringtones.collectAsStateWithLifecycle()
    val selectedRingtone by viewModel.selectedRingtone.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showSaveAsDefault by remember { mutableStateOf(false) }

    val ringtonesByType = remember(ringtones) { ringtones.groupBy { it.contentType } }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Ringtones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {

                fun section(title: String, list: List<RingtoneItem>?) {
                    if (!list.isNullOrEmpty()) {
                        item { ModernSectionHeader(title) }
                        items(list, key = { it.id }) { ringtone ->
                            ModernRingtoneItem(
                                ringtone = ringtone,
                                isSelected = ringtone.uri == selectedRingtone,
                                onClick = {
                                    viewModel.selectRingtone(ringtone.uri)
                                    showSaveAsDefault = true
                                }
                            )
                        }
                    }
                }

                section("Alarm Sounds", ringtonesByType[RingtoneType.ALARM])
                section("Ringtones", ringtonesByType[RingtoneType.RINGTONE])
                section("Notifications", ringtonesByType[RingtoneType.NOTIFICATION])
                section("Music", ringtonesByType[RingtoneType.MUSIC])

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    if (showSaveAsDefault && selectedRingtone != null) {
        AlertDialog(
            onDismissRequest = {
                showSaveAsDefault = false
                onRingtoneSelected(selectedRingtone)
            },
            title = { Text("Set as default ringtone?") },
            text = { Text("Use this ringtone automatically for new alarms.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAsDefault(selectedRingtone)
                    showSaveAsDefault = false
                    onRingtoneSelected(selectedRingtone)
                }) { Text("Set Default") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveAsDefault = false
                    onRingtoneSelected(selectedRingtone)
                }) { Text("Only This Alarm") }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

@Composable
fun ModernSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun ModernRingtoneItem(
    ringtone: RingtoneItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val ringtonePlayer = remember { RingtonePlayer(context) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { ringtonePlayer.stopPlayback() }
    }

    ListItem(
        headlineContent = {
            Text(
                text = ringtone.title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = when (ringtone.contentType) {
                    RingtoneType.ALARM -> "Alarm sound"
                    RingtoneType.RINGTONE -> "Ringtone"
                    RingtoneType.NOTIFICATION -> "Notification"
                    RingtoneType.MUSIC -> "Music file"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            FilledIconButton(
                onClick = {
                    if (isPlaying) {
                        ringtonePlayer.stopPlayback()
                        isPlaying = false
                    } else {
                        ringtonePlayer.playRingtone(ringtone.uri)
                        isPlaying = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
        },
        trailingContent = {
            when {
                isSelected -> Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                ringtone.isDefault -> Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor =
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else
                    MaterialTheme.colorScheme.surface
        ),
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}