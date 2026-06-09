package com.example.lunchreminder

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ReminderConfig(
    val enabled: Boolean = false,
    val hour: Int = 12,
    val minute: Int = 0,
)

private val Context.reminderDataStore by preferencesDataStore(name = "reminder_settings")

class ReminderPreferences(private val context: Context) {
    private object Keys {
        val Enabled = booleanPreferencesKey("enabled")
        val Hour = intPreferencesKey("hour")
        val Minute = intPreferencesKey("minute")
    }

    val configFlow: Flow<ReminderConfig> = context.reminderDataStore.data.map { preferences ->
        ReminderConfig(
            enabled = preferences[Keys.Enabled] ?: false,
            hour = preferences[Keys.Hour] ?: 12,
            minute = preferences[Keys.Minute] ?: 0,
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.Enabled] = enabled
        }
    }

    suspend fun setTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.Hour] = hour
            preferences[Keys.Minute] = minute
        }
    }
}
