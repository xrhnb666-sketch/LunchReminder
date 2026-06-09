package com.example.lunchreminder

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class NextReminder(
    val mealType: MealType,
    val dateTime: LocalDateTime,
)

object DateUtils {
    private val dateLabelFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)

    fun calculateNextReminder(
        config: ReminderConfig,
        now: LocalDateTime = LocalDateTime.now(),
    ): NextReminder? {
        return MealType.entries
            .filter { mealType -> config.isEnabled(mealType) }
            .map { mealType ->
                NextReminder(
                    mealType = mealType,
                    dateTime = calculateNextReminder(config, mealType, now),
                )
            }
            .minByOrNull { reminder -> reminder.dateTime }
    }

    fun calculateNextReminder(
        config: ReminderConfig,
        mealType: MealType,
        now: LocalDateTime = LocalDateTime.now(),
    ): LocalDateTime {
        var date = now.toLocalDate()

        while (true) {
            val candidate = date.atTime(config.hourFor(mealType), config.minuteFor(mealType))

            if (
                candidate.isAfter(now) &&
                !isSkippedDate(config, date) &&
                shouldRemindOnDate(config, date)
            ) {
                return candidate
            }

            date = date.plusDays(1)
        }
    }

    fun shouldRemindOnDate(config: ReminderConfig, date: LocalDate = LocalDate.now()): Boolean {
        return !config.weekdaysOnly || isWeekday(date)
    }

    fun shouldNotifyNow(config: ReminderConfig, today: LocalDate = LocalDate.now()): Boolean {
        return !isSkippedToday(config, today) && shouldRemindOnDate(config, today)
    }

    fun canSkipToday(config: ReminderConfig, now: LocalDateTime = LocalDateTime.now()): Boolean {
        return config.enabled && hasRemainingReminderToday(config, now)
    }

    fun hasRemainingReminderToday(
        config: ReminderConfig,
        now: LocalDateTime = LocalDateTime.now(),
    ): Boolean {
        val today = now.toLocalDate()
        if (!config.enabled || isSkippedDate(config, today) || !shouldRemindOnDate(config, today)) {
            return false
        }

        return MealType.entries.any { mealType ->
            config.isEnabled(mealType) &&
                today.atTime(config.hourFor(mealType), config.minuteFor(mealType)).isAfter(now)
        }
    }

    fun isSkippedToday(config: ReminderConfig, today: LocalDate = LocalDate.now()): Boolean {
        return isSkippedDate(config, today)
    }

    fun skipTodayButtonText(
        config: ReminderConfig,
        now: LocalDateTime = LocalDateTime.now(),
    ): String {
        return when {
            !config.enabled -> "提醒已关闭"
            isSkippedDate(config, now.toLocalDate()) -> "今日已跳过全部"
            hasRemainingReminderToday(config, now) -> "今日跳过全部"
            else -> "今天提醒已结束"
        }
    }

    fun todayEpochDay(): Long {
        return LocalDate.now().toEpochDay()
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.CHINA, "%02d:%02d", hour, minute)
    }

    fun formatNextReminder(config: ReminderConfig, now: LocalDateTime = LocalDateTime.now()): String {
        if (!config.enabled) return "提醒已关闭"

        val reminder = calculateNextReminder(config, now) ?: return "提醒已关闭"
        val reminderTime = reminder.dateTime
        val today = now.toLocalDate()
        val dayText = when (reminderTime.toLocalDate()) {
            today -> "今天"
            today.plusDays(1) -> "明天"
            else -> reminderTime.format(dateLabelFormatter)
        }

        return "$dayText ${formatTime(reminderTime.hour, reminderTime.minute)} ${reminder.mealType.displayName}"
    }

    private fun isWeekday(date: LocalDate): Boolean {
        return date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
    }

    private fun isSkippedDate(config: ReminderConfig, date: LocalDate): Boolean {
        return config.skippedDateEpochDay == date.toEpochDay()
    }
}
