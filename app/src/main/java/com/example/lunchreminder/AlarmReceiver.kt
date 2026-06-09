package com.example.lunchreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val mealType = MealType.fromStorageKey(
            intent.getStringExtra(ReminderScheduler.EXTRA_MEAL_TYPE),
        ) ?: MealType.LUNCH
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = ReminderSettings(appContext).configFlow.first()
                if (config.isEnabled(mealType)) {
                    if (DateUtils.shouldNotifyNow(config)) {
                        LunchNotification.ensureChannel(appContext)
                        LunchNotification.show(appContext, mealType, config)
                    }
                }
                ReminderScheduler(appContext).scheduleAll(config)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
