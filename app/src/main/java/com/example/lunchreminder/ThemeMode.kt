package com.example.lunchreminder

enum class ThemeMode(
    val storageKey: String,
    val label: String,
) {
    SYSTEM("system", "跟随系统"),
    LIGHT("light", "浅色模式"),
    DARK("dark", "深色模式");

    companion object {
        fun fromStorageKey(value: String?): ThemeMode {
            return entries.firstOrNull { mode -> mode.storageKey == value } ?: SYSTEM
        }
    }
}
