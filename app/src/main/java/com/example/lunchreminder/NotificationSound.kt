package com.example.lunchreminder

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes

enum class NotificationSound(
    val storageKey: String,
    val displayName: String,
    @RawRes val rawResId: Int?,
) {
    DEFAULT("default", "默认铃声", R.raw.default_sound),
    GENTLE("gentle", "温柔铃声", R.raw.gentle_sound),
    BEAR("bear", "小熊铃声", R.raw.bear_sound),
    MUSIC("music", "轻音乐", R.raw.music_sound),
    CUSTOM("custom", "自定义铃声", null);

    fun uri(context: Context): Uri {
        requireNotNull(rawResId) { "Custom sound does not have a bundled raw resource." }
        return Uri.parse(
            "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$rawResId",
        )
    }

    companion object {
        fun fromStorageKey(storageKey: String?): NotificationSound {
            return entries.firstOrNull { it.storageKey == storageKey } ?: DEFAULT
        }
    }
}
