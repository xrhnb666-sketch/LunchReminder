package com.example.lunchreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class LunchAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(hour: Int, minute: Int) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(hour, minute),
            alarmPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT),
        )
    }

    fun cancel() {
        alarmManager.cancel(alarmPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT))
    }

    private fun alarmPendingIntent(extraFlags: Int): PendingIntent {
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

        fun nextTriggerMillis(hour: Int, minute: Int): Long {
            val now = Calendar.getInstance()
            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (!trigger.after(now)) {
                trigger.add(Calendar.DAY_OF_YEAR, 1)
            }

            return trigger.timeInMillis
        }
    }
}
