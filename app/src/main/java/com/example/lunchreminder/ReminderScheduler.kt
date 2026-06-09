package com.example.lunchreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleNextReminder(config: ReminderConfig) {
        if (!config.enabled) {
            cancelReminder()
            return
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calculateNextReminder(config)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            reminderPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }

    fun cancelReminder() {
        alarmManager.cancel(reminderPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT))
    }

    fun calculateNextReminder(config: ReminderConfig): LocalDateTime {
        return DateUtils.calculateNextReminder(config)
    }

    private fun reminderPendingIntent(extraFlags: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            extraFlags or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQUEST_CODE = 1200
    }
}
