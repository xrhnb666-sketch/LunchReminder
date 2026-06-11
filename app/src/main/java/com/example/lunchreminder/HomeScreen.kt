package com.example.lunchreminder

import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime

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
    val todaySkipped = DateUtils.isSkippedToday(config)
    val anyMealEnabled = MealType.entries.any { mealType -> config.isEnabled(mealType) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UiAssetBackgroundSlot()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CuteDimens.PagePadding)
                    .padding(top = 18.dp, bottom = 14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HomeHeader(greeting = homeGreeting())

                    MealType.entries.forEach { mealType ->
                        AnimatedMealCard(
                            mealType = mealType,
                            animationIndex = mealType.ordinal,
                        ) {
                            MealReminderCard(
                                mealType = mealType,
                                timeText = DateUtils.formatTime(
                                    config.hourFor(mealType),
                                    config.minuteFor(mealType),
                                ),
                                enabled = config.isEnabled(mealType),
                                todaySkipped = todaySkipped,
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
                    }

                    if (anyMealEnabled) {
                        SkipTodayCard(
                            todaySkipped = todaySkipped,
                            onSkipTodayChange = onSkipTodayChange,
                        )

                        NextReminderCard(
                            nextReminderText = DateUtils.formatNextReminder(config),
                            todaySkipped = todaySkipped,
                        )
                    } else {
                        HomeAllDisabledState()
                    }

                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !hasNotificationPermission
                    ) {
                        PermissionCard(onRequestPermission = onRequestPermission)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                CuteGradientButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "测试通知",
                    onClick = onTestNotification,
                )
            }
        }
    }
}

@Composable
private fun AnimatedMealCard(
    mealType: MealType,
    animationIndex: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember(mealType) { mutableStateOf(false) }

    LaunchedEffect(mealType) {
        delay(animationIndex * UiConstants.Animation.CardStaggerMillis)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(UiConstants.Animation.PageTransitionMillis)),
    ) {
        content()
    }
}

@Composable
private fun HomeAllDisabledState() {
    CuteCard(containerColor = CuteColors.Card) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CuteDimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "今天不需要提醒啦～",
                style = MaterialTheme.typography.headlineSmall,
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "记得按时吃饭哦 🍀",
                style = MaterialTheme.typography.bodyMedium,
                color = CuteColors.TextSecondary,
            )
        }
    }
}

private fun homeGreeting(now: LocalTime = LocalTime.now()): String {
    return when {
        now.hour < 11 -> "☀️ 早安，记得吃早餐"
        now.hour < 17 -> "🍱 午安，按时吃饭哦"
        else -> "🍜 晚安，好好吃饭"
    }
}
