package com.vng.alarm.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vng.alarm.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.SETTINGS_PREFERENCES)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val DEFAULT_SNOOZE_DURATION = intPreferencesKey("default_snooze_duration")
        val DEFAULT_RINGTONE_URI = stringPreferencesKey("default_ringtone_uri")
        val DEFAULT_VIBRATE = booleanPreferencesKey("default_vibrate")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    suspend fun saveDefaultSnoozeDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SNOOZE_DURATION] = duration
        }
    }

    val defaultSnoozeDuration: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_SNOOZE_DURATION] ?: 5
        }

    suspend fun saveDefaultRingtoneUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_RINGTONE_URI] = uri
        }
    }

    val defaultRingtoneUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_RINGTONE_URI]
        }

    suspend fun saveDefaultVibrate(vibrate: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_VIBRATE] = vibrate
        }
    }

    val defaultVibrate: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_VIBRATE] ?: true
        }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }
}