package com.example.lunchreminder

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiverSmokeTest {
    @Test
    fun alarmReceiverExplicitBroadcastDoesNotCrashWhenReminderIsOff() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_MEAL_TYPE, MealType.LUNCH.storageKey)
        }

        context.sendBroadcast(intent)
    }

    @Test
    fun bootReceiverIgnoresNonBootActionWithoutCrashing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent("com.example.lunchreminder.TEST_ACTION")

        BootReceiver().onReceive(context, intent)
    }
}
