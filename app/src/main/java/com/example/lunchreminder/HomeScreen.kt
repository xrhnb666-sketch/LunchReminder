package com.example.lunchreminder

import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    config: ReminderConfig,
    hasNotificationPermission: Boolean,
    onMealTimeChange: (MealType, Int, Int) -> Unit,
    onMealEnabledChange: (MealType, Boolean) -> Unit,
    onSkipTodayChange: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CuteDimens.PagePadding, vertical = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
            ) {
                HomeHeader()

                MealType.entries.forEach { mealType ->
                    MealReminderCard(
                        mealType = mealType,
                        timeText = DateUtils.formatTime(
                            config.hourFor(mealType),
                            config.minuteFor(mealType),
                        ),
                        enabled = config.isEnabled(mealType),
                        todaySkipped = DateUtils.isSkippedToday(config),
                        onTimeClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> onMealTimeChange(mealType, hour, minute) },
                                config.hourFor(mealType),
                                config.minuteFor(mealType),
                                true,
                            ).show()
                        },
                        onEnabledChange = { checked ->
                            onMealEnabledChange(mealType, checked)
                            if (
                                checked &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !hasNotificationPermission
                            ) {
                                onRequestPermission()
                            }
                        },
                    )
                }

                SkipTodayCard(
                    todaySkipped = DateUtils.isSkippedToday(config),
                    onSkipTodayChange = onSkipTodayChange,
                )

                NextReminderCard(
                    nextReminderText = DateUtils.formatNextReminder(config),
                    todaySkipped = DateUtils.isSkippedToday(config),
                )

                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasNotificationPermission
                ) {
                    PermissionCard(onRequestPermission = onRequestPermission)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            CuteGradientButton(
                modifier = Modifier.fillMaxWidth(),
                text = "测试通知",
                onClick = onTestNotification,
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    CuteCard(containerColor = CuteColors.Card) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "三餐提醒",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "按时吃饭，照顾自己",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.TextSecondary,
                )
            }
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CuteColors.Breakfast),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🍱🌱", fontSize = 32.sp)
            }
        }
    }
}

@Composable
private fun MealReminderCard(
    mealType: MealType,
    timeText: String,
    enabled: Boolean,
    todaySkipped: Boolean,
    onTimeClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    CuteCard(containerColor = mealCardColor(mealType)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CuteColors.Card),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = mealEmoji(mealType), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTimeClick),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = mealShortName(mealType),
                    style = MaterialTheme.typography.titleMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = timeText,
                    fontSize = 36.sp,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (todaySkipped) "今日已跳过" else mealDescription(mealType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            CuteSwitch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
    }
}

@Composable
private fun SkipTodayCard(
    todaySkipped: Boolean,
    onSkipTodayChange: (Boolean) -> Unit,
) {
    CuteCard(containerColor = CuteColors.SkipToday) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "☁️", style = MaterialTheme.typography.headlineSmall)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "今日跳过全部",
                        style = MaterialTheme.typography.titleMedium,
                        color = CuteColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "开启后今天不再提醒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CuteColors.TextSecondary,
                    )
                }
            }
            CuteSwitch(
                checked = todaySkipped,
                onCheckedChange = onSkipTodayChange,
            )
        }
    }
}

@Composable
private fun NextReminderCard(
    nextReminderText: String,
    todaySkipped: Boolean,
) {
    CuteCard(containerColor = CuteColors.Card) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (todaySkipped) "今天已跳过全部提醒" else "下一次提醒",
                    style = MaterialTheme.typography.titleMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (todaySkipped) "下次提醒：$nextReminderText" else nextReminderPrimaryText(nextReminderText),
                    style = if (todaySkipped) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                if (!todaySkipped) {
                    Text(
                        text = nextReminderSecondaryText(nextReminderText),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CuteColors.TextSecondary,
                    )
                }
            }
            CuteArrow()
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    CuteCard(containerColor = CuteColors.Lunch) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "需要通知权限",
                    style = MaterialTheme.typography.titleMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "允许后才能显示提醒通知",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.TextSecondary,
                )
            }
            CuteGradientButton(
                modifier = Modifier.width(92.dp),
                text = "开启",
                onClick = onRequestPermission,
            )
        }
    }
}

private fun nextReminderPrimaryText(nextReminderText: String): String {
    val parts = nextReminderText.split(" ")
    return if (parts.size >= 2) "${parts[0]} ${parts[1]}" else nextReminderText
}

private fun nextReminderSecondaryText(nextReminderText: String): String {
    val parts = nextReminderText.split(" ")
    return if (parts.size >= 3) parts.drop(2).joinToString(" ") else "好好吃饭"
}
