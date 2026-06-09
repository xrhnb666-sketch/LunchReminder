package com.example.lunchreminder

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ReminderConfig(
    val enabled: Boolean = false,
    val reminderHour: Int = 12,
    val reminderMinute: Int = 0,
    val weekdaysOnly: Boolean = false,
    val skippedDateEpochDay: Long? = null,
    val customMessage: String = DEFAULT_CUSTOM_MESSAGE,
) {
    companion object {
        const val DEFAULT_CUSTOM_MESSAGE = "到午饭时间啦，记得好好吃饭。"
    }
}

private val Context.reminderDataStore by preferencesDataStore(name = "reminder_settings")

class ReminderSettings(private val context: Context) {
    private object Keys {
        val Enabled = booleanPreferencesKey("enabled")
        val ReminderHour = intPreferencesKey("hour")
        val ReminderMinute = intPreferencesKey("minute")
        val WeekdaysOnly = booleanPreferencesKey("weekdays_only")
        val SkippedDateEpochDay = longPreferencesKey("skipped_date_epoch_day")
        val CustomMessage = stringPreferencesKey("custom_message")
    }

    val configFlow: Flow<ReminderConfig> = context.reminderDataStore.data.map { preferences ->
        ReminderConfig(
            enabled = preferences[Keys.Enabled] ?: false,
            reminderHour = preferences[Keys.ReminderHour] ?: 12,
            reminderMinute = preferences[Keys.ReminderMinute] ?: 0,
            weekdaysOnly = preferences[Keys.WeekdaysOnly] ?: false,
            skippedDateEpochDay = preferences[Keys.SkippedDateEpochDay],
            customMessage = preferences[Keys.CustomMessage] ?: ReminderConfig.DEFAULT_CUSTOM_MESSAGE,
        )
    }

    suspend fun updateEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.Enabled] = enabled
        }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.ReminderHour] = hour
            preferences[Keys.ReminderMinute] = minute
        }
    }

    suspend fun updateWeekdaysOnly(weekdaysOnly: Boolean) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.WeekdaysOnly] = weekdaysOnly
        }
    }

    suspend fun skipToday() {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.SkippedDateEpochDay] = DateUtils.todayEpochDay()
        }
    }

    suspend fun updateCustomMessage(message: String) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.CustomMessage] = message
        }
    }
}
