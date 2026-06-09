package com.example.lunchreminder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(
    summary: StatisticsSummary,
    records: List<ReminderHistoryRecord>,
    modifier: Modifier = Modifier,
) {
    val weeklyTrend = remember(records) { weeklyTrend(records) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CuteDimens.PagePadding, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
        ) {
            CutePageTitle(title = "统计分析", subtitle = "看看最近有没有好好吃饭")
            TodaySummaryCard(summary = summary)
            MealDistributionCard(summary = summary)
            WeeklyTrendCard(points = weeklyTrend)
        }
    }
}

@Composable
private fun TodaySummaryCard(summary: StatisticsSummary) {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "今日数据",
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BigNumber(label = "今日提醒", value = "${summary.todayCount}")
                BigNumber(label = "本周提醒", value = "${summary.weekCount}")
                BigNumber(label = "本月提醒", value = "${summary.monthCount}")
                BigNumber(label = "连续天数", value = "${summary.streakDays}")
            }
        }
    }
}

@Composable
private fun BigNumber(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = CuteColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CuteColors.TextSecondary,
        )
    }
}

@Composable
private fun MealDistributionCard(summary: StatisticsSummary) {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "餐次占比",
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PieChart(
                    breakfast = summary.breakfastPercent,
                    lunch = summary.lunchPercent,
                    dinner = summary.dinnerPercent,
                    modifier = Modifier.size(132.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendRow("🍳 早餐", summary.breakfastPercent, CuteColors.Orange)
                    LegendRow("🍱 午餐", summary.lunchPercent, CuteColors.ChartBrown)
                    LegendRow("🍜 晚餐", summary.dinnerPercent, CuteColors.ChartGreen)
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    breakfast: Float,
    lunch: Float,
    dinner: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
        val chartSize = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx())
        val topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
        val values = listOf(
            breakfast to CuteColors.Orange,
            lunch to CuteColors.ChartBrown,
            dinner to CuteColors.ChartGreen,
        )

        var startAngle = -90f
        if (values.sumOf { (value, _) -> value.toDouble() } == 0.0) {
            drawArc(
                color = CuteColors.WarmLine,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = stroke,
            )
        } else {
            values.forEach { (value, color) ->
                val sweep = value * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = stroke,
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, percent: Float, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(1.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CuteColors.TextPrimary,
        )
        Text(
            text = formatPercent(percent),
            style = MaterialTheme.typography.bodyMedium,
            color = CuteColors.TextSecondary,
        )
    }
}

@Composable
private fun WeeklyTrendCard(points: List<TrendPoint>) {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "每日趋势（本周）",
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            LineChart(points = points, modifier = Modifier.fillMaxWidth().height(150.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                points.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = CuteColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(points: List<TrendPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val maxCount = (points.maxOfOrNull { point -> point.count } ?: 0).coerceAtLeast(1)
        val left = 12.dp.toPx()
        val right = size.width - 12.dp.toPx()
        val top = 14.dp.toPx()
        val bottom = size.height - 16.dp.toPx()
        val widthStep = if (points.size > 1) (right - left) / (points.size - 1) else 0f
        val chartPoints = points.mapIndexed { index, point ->
            val x = left + widthStep * index
            val y = bottom - (point.count.toFloat() / maxCount.toFloat()) * (bottom - top)
            Offset(x, y)
        }

        repeat(4) { row ->
            val y = top + (bottom - top) * row / 3f
            drawLine(
                color = CuteColors.WarmLine,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        chartPoints.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = CuteColors.Orange,
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        chartPoints.forEach { point ->
            drawCircle(color = CuteColors.Orange, radius = 5.dp.toPx(), center = point)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
        }
    }
}

private data class TrendPoint(
    val label: String,
    val count: Int,
)

private fun weeklyTrend(records: List<ReminderHistoryRecord>): List<TrendPoint> {
    val today = LocalDate.now()
    val weekStart = DateUtils.weekStart(today)
    val counts = records.groupingBy { record ->
        Instant.ofEpochMilli(record.timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }.eachCount()

    return (0..6).map { offset ->
        val date = weekStart.plusDays(offset.toLong())
        TrendPoint(
            label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA),
            count = counts[date] ?: 0,
        )
    }
}

private fun formatPercent(percent: Float): String {
    return String.format(Locale.CHINA, "%.0f%%", percent * 100f)
}
