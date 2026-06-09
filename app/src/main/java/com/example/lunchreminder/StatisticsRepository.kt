package com.example.lunchreminder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StatisticsSummary(
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val streakDays: Int,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int,
    val breakfastPercent: Float,
    val lunchPercent: Float,
    val dinnerPercent: Float,
)

class StatisticsRepository(private val historyStore: ReminderHistoryStore) {
    val statisticsFlow: Flow<StatisticsSummary> = historyStore.historyFlow.map { records ->
        calculate(records)
    }

    companion object {
        fun calculate(
            records: List<ReminderHistoryRecord>,
            today: LocalDate = LocalDate.now(),
            zoneId: ZoneId = ZoneId.systemDefault(),
        ): StatisticsSummary {
            val datedRecords = records.map { record ->
                DatedHistoryRecord(date = record.localDate(zoneId))
            }
            val weekStart = DateUtils.weekStart(today)
            val monthStart = today.withDayOfMonth(1)
            val breakfastCount = records.count { record -> record.mealType == MealType.BREAKFAST }
            val lunchCount = records.count { record -> record.mealType == MealType.LUNCH }
            val dinnerCount = records.count { record -> record.mealType == MealType.DINNER }
            val totalMealCount = breakfastCount + lunchCount + dinnerCount

            return StatisticsSummary(
                todayCount = datedRecords.count { item -> item.date == today },
                weekCount = datedRecords.count { item ->
                    !item.date.isBefore(weekStart) && !item.date.isAfter(today)
                },
                monthCount = datedRecords.count { item ->
                    !item.date.isBefore(monthStart) && !item.date.isAfter(today)
                },
                streakDays = calculateStreakDays(datedRecords.map { item -> item.date }.toSet(), today),
                breakfastCount = breakfastCount,
                lunchCount = lunchCount,
                dinnerCount = dinnerCount,
                breakfastPercent = percentage(breakfastCount, totalMealCount),
                lunchPercent = percentage(lunchCount, totalMealCount),
                dinnerPercent = percentage(dinnerCount, totalMealCount),
            )
        }

        private fun calculateStreakDays(recordDates: Set<LocalDate>, today: LocalDate): Int {
            var date = today
            var count = 0

            while (recordDates.contains(date)) {
                count += 1
                date = date.minusDays(1)
            }

            return count
        }

        private fun percentage(count: Int, total: Int): Float {
            if (total == 0) return 0f
            return count.toFloat() / total.toFloat()
        }
    }
}

private data class DatedHistoryRecord(
    val date: LocalDate,
)

private fun ReminderHistoryRecord.localDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(timestampMillis)
        .atZone(zoneId)
        .toLocalDate()
}
