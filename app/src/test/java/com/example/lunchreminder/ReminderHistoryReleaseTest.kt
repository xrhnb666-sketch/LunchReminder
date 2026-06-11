package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReminderHistoryReleaseTest {
    @Test
    fun decodeEmptyHistoryRepresentsClearedHistory() {
        assertTrue(ReminderHistory.decode("").isEmpty())
    }

    @Test
    fun clearingHistoryDataDoesNotChangeReminderConfigObject() {
        val config = ReminderConfig(
            breakfastEnabled = true,
            lunchEnabled = true,
            dinnerEnabled = true,
            weekdaysOnly = true,
            notificationSound = NotificationSound.GENTLE,
        )

        val clearedHistory = ReminderHistory.decode("")

        assertTrue(clearedHistory.isEmpty())
        assertEquals(true, config.breakfastEnabled)
        assertEquals(true, config.lunchEnabled)
        assertEquals(true, config.dinnerEnabled)
        assertEquals(true, config.weekdaysOnly)
        assertEquals(NotificationSound.GENTLE, config.notificationSound)
    }

    @Test
    fun statsReturnsZeroForNoHistoryRecords() {
        val stats = ReminderHistory.stats(
            records = emptyList(),
            today = LocalDate.of(2026, 6, 11),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(0, stats.todayCount)
        assertEquals(0, stats.weekCount)
        assertEquals(0, stats.totalCount)
    }

    @Test
    fun addRecordRepresentsOnlyActualReminderHistoryWrites() {
        val records = ReminderHistory.addRecord(
            records = emptyList(),
            record = historyRecord(
                dateTime = LocalDateTime.of(2026, 6, 11, 12, 0),
                mealType = MealType.LUNCH,
                message = "午饭时间到了",
            ),
        )

        assertEquals(1, records.size)
        assertEquals(MealType.LUNCH, records.single().mealType)
        assertEquals("午饭时间到了", records.single().message)
    }

    private fun historyRecord(
        dateTime: LocalDateTime,
        mealType: MealType,
        message: String,
    ): ReminderHistoryRecord {
        return ReminderHistoryRecord(
            timestampMillis = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            mealType = mealType,
            message = message,
        )
    }
}
