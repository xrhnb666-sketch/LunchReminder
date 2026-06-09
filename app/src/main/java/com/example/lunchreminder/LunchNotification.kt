package com.example.lunchreminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object LunchNotification {
    private const val CHANNEL_ID = "lunch_reminders"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "三餐提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "每天到点提醒你吃饭"
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun show(context: Context) {
        show(context, MealType.LUNCH, ReminderConfig(lunchEnabled = true))
    }

    fun show(context: Context, mealType: MealType, config: ReminderConfig) {
        if (!canPostNotifications(context)) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = NotificationNavigation.FLAGS
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            NotificationNavigation.REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val content = NotificationMessageProvider.contentFor(config, mealType)
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lunch)
            .setContentTitle(content.title)
            .setContentText(content.message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(mealType.requestCode, notification)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
