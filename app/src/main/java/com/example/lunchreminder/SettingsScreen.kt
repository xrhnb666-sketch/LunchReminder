package com.example.lunchreminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    config: ReminderConfig,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
    onSkipTodayChange: (Boolean) -> Unit,
    onMessagesChange: (MealType, String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            CutePageTitle(title = "设置", subtitle = "把提醒调成你喜欢的样子", icon = "🌿")
            ReminderSettingsGroup(
                config = config,
                onWeekdaysOnlyChange = onWeekdaysOnlyChange,
                onSkipTodayChange = onSkipTodayChange,
                onMessagesChange = onMessagesChange,
            )
            AppearanceSettingsGroup(
                themeMode = config.themeMode,
                onThemeModeChange = onThemeModeChange,
            )
            AboutAppCard()
        }
    }
}

@Composable
private fun ReminderSettingsGroup(
    config: ReminderConfig,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
    onSkipTodayChange: (Boolean) -> Unit,
    onMessagesChange: (MealType, String) -> Unit,
) {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GroupTitle(icon = "⏰", title = "提醒设置")
            SwitchSettingRow(
                title = "仅工作日提醒",
                subtitle = "周六周日自动跳过",
                checked = config.weekdaysOnly,
                onCheckedChange = onWeekdaysOnlyChange,
            )
            CuteDividerLine()
            SwitchSettingRow(
                title = "今日跳过全部",
                subtitle = "开启后今天不再提醒",
                checked = DateUtils.isSkippedToday(config),
                onCheckedChange = onSkipTodayChange,
            )
            CuteDividerLine()
            Text(
                text = "通知文案设置",
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "每行一条，提醒时随机选择。",
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
            MealType.entries.forEach { mealType ->
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = messagesFor(config, mealType),
                    onValueChange = { value -> onMessagesChange(mealType, value) },
                    label = { Text("${mealShortName(mealType)}文案") },
                    minLines = 1,
                    maxLines = 3,
                    leadingIcon = { Text(mealEmoji(mealType)) },
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsGroup(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GroupTitle(icon = "🎨", title = "外观设置")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    if (mode == themeMode) {
                        CuteGradientButton(
                            modifier = Modifier.weight(1f),
                            text = mode.label,
                            onClick = { onThemeModeChange(mode) },
                        )
                    } else {
                        CuteOutlinedButton(
                            modifier = Modifier.weight(1f),
                            text = mode.label,
                            onClick = { onThemeModeChange(mode) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutAppCard() {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier.padding(CuteDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GroupTitle(icon = "🍱", title = "关于")
            SettingInfoRow(title = "LunchReminder", subtitle = "版本号：${BuildConfig.VERSION_NAME}")
            CuteDividerLine()
            SettingInfoRow(
                title = "GitHub",
                subtitle = "github.com/xrhnb666-sketch/LunchReminder",
            )
            CuteDividerLine()
            Text(
                text = "一个温柔提醒你好好吃饭的生活工具。",
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun GroupTitle(icon: String, title: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CuteColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.width(CuteDimens.CardSpacing))
        CuteSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingInfoRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
        CuteArrow()
    }
}

private fun messagesFor(config: ReminderConfig, mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> config.breakfastMessages
        MealType.LUNCH -> config.lunchMessages
        MealType.DINNER -> config.dinnerMessages
    }
}
