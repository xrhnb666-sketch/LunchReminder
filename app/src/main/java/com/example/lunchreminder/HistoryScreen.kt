package com.example.lunchreminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    records: List<ReminderHistoryRecord>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirmClear by remember { mutableStateOf(false) }
    val groupedRecords = remember(records) { groupHistoryRecords(records) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CuteDimens.PagePadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CutePageTitle(title = "历史记录", subtitle = "每一次好好吃饭都算数")
                CuteOutlinedButton(
                    enabled = records.isNotEmpty(),
                    text = "清空",
                    onClick = { showConfirmClear = true },
                )
            }

            if (records.isEmpty()) {
                CuteCard(containerColor = CuteColors.Card) {
                    Text(
                        modifier = Modifier.padding(CuteDimens.CardPadding),
                        text = "暂无提醒记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CuteColors.TextSecondary,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
                ) {
                    groupedRecords.forEach { group ->
                        HistoryDayGroup(group = group)
                    }
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("清空历史记录？") },
            text = { Text("清空后无法恢复。") },
            confirmButton = {
                CuteGradientButton(
                    text = "清空",
                    onClick = {
                        showConfirmClear = false
                        onClearHistory()
                    },
                )
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun HistoryDayGroup(group: HistoryGroup) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = group.label,
            style = MaterialTheme.typography.titleMedium,
            color = CuteColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        CuteCard(containerColor = CuteColors.Card) {
            Column {
                group.records.forEachIndexed { index, record ->
                    HistoryRecordRow(record = record)
                    if (index != group.records.lastIndex) {
                        CuteDividerLine(modifier = Modifier.padding(horizontal = CuteDimens.CardPadding))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordRow(record: ReminderHistoryRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CuteDimens.CardPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(mealCardColor(record.mealType), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mealEmoji(record.mealType),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${formatRecordTime(record.timestampMillis)} ${record.mealType.displayName}",
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = record.message,
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
        CuteArrow()
    }
}

private data class HistoryGroup(
    val label: String,
    val records: List<ReminderHistoryRecord>,
)

private fun groupHistoryRecords(records: List<ReminderHistoryRecord>): List<HistoryGroup> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val grouped = records.groupBy { record -> record.localDate() }

    return grouped
        .toSortedMap(compareByDescending { date -> date })
        .map { (date, dayRecords) ->
            HistoryGroup(
                label = when (date) {
                    today -> "今天"
                    yesterday -> "昨天"
                    else -> "更早 ${date.format(dateFormatter)}"
                },
                records = dayRecords.sortedByDescending { record -> record.timestampMillis },
            )
        }
}

private fun ReminderHistoryRecord.localDate(): LocalDate {
    return Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun formatRecordTime(timestampMillis: Long): String {
    return Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .format(timeFormatter)
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
private val dateFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
