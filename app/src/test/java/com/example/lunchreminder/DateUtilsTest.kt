package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DateUtilsTest {
    @Test
    fun calculateNextReminder_returnsBreakfastReminder() {
        val config = ReminderConfig(breakfastEnabled = true)

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 7, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.BREAKFAST,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_returnsLunchReminder() {
        val config = ReminderConfig(lunchEnabled = true)

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 9, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.LUNCH,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_returnsDinnerReminder() {
        val config = ReminderConfig(dinnerEnabled = true)

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 13, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.DINNER,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 18, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_sortsMultipleEnabledReminders() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 11, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.LUNCH,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_rollsAcrossDayToBreakfast() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 19, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.BREAKFAST,
            expectedDateTime = LocalDateTime.of(2026, 6, 9, 8, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_skipsWeekendInWeekdayMode() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            weekdaysOnly = true,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 5, 19, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.BREAKFAST,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_skipsTodayForAllMeals() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 7, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.BREAKFAST,
            expectedDateTime = LocalDateTime.of(2026, 6, 9, 8, 0),
            actual = result,
        )
    }

    @Test
    fun calculateNextReminder_returnsNullWhenAllMealsDisabled() {
        val result = DateUtils.calculateNextReminder(
            config = ReminderConfig(),
            now = LocalDateTime.of(2026, 6, 8, 7, 0),
        )

        assertEquals(null, result)
    }

    @Test
    fun hasRemainingReminderToday_returnsTrueWhenAnyEnabledMealIsStillUpcoming() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )

        val result = DateUtils.hasRemainingReminderToday(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 11, 0),
        )

        assertTrue(result)
    }

    @Test
    fun hasRemainingReminderToday_returnsFalseWhenAllEnabledMealsPassedToday() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )

        val result = DateUtils.hasRemainingReminderToday(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 19, 0),
        )

        assertFalse(result)
        assertEquals(
            "今天提醒已结束",
            DateUtils.skipTodayButtonText(config, LocalDateTime.of(2026, 6, 8, 19, 0)),
        )
    }

    @Test
    fun hasRemainingReminderToday_returnsFalseWhenTodayIsSkipped() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        val result = DateUtils.hasRemainingReminderToday(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 7, 0),
        )

        assertFalse(result)
        assertEquals(
            "今日已跳过全部",
            DateUtils.skipTodayButtonText(config, LocalDateTime.of(2026, 6, 8, 7, 0)),
        )
    }

    @Test
    fun hasRemainingReminderToday_returnsFalseOnWeekendInWeekdayMode() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            weekdaysOnly = true,
        )

        val result = DateUtils.hasRemainingReminderToday(
            config = config,
            now = LocalDateTime.of(2026, 6, 6, 7, 0),
        )

        assertFalse(result)
    }

    @Test
    fun skipTodayButtonText_returnsSkipAllWhenUpcomingMealExists() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )

        assertEquals(
            "今日跳过全部",
            DateUtils.skipTodayButtonText(config, LocalDateTime.of(2026, 6, 8, 11, 0)),
        )
    }

    @Test
    fun formatNextReminder_showsBreakfastAfterSkippingToday() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        val result = DateUtils.formatNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 8, 9, 0),
        )

        assertEquals("明天 08:00 早餐提醒", result)
    }

    @Test
    fun calculateNextReminder_skipsFromFridayToMondayBreakfastWhenTodaySkippedInWeekdayMode() {
        val skippedDate = LocalDate.of(2026, 6, 5)
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            weekdaysOnly = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 5, 7, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.BREAKFAST,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            actual = result,
        )
    }

    private fun assertNextReminder(
        expectedMealType: MealType,
        expectedDateTime: LocalDateTime,
        actual: NextReminder?,
    ) {
        assertNotNull(actual)
        assertEquals(expectedMealType, actual?.mealType)
        assertEquals(expectedDateTime, actual?.dateTime)
    }
}
