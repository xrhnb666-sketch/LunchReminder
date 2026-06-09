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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LunchNotification.ensureChannel(applicationContext)

        setContent {
            LunchReminderTheme {
                LunchReminderScreen()
            }
        }
    }
}

@Composable
private fun LunchReminderTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
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
private fun LunchReminderScreen() {
    val context = LocalContext.current
    val preferences = remember { ReminderPreferences(context.applicationContext) }
    val scheduler = remember { LunchAlarmScheduler(context.applicationContext) }
    val config by preferences.configFlow.collectAsState(initial = ReminderConfig())
    val scope = rememberCoroutineScope()

    var hasNotificationPermission by remember { mutableStateOf(LunchNotification.canPostNotifications(context)) }
    var sendTestAfterPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasNotificationPermission = granted
        if (granted && sendTestAfterPermission) {
            LunchNotification.show(context.applicationContext)
        }
        sendTestAfterPermission = false
    }

    LaunchedEffect(Unit) {
        hasNotificationPermission = LunchNotification.canPostNotifications(context)
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

                TimeCard(
                    timeText = formatTime(config.hour, config.minute),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                scope.launch {
                                    preferences.setTime(hour, minute)
                                    if (config.enabled) {
                                        scheduler.schedule(hour, minute)
                                    }
                                }
                            },
                            config.hour,
                            config.minute,
                            true,
                        ).show()
                    },
                )

                ReminderSwitchCard(
                    enabled = config.enabled,
                    onEnabledChange = { checked ->
                        scope.launch {
                            preferences.setEnabled(checked)
                            if (checked) {
                                scheduler.schedule(config.hour, config.minute)
                            } else {
                                scheduler.cancel()
                            }
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

                NextReminderCard(
                    nextReminderText = nextReminderText(config.hour, config.minute),
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

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        sendTestAfterPermission = true
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        LunchNotification.show(context.applicationContext)
                        hasNotificationPermission = true
                    }
                },
            ) {
                Text("测试通知")
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
            text = "午饭提醒",
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
private fun TimeCard(
    timeText: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "当前提醒时间",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ReminderSwitchCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "每日午饭提醒",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "开启后每天到点通知你",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private fun NextReminderCard(nextReminderText: String) {
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
            Text(
                text = nextReminderText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
            )
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
private val CardCornerRadius = 28.dp

private fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.CHINA, "%02d:%02d", hour, minute)
}

private fun nextReminderText(hour: Int, minute: Int): String {
    val now = LocalDateTime.now()
    val reminderTime = now
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)
        .let { time ->
            if (time.isAfter(now)) time else time.plusDays(1)
        }

    val dayText = when (reminderTime.toLocalDate()) {
        now.toLocalDate() -> "今天"
        now.toLocalDate().plusDays(1) -> "明天"
        else -> reminderTime.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))
    }

    return "$dayText ${formatTime(hour, minute)}"
}
