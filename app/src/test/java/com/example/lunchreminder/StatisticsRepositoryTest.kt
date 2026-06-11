package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class StatisticsRepositoryTest {
    @Test
    fun calculate_countsTodayWeekAndMonthRecords() {
        val records = listOf(
            historyRecord(LocalDateTime.of(2026, 6, 10, 8, 0), MealType.BREAKFAST),
            historyRecord(LocalDateTime.of(2026, 6, 9, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 1, 18, 0), MealType.DINNER),
            historyRecord(LocalDateTime.of(2026, 5, 30, 8, 0), MealType.BREAKFAST),
        )

        val summary = StatisticsRepository.calculate(
            records = records,
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(1, summary.todayCount)
        assertEquals(2, summary.weekCount)
        assertEquals(3, summary.monthCount)
    }

    @Test
    fun calculate_countsMealTypes() {
        val records = listOf(
            historyRecord(LocalDateTime.of(2026, 6, 10, 8, 0), MealType.BREAKFAST),
            historyRecord(LocalDateTime.of(2026, 6, 10, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 9, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 8, 18, 0), MealType.DINNER),
        )

        val summary = StatisticsRepository.calculate(
            records = records,
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(1, summary.breakfastCount)
        assertEquals(2, summary.lunchCount)
        assertEquals(1, summary.dinnerCount)
    }

    @Test
    fun calculate_countsStreakDaysFromToday() {
        val records = listOf(
            historyRecord(LocalDateTime.of(2026, 6, 10, 8, 0), MealType.BREAKFAST),
            historyRecord(LocalDateTime.of(2026, 6, 9, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 8, 18, 0), MealType.DINNER),
            historyRecord(LocalDateTime.of(2026, 6, 6, 8, 0), MealType.BREAKFAST),
        )

        val summary = StatisticsRepository.calculate(
            records = records,
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(3, summary.streakDays)
    }

    @Test
    fun calculate_returnsZeroStreakWhenTodayHasNoRecords() {
        val records = listOf(
            historyRecord(LocalDateTime.of(2026, 6, 9, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 8, 18, 0), MealType.DINNER),
        )

        val summary = StatisticsRepository.calculate(
            records = records,
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(0, summary.streakDays)
    }

    @Test
    fun calculate_returnsMealPercentages() {
        val records = listOf(
            historyRecord(LocalDateTime.of(2026, 6, 10, 8, 0), MealType.BREAKFAST),
            historyRecord(LocalDateTime.of(2026, 6, 10, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 9, 12, 0), MealType.LUNCH),
            historyRecord(LocalDateTime.of(2026, 6, 8, 18, 0), MealType.DINNER),
        )

        val summary = StatisticsRepository.calculate(
            records = records,
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(0.25f, summary.breakfastPercent, 0.001f)
        assertEquals(0.5f, summary.lunchPercent, 0.001f)
        assertEquals(0.25f, summary.dinnerPercent, 0.001f)
    }

    @Test
    fun calculate_returnsZeroPercentagesWhenNoRecordsExist() {
        val summary = StatisticsRepository.calculate(
            records = emptyList(),
            today = LocalDate.of(2026, 6, 10),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(0, summary.todayCount)
        assertEquals(0, summary.weekCount)
        assertEquals(0, summary.monthCount)
        assertEquals(0, summary.streakDays)
        assertEquals(0, summary.breakfastCount)
        assertEquals(0, summary.lunchCount)
        assertEquals(0, summary.dinnerCount)
        assertEquals(0f, summary.breakfastPercent, 0.001f)
        assertEquals(0f, summary.lunchPercent, 0.001f)
        assertEquals(0f, summary.dinnerPercent, 0.001f)
    }

    private fun historyRecord(
        dateTime: LocalDateTime,
        mealType: MealType,
    ): ReminderHistoryRecord {
        return ReminderHistoryRecord(
            timestampMillis = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            mealType = mealType,
            message = mealType.displayName,
        )
    }
}
