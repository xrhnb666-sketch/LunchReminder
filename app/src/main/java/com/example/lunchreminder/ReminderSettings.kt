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
    val breakfastEnabled: Boolean = false,
    val breakfastHour: Int = 8,
    val breakfastMinute: Int = 0,
    val lunchEnabled: Boolean = false,
    val lunchHour: Int = 12,
    val lunchMinute: Int = 0,
    val dinnerEnabled: Boolean = false,
    val dinnerHour: Int = 18,
    val dinnerMinute: Int = 0,
    val weekdaysOnly: Boolean = false,
    val skippedDateEpochDay: Long? = null,
    val customMessage: String = DEFAULT_CUSTOM_MESSAGE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val breakfastMessages: String = DEFAULT_BREAKFAST_MESSAGES,
    val lunchMessages: String = DEFAULT_LUNCH_MESSAGES,
    val dinnerMessages: String = DEFAULT_DINNER_MESSAGES,
) {
    companion object {
        const val DEFAULT_CUSTOM_MESSAGE = "记得按时吃饭"
        const val DEFAULT_BREAKFAST_MESSAGES = "🍳 早餐时间到了"
        const val DEFAULT_LUNCH_MESSAGES = "🍱 午饭时间到了"
        const val DEFAULT_DINNER_MESSAGES = "🍜 晚饭时间到了"
    }

    val enabled: Boolean
        get() = breakfastEnabled || lunchEnabled || dinnerEnabled

    fun isEnabled(mealType: MealType): Boolean {
        return when (mealType) {
            MealType.BREAKFAST -> breakfastEnabled
            MealType.LUNCH -> lunchEnabled
            MealType.DINNER -> dinnerEnabled
        }
    }

    fun hourFor(mealType: MealType): Int {
        return when (mealType) {
            MealType.BREAKFAST -> breakfastHour
            MealType.LUNCH -> lunchHour
            MealType.DINNER -> dinnerHour
        }
    }

    fun minuteFor(mealType: MealType): Int {
        return when (mealType) {
            MealType.BREAKFAST -> breakfastMinute
            MealType.LUNCH -> lunchMinute
            MealType.DINNER -> dinnerMinute
        }
    }

    fun withMealEnabled(mealType: MealType, enabled: Boolean): ReminderConfig {
        return when (mealType) {
            MealType.BREAKFAST -> copy(breakfastEnabled = enabled)
            MealType.LUNCH -> copy(lunchEnabled = enabled)
            MealType.DINNER -> copy(dinnerEnabled = enabled)
        }
    }

    fun withMealTime(mealType: MealType, hour: Int, minute: Int): ReminderConfig {
        return when (mealType) {
            MealType.BREAKFAST -> copy(breakfastHour = hour, breakfastMinute = minute)
            MealType.LUNCH -> copy(lunchHour = hour, lunchMinute = minute)
            MealType.DINNER -> copy(dinnerHour = hour, dinnerMinute = minute)
        }
    }
}

internal val Context.reminderDataStore by preferencesDataStore(name = "reminder_settings")

class ReminderSettings(private val context: Context) {
    private object Keys {
        val Enabled = booleanPreferencesKey("enabled")
        val ReminderHour = intPreferencesKey("hour")
        val ReminderMinute = intPreferencesKey("minute")
        val BreakfastEnabled = booleanPreferencesKey("breakfast_enabled")
        val BreakfastHour = intPreferencesKey("breakfast_hour")
        val BreakfastMinute = intPreferencesKey("breakfast_minute")
        val LunchEnabled = booleanPreferencesKey("lunch_enabled")
        val LunchHour = intPreferencesKey("lunch_hour")
        val LunchMinute = intPreferencesKey("lunch_minute")
        val DinnerEnabled = booleanPreferencesKey("dinner_enabled")
        val DinnerHour = intPreferencesKey("dinner_hour")
        val DinnerMinute = intPreferencesKey("dinner_minute")
        val WeekdaysOnly = booleanPreferencesKey("weekdays_only")
        val SkippedDateEpochDay = longPreferencesKey("skipped_date_epoch_day")
        val CustomMessage = stringPreferencesKey("custom_message")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val BreakfastMessages = stringPreferencesKey("breakfast_messages")
        val LunchMessages = stringPreferencesKey("lunch_messages")
        val DinnerMessages = stringPreferencesKey("dinner_messages")
    }

