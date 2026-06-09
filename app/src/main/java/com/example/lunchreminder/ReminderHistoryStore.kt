package com.example.lunchreminder

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReminderHistoryRecord(
    val timestampMillis: Long,
    val mealType: MealType,
    val message: String,
)

data class ReminderHistoryStats(
    val todayCount: Int,
    val weekCount: Int,
    val totalCount: Int,
)

object ReminderHistory {
    const val MAX_RECORDS = 100

    fun addRecord(
        records: List<ReminderHistoryRecord>,
        record: ReminderHistoryRecord,
    ): List<ReminderHistoryRecord> {
        return (records + record)
            .sortedByDescending { item -> item.timestampMillis }
            .take(MAX_RECORDS)
    }

    fun stats(
        records: List<ReminderHistoryRecord>,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): ReminderHistoryStats {
        val weekStart = DateUtils.weekStart(today)
        val weekEnd = weekStart.plusDays(6)
        val todayCount = records.count { record ->
            record.localDate(zoneId) == today
        }
        val weekCount = records.count { record ->
            val date = record.localDate(zoneId)
            !date.isBefore(weekStart) && !date.isAfter(weekEnd)
        }

        return ReminderHistoryStats(
            todayCount = todayCount,
            weekCount = weekCount,
            totalCount = records.size,
        )
    }

    fun encode(records: List<ReminderHistoryRecord>): String {
        return records.joinToString(separator = "\n") { record ->
            listOf(
                record.timestampMillis.toString(),
                record.mealType.storageKey,
                encodeField(record.message),
            ).joinToString(separator = "|")
        }
    }

    fun decode(raw: String): List<ReminderHistoryRecord> {
        return raw
            .lineSequence()
            .mapNotNull { line -> decodeLine(line) }
            .sortedByDescending { record -> record.timestampMillis }
            .take(MAX_RECORDS)
            .toList()
    }

    private fun decodeLine(line: String): ReminderHistoryRecord? {
        val parts = line.split("|", limit = 3)
        if (parts.size != 3) return null

        val timestampMillis = parts[0].toLongOrNull() ?: return null
        val mealType = MealType.fromStorageKey(parts[1]) ?: return null
        return ReminderHistoryRecord(
            timestampMillis = timestampMillis,
            mealType = mealType,
            message = decodeField(parts[2]),
        )
    }

    private fun encodeField(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    }

    private fun decodeField(value: String): String {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    }
}

class ReminderHistoryStore(private val context: Context) {
    private object Keys {
        val Records = stringPreferencesKey("history_records")
    }

    val historyFlow: Flow<List<ReminderHistoryRecord>> = context.reminderDataStore.data.map { preferences ->
        ReminderHistory.decode(preferences[Keys.Records].orEmpty())
    }

    suspend fun addRecord(record: ReminderHistoryRecord) {
        context.reminderDataStore.edit { preferences ->
            val currentRecords = ReminderHistory.decode(preferences[Keys.Records].orEmpty())
            preferences[Keys.Records] = ReminderHistory.encode(
                ReminderHistory.addRecord(currentRecords, record),
            )
        }
    }

    suspend fun clear() {
        context.reminderDataStore.edit { preferences ->
            preferences.remove(Keys.Records)
        }
    }
}

private fun ReminderHistoryRecord.localDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(timestampMillis)
        .atZone(zoneId)
        .toLocalDate()
}
