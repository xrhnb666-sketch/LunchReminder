package com.example.lunchreminder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MealReminderCard(
    mealType: MealType,
    timeText: String,
    enabled: Boolean,
    todaySkipped: Boolean,
    onTimeClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CuteCard(
        modifier = modifier.defaultMinSize(minHeight = 92.dp),
        containerColor = mealCardColor(mealType),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CuteColors.Card),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = UiAssets.painter(mealIconRes(mealType)),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTimeClick),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = mealShortName(mealType),
                    style = MaterialTheme.typography.labelLarge,
                    color = CuteColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = timeText,
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    color = mealAccentColor(mealType),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (todaySkipped) "今日已跳过" else mealDescription(mealType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CuteColors.TextSecondary,
                )
            }
            CuteSwitch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
    }
}
