package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DateUtilsReleaseTest {
    @Test
    fun calculateNextReminder_selectsBreakfastWhenNowIsBeforeBreakfast() {
        val config = allMealsEnabled()

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 11, 7, 30),
        )

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 11, 8, 0), result)
    }

    @Test
    fun calculateNextReminder_selectsLunchWhenNowIsBetweenBreakfastAndLunch() {
        val config = allMealsEnabled()

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 11, 9, 30),
        )

        assertNext(MealType.LUNCH, LocalDateTime.of(2026, 6, 11, 12, 0), result)
    }

    @Test
    fun calculateNextReminder_selectsDinnerWhenNowIsBetweenLunchAndDinner() {
        val config = allMealsEnabled()

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 11, 13, 30),
        )

        assertNext(MealType.DINNER, LocalDateTime.of(2026, 6, 11, 18, 0), result)
    }

    @Test
    fun calculateNextReminder_rollsToNextDayBreakfastWhenNowIsAfterDinner() {
        val config = allMealsEnabled()

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 11, 19, 30),
        )

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 12, 8, 0), result)
    }

    @Test
    fun calculateNextReminder_returnsNullWhenAllMealsAreOff() {
        val result = DateUtils.calculateNextReminder(
            config = ReminderConfig(),
            now = LocalDateTime.of(2026, 6, 11, 7, 30),
        )

        assertNull(result)
    }

    @Test
    fun calculateNextReminder_handlesEverySingleMealAndTwoMealCombination() {
        val now = LocalDateTime.of(2026, 6, 11, 9, 0)
        val cases = listOf(
            ReminderConfig(breakfastEnabled = true) to
                (MealType.BREAKFAST to LocalDateTime.of(2026, 6, 12, 8, 0)),
            ReminderConfig(lunchEnabled = true) to
                (MealType.LUNCH to LocalDateTime.of(2026, 6, 11, 12, 0)),
            ReminderConfig(dinnerEnabled = true) to
                (MealType.DINNER to LocalDateTime.of(2026, 6, 11, 18, 0)),
            ReminderConfig(breakfastEnabled = true, lunchEnabled = true) to
                (MealType.LUNCH to LocalDateTime.of(2026, 6, 11, 12, 0)),
            ReminderConfig(breakfastEnabled = true, dinnerEnabled = true) to
                (MealType.DINNER to LocalDateTime.of(2026, 6, 11, 18, 0)),
            ReminderConfig(lunchEnabled = true, dinnerEnabled = true) to
                (MealType.LUNCH to LocalDateTime.of(2026, 6, 11, 12, 0)),
        )

        cases.forEach { (config, expected) ->
            val result = DateUtils.calculateNextReminder(config, now)

            assertNext(expected.first, expected.second, result)
        }
    }

    @Test
    fun calculateNextReminder_usesUpdatedCustomMealTimes() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            breakfastHour = 7,
            breakfastMinute = 45,
            lunchHour = 11,
            lunchMinute = 20,
            dinnerHour = 19,
            dinnerMinute = 10,
        )

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 11, 11, 30),
        )

        assertNext(MealType.DINNER, LocalDateTime.of(2026, 6, 11, 19, 10), result)
    }

    @Test
    fun calculateNextReminder_weekdayModeMovesFromFridayNightToMondayBreakfast() {
        val config = allMealsEnabled().copy(weekdaysOnly = true)

        val result = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 6, 12, 19, 0),
        )

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 15, 8, 0), result)
    }

    @Test
    fun calculateNextReminder_weekdayModeMovesSaturdayAndSundayToMondayBreakfast() {
        val config = allMealsEnabled().copy(weekdaysOnly = true)
        val saturday = DateUtils.calculateNextReminder(config, LocalDateTime.of(2026, 6, 13, 7, 0))
        val sunday = DateUtils.calculateNextReminder(config, LocalDateTime.of(2026, 6, 14, 7, 0))

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 15, 8, 0), saturday)
        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 15, 8, 0), sunday)
    }

    @Test
    fun calculateNextReminder_skipTodayMovesToTomorrowThenCancelRestoresToday() {
        val today = LocalDate.of(2026, 6, 11)
        val skipped = allMealsEnabled().copy(skippedDateEpochDay = today.toEpochDay())
        val restored = skipped.copy(skippedDateEpochDay = null)

        val skippedResult = DateUtils.calculateNextReminder(
            config = skipped,
            now = today.atTime(9, 0),
        )
        val restoredResult = DateUtils.calculateNextReminder(
            config = restored,
            now = today.atTime(9, 0),
        )

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 6, 12, 8, 0), skippedResult)
        assertNext(MealType.LUNCH, LocalDateTime.of(2026, 6, 11, 12, 0), restoredResult)
    }

    @Test
    fun calculateNextReminder_handlesCrossMonthAndCrossYear() {
        val config = ReminderConfig(breakfastEnabled = true)

        val crossMonth = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 1, 31, 23, 0),
        )
        val crossYear = DateUtils.calculateNextReminder(
            config = config,
            now = LocalDateTime.of(2026, 12, 31, 23, 0),
        )

        assertNext(MealType.BREAKFAST, LocalDateTime.of(2026, 2, 1, 8, 0), crossMonth)
        assertNext(MealType.BREAKFAST, LocalDateTime.of(2027, 1, 1, 8, 0), crossYear)
    }

    private fun allMealsEnabled(): ReminderConfig {
        return ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
        )
    }

    private fun assertNext(
        expectedMealType: MealType,
        expectedDateTime: LocalDateTime,
        actual: NextReminder?,
    ) {
        assertEquals(expectedMealType, actual?.mealType)
        assertEquals(expectedDateTime, actual?.dateTime)
    }
}
