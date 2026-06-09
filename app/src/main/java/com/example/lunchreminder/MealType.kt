package com.example.lunchreminder

enum class MealType(
    val storageKey: String,
    val displayName: String,
    val reminderLabel: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val requestCode: Int,
) {
    BREAKFAST(
        storageKey = "breakfast",
        displayName = "早餐提醒",
        reminderLabel = "早餐",
        defaultHour = 8,
        defaultMinute = 0,
        requestCode = 1201,
    ),
    LUNCH(
        storageKey = "lunch",
        displayName = "午餐提醒",
        reminderLabel = "午餐",
        defaultHour = 12,
        defaultMinute = 0,
        requestCode = 1202,
    ),
    DINNER(
        storageKey = "dinner",
        displayName = "晚餐提醒",
        reminderLabel = "晚餐",
        defaultHour = 18,
        defaultMinute = 0,
        requestCode = 1203,
    );

    companion object {
        fun fromStorageKey(value: String?): MealType? {
            return entries.firstOrNull { mealType -> mealType.storageKey == value }
        }
    }
}
