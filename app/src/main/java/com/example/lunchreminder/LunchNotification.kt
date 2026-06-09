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
    private const val NOTIFICATION_ID = 1201

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "午饭提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "每天到点提醒你吃午饭"
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun show(context: Context) {
        if (!canPostNotifications(context)) return

        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lunch)
            .setContentTitle("午饭提醒")
            .setContentText("到午饭时间啦，记得好好吃饭。")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
