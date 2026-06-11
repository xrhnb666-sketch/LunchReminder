package com.example.lunchreminder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CutePageTitle(
    title: String,
    subtitle: String? = null,
    illustrationRes: Int? = null,
    illustrationSize: Dp = 72.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (illustrationRes != null) {
            Image(
                painter = UiAssets.painter(illustrationRes),
                contentDescription = null,
                modifier = Modifier.size(illustrationSize),
            )
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
    val shape = RoundedCornerShape(CuteDimens.CardRadius)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color(0x1AD5A06C),
                spotColor = Color(0x1AD5A06C),
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
            .background(
                Brush.horizontalGradient(
                    colors = listOf(CuteColors.OrangeStart, CuteColors.OrangeEnd),
                ),
            )
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
    val switchScale by animateFloatAsState(
        targetValue = if (checked) 1.04f else 1f,
        animationSpec = tween(UiConstants.Animation.SwitchScaleMillis),
        label = "switchScale",
    )

    Switch(
        modifier = modifier.scale(switchScale),
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

fun mealAccentColor(mealType: MealType): Color {
    return when (mealType) {
        MealType.BREAKFAST -> CuteColors.BreakfastAccent
        MealType.LUNCH -> CuteColors.LunchAccent
        MealType.DINNER -> CuteColors.DinnerAccent
    }
}

fun mealIconRes(mealType: MealType): Int {
    return when (mealType) {
        MealType.BREAKFAST -> UiAssets.breakfast
        MealType.LUNCH -> UiAssets.lunch
        MealType.DINNER -> UiAssets.dinner
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
