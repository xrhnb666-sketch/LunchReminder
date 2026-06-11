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
    fun calculateNextReminder_handlesAllMealEnableCombinations() {
        val cases = listOf(
            ReminderCase(
                config = ReminderConfig(
                    breakfastEnabled = true,
                    lunchEnabled = true,
                    dinnerEnabled = true,
                ),
                expectedMealType = MealType.BREAKFAST,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            ),
            ReminderCase(
                config = ReminderConfig(breakfastEnabled = true),
                expectedMealType = MealType.BREAKFAST,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            ),
            ReminderCase(
                config = ReminderConfig(lunchEnabled = true),
                expectedMealType = MealType.LUNCH,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
            ),
            ReminderCase(
                config = ReminderConfig(dinnerEnabled = true),
                expectedMealType = MealType.DINNER,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 18, 0),
            ),
            ReminderCase(
                config = ReminderConfig(breakfastEnabled = true, lunchEnabled = true),
                expectedMealType = MealType.BREAKFAST,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            ),
            ReminderCase(
                config = ReminderConfig(breakfastEnabled = true, dinnerEnabled = true),
                expectedMealType = MealType.BREAKFAST,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 8, 0),
            ),
            ReminderCase(
                config = ReminderConfig(lunchEnabled = true, dinnerEnabled = true),
                expectedMealType = MealType.LUNCH,
                expectedDateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
            ),
        )

        cases.forEach { case ->
            val result = DateUtils.calculateNextReminder(
                config = case.config,
                now = LocalDateTime.of(2026, 6, 8, 7, 0),
            )

            assertNextReminder(
                expectedMealType = case.expectedMealType,
                expectedDateTime = case.expectedDateTime,
                actual = result,
            )
        }
    }

    @Test
    fun calculateNextReminder_handlesWeekdaysOnlyAcrossEveryDayOfWeek() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            weekdaysOnly = true,
        )
        val cases = listOf(
            LocalDate.of(2026, 6, 8) to LocalDateTime.of(2026, 6, 8, 8, 0),
            LocalDate.of(2026, 6, 9) to LocalDateTime.of(2026, 6, 9, 8, 0),
            LocalDate.of(2026, 6, 10) to LocalDateTime.of(2026, 6, 10, 8, 0),
            LocalDate.of(2026, 6, 11) to LocalDateTime.of(2026, 6, 11, 8, 0),
            LocalDate.of(2026, 6, 12) to LocalDateTime.of(2026, 6, 12, 8, 0),
            LocalDate.of(2026, 6, 13) to LocalDateTime.of(2026, 6, 15, 8, 0),
            LocalDate.of(2026, 6, 14) to LocalDateTime.of(2026, 6, 15, 8, 0),
        )

        cases.forEach { (date, expectedDateTime) ->
            val result = DateUtils.calculateNextReminder(
                config = config,
                now = date.atTime(7, 0),
            )

            assertNextReminder(
                expectedMealType = MealType.BREAKFAST,
                expectedDateTime = expectedDateTime,
                actual = result,
            )
        }
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

    @Test
    fun isSkippedToday_returnsTrueWhenTodayHasAlreadyBeenSkipped() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )

        assertTrue(DateUtils.isSkippedToday(config, LocalDate.of(2026, 6, 8)))
    }

    @Test
    fun calculateNextReminder_returnsTodayReminderAfterSkipTodayIsCanceled() {
        val skippedDate = LocalDate.of(2026, 6, 8)
        val skippedConfig = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
        )
        val restoredConfig = skippedConfig.copy(skippedDateEpochDay = null)

        val result = DateUtils.calculateNextReminder(
            config = restoredConfig,
            now = LocalDateTime.of(2026, 6, 8, 9, 0),
        )

        assertNextReminder(
            expectedMealType = MealType.LUNCH,
            expectedDateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
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

    private data class ReminderCase(
        val config: ReminderConfig,
        val expectedMealType: MealType,
        val expectedDateTime: LocalDateTime,
    )
}
