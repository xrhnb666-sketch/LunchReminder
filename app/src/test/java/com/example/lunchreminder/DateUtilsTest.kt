package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DateUtilsTest {
    @Test
    fun calculateNextReminder_returnsTodayForFutureTimeOnNormalDate() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 9, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 8, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_returnsTomorrowWhenTodayTimePassed() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 13, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 9, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_returnsFridayInWeekdayModeBeforeFridayReminder() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
            weekdaysOnly = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 5, 9, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 5, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_skipsWeekendFromFridayAfterReminderInWeekdayMode() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
            weekdaysOnly = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 5, 13, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 8, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_skipsSaturdayInWeekdayMode() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
            weekdaysOnly = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 6, 9, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 8, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_skipsSundayInWeekdayMode() {
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
            weekdaysOnly = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 7, 9, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 8, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_skipsTodayWhenTodaySkipped() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val config = ReminderConfig(
            enabled = true,
            reminderHour = 12,
            reminderMinute = 0,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 9, 0),
        )

        assertEquals(LocalDateTime.of(2026, 6, 9, 12, 0), result)
    }
}
