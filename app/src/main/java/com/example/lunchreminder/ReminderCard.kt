package com.example.lunchreminder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeHeader(
    greeting: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
    ) {
        Image(
            painter = UiAssets.painter(UiAssets.cloudBackground),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.10f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Image(
                painter = UiAssets.painter(UiAssets.stars),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(18.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "三餐提醒",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "按时吃饭，照顾自己",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.TextSecondary,
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.Orange,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Image(
                painter = UiAssets.painter(UiAssets.plant),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
            )
        }
    }
}

@Composable
fun SkipTodayCard(
    todaySkipped: Boolean,
    onSkipTodayChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CuteCard(
        modifier = modifier,
        containerColor = CuteColors.SkipToday,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CuteColors.Card),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = UiAssets.painter(UiAssets.skipCloud),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                    )
                }
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
                modifier = Modifier
                    .testTag("skip_today_switch")
                    .semantics { contentDescription = "今日跳过开关" },
                checked = todaySkipped,
                onCheckedChange = onSkipTodayChange,
            )
        }
    }
}

@Composable
fun NextReminderCard(
    nextReminderText: String,
    todaySkipped: Boolean,
    modifier: Modifier = Modifier,
) {
    val reminderParts = rememberNextReminderParts(nextReminderText)

    CuteCard(
        modifier = modifier,
        containerColor = CuteColors.Card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = if (todaySkipped) "今天已跳过全部提醒" else "⏰ 下一次提醒",
                    style = MaterialTheme.typography.titleMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (todaySkipped) {
                        "下次提醒：$nextReminderText"
                    } else {
                        reminderParts.timeText
                    },
                    style = if (todaySkipped) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                if (!todaySkipped) {
                    Text(
                        text = reminderParts.mealText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CuteColors.TextSecondary,
                    )
                }
            }
            CuteArrow()
        }
    }
}

private data class NextReminderParts(
    val timeText: String,
    val mealText: String,
)

private fun rememberNextReminderParts(nextReminderText: String): NextReminderParts {
    val parts = nextReminderText.split(" ")
    return if (parts.size >= 3) {
        NextReminderParts(
            timeText = "${parts[0]} ${parts[1]}",
            mealText = parts.drop(2).joinToString(" "),
        )
    } else {
        NextReminderParts(
            timeText = nextReminderText,
            mealText = "记得按时吃饭",
        )
    }
}

@Composable
fun PermissionCard(onRequestPermission: () -> Unit) {
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
            Spacer(modifier = Modifier.width(12.dp))
            CuteGradientButton(
                modifier = Modifier.width(92.dp),
                text = "开启",
                onClick = onRequestPermission,
            )
        }
    }
}
