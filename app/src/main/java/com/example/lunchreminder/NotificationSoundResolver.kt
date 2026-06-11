package com.example.lunchreminder

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

object NotificationSoundResolver {
    private const val UNAVAILABLE = "铃声不可用"

    fun effectiveUri(context: Context, config: ReminderConfig): Uri {
        val customUri = customUri(config)
        return if (
            config.notificationSound == NotificationSound.CUSTOM &&
            customUri != null &&
            canRead(context, customUri)
        ) {
            customUri
        } else {
            NotificationSound.DEFAULT.uri(context)
        }
    }

    fun displayName(context: Context, config: ReminderConfig): String {
        val customUri = customUri(config)
        return when {
            config.notificationSound != NotificationSound.CUSTOM -> config.notificationSound.displayName
            customUri == null -> UNAVAILABLE
            !canRead(context, customUri) -> UNAVAILABLE
            else -> queryDisplayName(context, customUri) ?: "自定义铃声"
        }
    }

    fun isCustomSoundUsable(context: Context, customSoundUri: String?): Boolean {
        val uri = customSoundUri?.let(Uri::parse) ?: return false
        return canRead(context, uri)
    }

    private fun customUri(config: ReminderConfig): Uri? {
        return config.customSoundUri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
    }

    private fun canRead(context: Context, uri: Uri): Boolean {
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        }.getOrDefault(false)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) it.getString(index) else null
                } else {
                    null
                }
            }
        }.getOrNull()
    }
}
