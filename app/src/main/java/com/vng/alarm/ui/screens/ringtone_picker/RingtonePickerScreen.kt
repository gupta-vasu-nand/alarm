package com.vng.alarm.ui.screens.ringtone_picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonePickerScreen(
    onRingtoneSelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RingtonePickerViewModel = hiltViewModel()
) {
    val ringtones by viewModel.ringtones.collectAsStateWithLifecycle()
    val selectedRingtone by viewModel.selectedRingtone.collectAsStateWithLifecycle()
    var showSaveAsDefault by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Ringtones") },
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
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(ringtones, key = { it.id }) { ringtone ->
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

    if (showSaveAsDefault && selectedRingtone != null) {
        AlertDialog(
            onDismissRequest = {
                showSaveAsDefault = false
                onRingtoneSelected(selectedRingtone)
            },
            title = { Text("Set as default ringtone?") },
            text = {
                Text("Use this ringtone automatically for new alarms.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveAsDefault(selectedRingtone)
                        showSaveAsDefault = false
                        onRingtoneSelected(selectedRingtone)
                    }
                ) { Text("Set Default") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveAsDefault = false
                        onRingtoneSelected(selectedRingtone)
                    }
                ) { Text("Only This Alarm") }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

@Composable
fun ModernRingtoneItem(
    ringtone: RingtoneItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = ringtone.title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            if (ringtone.isDefault) {
                Text(
                    text = "System default",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = if (ringtone.isDefault)
                    Icons.Default.Star
                else
                    Icons.Default.Audiotrack,
                contentDescription = null,
                tint = if (ringtone.isDefault)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        tonalElevation = if (isSelected) 4.dp else 0.dp
    )
}