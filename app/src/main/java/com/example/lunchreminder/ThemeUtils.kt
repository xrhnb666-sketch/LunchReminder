package com.example.lunchreminder

import android.os.Build

object ThemeUtils {
    fun shouldUseDarkTheme(themeMode: ThemeMode, systemInDarkTheme: Boolean): Boolean {
        return when (themeMode) {
            ThemeMode.SYSTEM -> systemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }

    fun supportsDynamicColor(sdkInt: Int = Build.VERSION.SDK_INT): Boolean {
        return sdkInt >= Build.VERSION_CODES.S
    }
}
