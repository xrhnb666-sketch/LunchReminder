package com.example.lunchreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleNextReminder(config: ReminderConfig) {
        scheduleAll(config)
    }

    fun scheduleBreakfast(config: ReminderConfig) {
        scheduleMeal(config, MealType.BREAKFAST)
    }

    fun scheduleLunch(config: ReminderConfig) {
        scheduleMeal(config, MealType.LUNCH)
    }

    fun scheduleDinner(config: ReminderConfig) {
        scheduleMeal(config, MealType.DINNER)
    }

    fun scheduleAll(config: ReminderConfig) {
        scheduleBreakfast(config)
        scheduleLunch(config)
        scheduleDinner(config)
    }

    fun cancelAll() {
        MealType.entries.forEach { mealType -> cancelMeal(mealType) }
    }

    fun cancelReminder() {
        cancelAll()
    }

    fun calculateNextReminder(config: ReminderConfig): NextReminder? {
        return DateUtils.calculateNextReminder(config)
    }

    private fun scheduleMeal(config: ReminderConfig, mealType: MealType) {
        if (!config.isEnabled(mealType)) {
            cancelMeal(mealType)
            return
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            DateUtils.calculateNextReminder(config, mealType)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            reminderPendingIntent(mealType, PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }

    private fun cancelMeal(mealType: MealType) {
        alarmManager.cancel(reminderPendingIntent(mealType, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    private fun reminderPendingIntent(mealType: MealType, extraFlags: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEAL_TYPE, mealType.storageKey)
        }
        return PendingIntent.getBroadcast(
            context,
            mealType.requestCode,
            intent,
            extraFlags or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_MEAL_TYPE = "extra_meal_type"
    }
}
