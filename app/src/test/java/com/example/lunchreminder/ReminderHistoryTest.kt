package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReminderHistoryTest {
    @Test
    fun addRecord_addsNewestRecordFirst() {
        val oldRecord = historyRecord(
            dateTime = LocalDateTime.of(2026, 6, 9, 8, 0),
            mealType = MealType.BREAKFAST,
        )
        val newRecord = historyRecord(
            dateTime = LocalDateTime.of(2026, 6, 9, 12, 0),
            mealType = MealType.LUNCH,
        )

        val records = ReminderHistory.addRecord(listOf(oldRecord), newRecord)

        assertEquals(listOf(newRecord, oldRecord), records)
    }

    @Test
    fun addRecord_keepsLatestOneHundredRecords() {
        val records = (0 until 105).fold(emptyList<ReminderHistoryRecord>()) { currentRecords, index ->
            ReminderHistory.addRecord(
                currentRecords,
                historyRecord(
                    dateTime = LocalDateTime.of(2026, 6, 9, 8, 0).plusMinutes(index.toLong()),
                    mealType = MealType.BREAKFAST,
                    message = "record $index",
                ),
            )
        }

        assertEquals(100, records.size)
        assertEquals("record 104", records.first().message)
        assertFalse(records.any { record -> record.message == "record 0" })
    }

    @Test
    fun stats_countsTodayWeekAndTotalRecords() {
        val records = listOf(
            historyRecord(
                dateTime = LocalDateTime.of(2026, 6, 9, 8, 0),
                mealType = MealType.BREAKFAST,
            ),
            historyRecord(
                dateTime = LocalDateTime.of(2026, 6, 8, 12, 0),
                mealType = MealType.LUNCH,
            ),
            historyRecord(
                dateTime = LocalDateTime.of(2026, 6, 1, 18, 0),
                mealType = MealType.DINNER,
            ),
        )

        val stats = ReminderHistory.stats(
            records = records,
            today = LocalDate.of(2026, 6, 9),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(1, stats.todayCount)
        assertEquals(2, stats.weekCount)
        assertEquals(3, stats.totalCount)
    }

    @Test
    fun encodeAndDecode_roundTripsRecords() {
        val records = listOf(
            historyRecord(
                dateTime = LocalDateTime.of(2026, 6, 9, 8, 0),
                mealType = MealType.BREAKFAST,
                message = "🍳 早餐时间到了",
            ),
        )

        val decodedRecords = ReminderHistory.decode(ReminderHistory.encode(records))

        assertEquals(records, decodedRecords)
    }

    private fun historyRecord(
        dateTime: LocalDateTime,
        mealType: MealType,
        message: String = mealType.displayName,
    ): ReminderHistoryRecord {
        return ReminderHistoryRecord(
            timestampMillis = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            mealType = mealType,
            message = message,
        )
    }
}