    val configFlow: Flow<ReminderConfig> = context.reminderDataStore.data.map { preferences ->
        val legacyLunchEnabled = preferences[Keys.Enabled] ?: false
        val legacyLunchHour = preferences[Keys.ReminderHour] ?: MealType.LUNCH.defaultHour
        val legacyLunchMinute = preferences[Keys.ReminderMinute] ?: MealType.LUNCH.defaultMinute

        ReminderConfig(
            breakfastEnabled = preferences[Keys.BreakfastEnabled] ?: false,
            breakfastHour = preferences[Keys.BreakfastHour] ?: MealType.BREAKFAST.defaultHour,
            breakfastMinute = preferences[Keys.BreakfastMinute] ?: MealType.BREAKFAST.defaultMinute,
            lunchEnabled = preferences[Keys.LunchEnabled] ?: legacyLunchEnabled,
            lunchHour = preferences[Keys.LunchHour] ?: legacyLunchHour,
            lunchMinute = preferences[Keys.LunchMinute] ?: legacyLunchMinute,
            dinnerEnabled = preferences[Keys.DinnerEnabled] ?: false,
            dinnerHour = preferences[Keys.DinnerHour] ?: MealType.DINNER.defaultHour,
            dinnerMinute = preferences[Keys.DinnerMinute] ?: MealType.DINNER.defaultMinute,
            weekdaysOnly = preferences[Keys.WeekdaysOnly] ?: false,
            skippedDateEpochDay = preferences[Keys.SkippedDateEpochDay],
            customMessage = preferences[Keys.CustomMessage] ?: ReminderConfig.DEFAULT_CUSTOM_MESSAGE,
            themeMode = ThemeMode.fromStorageKey(preferences[Keys.ThemeMode]),
            breakfastMessages = preferences[Keys.BreakfastMessages] ?: ReminderConfig.DEFAULT_BREAKFAST_MESSAGES,
            lunchMessages = preferences[Keys.LunchMessages] ?: ReminderConfig.DEFAULT_LUNCH_MESSAGES,
            dinnerMessages = preferences[Keys.DinnerMessages] ?: ReminderConfig.DEFAULT_DINNER_MESSAGES,
        )
    }

    suspend fun updateMealEnabled(mealType: MealType, enabled: Boolean) {
        context.reminderDataStore.edit { preferences ->
            when (mealType) {
                MealType.BREAKFAST -> preferences[Keys.BreakfastEnabled] = enabled
                MealType.LUNCH -> preferences[Keys.LunchEnabled] = enabled
                MealType.DINNER -> preferences[Keys.DinnerEnabled] = enabled
            }
        }
    }

    suspend fun updateMealTime(mealType: MealType, hour: Int, minute: Int) {
        context.reminderDataStore.edit { preferences ->
            when (mealType) {
                MealType.BREAKFAST -> {
                    preferences[Keys.BreakfastHour] = hour
                    preferences[Keys.BreakfastMinute] = minute
                }
                MealType.LUNCH -> {
                    preferences[Keys.LunchHour] = hour
                    preferences[Keys.LunchMinute] = minute
                }
                MealType.DINNER -> {
                    preferences[Keys.DinnerHour] = hour
                    preferences[Keys.DinnerMinute] = minute
                }
            }
        }
    }

    suspend fun updateEnabled(enabled: Boolean) {
        updateMealEnabled(MealType.LUNCH, enabled)
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        updateMealTime(MealType.LUNCH, hour, minute)
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

    suspend fun cancelSkipToday() {
        context.reminderDataStore.edit { preferences ->
            preferences.remove(Keys.SkippedDateEpochDay)
        }
    }

    suspend fun updateCustomMessage(message: String) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.CustomMessage] = message
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.reminderDataStore.edit { preferences ->
            preferences[Keys.ThemeMode] = themeMode.storageKey
        }
    }

    suspend fun updateMealMessages(mealType: MealType, messages: String) {
        context.reminderDataStore.edit { preferences ->
            when (mealType) {
                MealType.BREAKFAST -> preferences[Keys.BreakfastMessages] = messages
                MealType.LUNCH -> preferences[Keys.LunchMessages] = messages
                MealType.DINNER -> preferences[Keys.DinnerMessages] = messages
            }
        }
    }
}
