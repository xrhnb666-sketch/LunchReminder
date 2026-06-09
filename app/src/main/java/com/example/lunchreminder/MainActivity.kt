package com.example.lunchreminder

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LunchNotification.ensureChannel(applicationContext)

        setContent {
            val settings = remember { ReminderSettings(applicationContext) }
            val config by settings.configFlow.collectAsState(initial = ReminderConfig())

            LunchReminderTheme(themeMode = config.themeMode) {
                LunchReminderScreen(
                    settings = settings,
                    config = config,
                )
            }
        }
    }
}

@Composable
private fun LunchReminderTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = ThemeUtils.shouldUseDarkTheme(
        themeMode = themeMode,
        systemInDarkTheme = isSystemInDarkTheme(),
    )
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Composable
private fun LunchReminderScreen(
    settings: ReminderSettings,
    config: ReminderConfig,
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var showAbout by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember { mutableStateOf(LunchNotification.canPostNotifications(context)) }
    var sendTestAfterPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasNotificationPermission = granted
        if (granted && sendTestAfterPermission) {
            LunchNotification.show(context.applicationContext, MealType.LUNCH, config)
        }
        sendTestAfterPermission = false
    }

    LaunchedEffect(Unit) {
        hasNotificationPermission = LunchNotification.canPostNotifications(context)
    }

    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PagePadding, vertical = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeaderSection()

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
                                { _, hour, minute ->
                                    scope.launch {
                                        settings.updateMealTime(mealType, hour, minute)
                                        scheduler.scheduleAll(
                                            config.withMealTime(mealType, hour, minute),
                                        )
                                    }
                                },
                                config.hourFor(mealType),
                                config.minuteFor(mealType),
                                true,
                            ).show()
                        },
                        onEnabledChange = { checked ->
                            scope.launch {
                                settings.updateMealEnabled(mealType, checked)
                                scheduler.scheduleAll(config.withMealEnabled(mealType, checked))
                            }

                            if (
                                checked &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !hasNotificationPermission
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }

                ReminderOptionsCard(
                    themeMode = config.themeMode,
                    weekdaysOnly = config.weekdaysOnly,
                    onThemeModeChange = { themeMode ->
                        scope.launch {
                            settings.updateThemeMode(themeMode)
                        }
                    },
                    onWeekdaysOnlyChange = { checked ->
                        scope.launch {
                            settings.updateWeekdaysOnly(checked)
                            val updatedConfig = config.copy(weekdaysOnly = checked)
                            scheduler.scheduleAll(updatedConfig)
                        }
                    },
                )

                NotificationMessagesCard(
                    config = config,
                    onMessagesChange = { mealType, messages ->
                        scope.launch {
                            settings.updateMealMessages(mealType, messages)
                        }
                    },
                )

                NextReminderCard(
                    nextReminderText = DateUtils.formatNextReminder(config),
                    skipTodayEnabled = DateUtils.canSkipToday(config),
                    skipTodayText = DateUtils.skipTodayButtonText(config),
                    todaySkipped = config.enabled && DateUtils.isSkippedToday(config),
                    onSkipToday = {
                        scope.launch {
                            val skippedConfig = config.copy(
                                skippedDateEpochDay = DateUtils.todayEpochDay(),
                            )
                            settings.skipToday()
                            scheduler.scheduleAll(skippedConfig)
                        }
                    },
                )

                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !hasNotificationPermission
                ) {
                    PermissionCard(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { showAbout = true },
                ) {
                    Text("关于")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            sendTestAfterPermission = true
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            LunchNotification.show(context.applicationContext, MealType.LUNCH, config)
                            hasNotificationPermission = true
                        }
                    },
                ) {
                    Text("测试通知")
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🍱",
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = "三餐提醒",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "按时吃饭，保持健康",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTimeClick),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = mealType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                if (todaySkipped) {
                    Text(
                        text = "今日已跳过",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
    }
}

@Composable
private fun ReminderOptionsCard(
    themeMode: ThemeMode,
    weekdaysOnly: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "主题模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val selected = mode == themeMode
                        if (selected) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { onThemeModeChange(mode) },
                            ) {
                                Text(mode.label)
                            }
                        } else {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { onThemeModeChange(mode) },
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                }
            }

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
                        text = "仅工作日提醒",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "周六周日自动跳过",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = weekdaysOnly,
                    onCheckedChange = onWeekdaysOnlyChange,
                )
            }
        }
    }
}

@Composable
private fun NotificationMessagesCard(
    config: ReminderConfig,
    onMessagesChange: (MealType, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "通知文案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "每行一条，提醒时随机选择。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MealType.entries.forEach { mealType ->
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = messagesFor(config, mealType),
                    onValueChange = { value -> onMessagesChange(mealType, value) },
                    label = { Text("${mealType.reminderLabel}文案") },
                    minLines = 1,
                    maxLines = 3,
                )
            }
        }
    }
}

@Composable
private fun NextReminderCard(
    nextReminderText: String,
    skipTodayEnabled: Boolean,
    skipTodayText: String,
    todaySkipped: Boolean,
    onSkipToday: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "下次提醒",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            if (todaySkipped) {
                Text(
                    text = "今天已跳过全部提醒",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "下次提醒：$nextReminderText",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = nextReminderText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = skipTodayEnabled,
                onClick = onSkipToday,
            ) {
                Text(skipTodayText)
            }
        }
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PagePadding, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(onClick = onBack) {
                Text("返回")
            }
            Text(
                text = "LunchReminder",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CardCornerRadius),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("版本号：1.0")
                    Text("GitHub：https://github.com/xrhnb666-sketch/LunchReminder")
                    Text("开发说明：一个使用 Kotlin、Jetpack Compose、Material 3、DataStore、AlarmManager 和 NotificationManager 构建的三餐提醒 App。")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "允许通知后，午饭提醒才能显示出来。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = onRequestPermission) {
                Text("开启")
            }
        }
    }
}

private val PagePadding = 24.dp
private val CardCornerRadius = 24.dp

private fun messagesFor(config: ReminderConfig, mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> config.breakfastMessages
        MealType.LUNCH -> config.lunchMessages
        MealType.DINNER -> config.dinnerMessages
    }
}
