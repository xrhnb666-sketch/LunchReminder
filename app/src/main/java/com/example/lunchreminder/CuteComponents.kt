package com.example.lunchreminder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CutePageTitle(
    title: String,
    subtitle: String? = null,
    icon: String? = null,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Text(text = icon, style = MaterialTheme.typography.displayMedium)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = CuteColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
    }
}

@Composable
fun CuteCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CuteColors.Card,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CuteDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = { content() },
    )
}

@Composable
fun CuteGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CuteDimens.ButtonRadius))
            .background(CuteColors.Orange)
            .clickable(onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun CuteOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(CuteDimens.ButtonRadius),
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = if (enabled) CuteColors.Orange else CuteColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun CuteSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = CuteColors.Orange,
            checkedBorderColor = CuteColors.Orange,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = CuteColors.SoftGray,
            uncheckedBorderColor = CuteColors.SoftGray,
        ),
    )
}

@Composable
fun CuteTabIcon(icon: String, selected: Boolean) {
    Text(
        text = icon,
        style = MaterialTheme.typography.titleMedium,
        color = if (selected) CuteColors.Orange else CuteColors.TextSecondary,
    )
}

@Composable
fun CuteArrow() {
    Text(
        text = "›",
        style = MaterialTheme.typography.headlineSmall,
        color = CuteColors.TextSecondary,
    )
}

@Composable
fun CuteDividerLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        drawLine(
            color = CuteColors.WarmLine,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

fun mealCardColor(mealType: MealType): Color {
    return when (mealType) {
        MealType.BREAKFAST -> CuteColors.Breakfast
        MealType.LUNCH -> CuteColors.Lunch
        MealType.DINNER -> CuteColors.Dinner
    }
}

fun mealEmoji(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "🍳"
        MealType.LUNCH -> "🍱"
        MealType.DINNER -> "🍜"
    }
}

fun mealShortName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
    }
}

fun mealDescription(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "清晨能量"
        MealType.LUNCH -> "先吃饭呀"
        MealType.DINNER -> "好好收尾"
    }
}
