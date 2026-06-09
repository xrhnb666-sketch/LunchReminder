package com.example.lunchreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = ReminderSettings(appContext).configFlow.first()
                if (config.enabled) {
                    if (shouldNotifyNow(config)) {
                        LunchNotification.ensureChannel(appContext)
                        LunchNotification.show(appContext)
                    }
                    ReminderScheduler(appContext).scheduleNextReminder(config)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun shouldNotifyNow(config: ReminderConfig): Boolean {
        val today = LocalDate.now()
        return !DateUtils.isSkippedToday(config, today) &&
            DateUtils.shouldRemindOnDate(config, today)
    }
}
