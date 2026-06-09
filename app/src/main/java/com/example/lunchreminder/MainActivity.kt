package com.example.lunchreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class AppTab(
    val label: String,
    val icon: String,
) {
    HOME("首页", "🏠"),
    HISTORY("历史", "🕘"),
    STATS("统计", "📊"),
    SETTINGS("设置", "⚙")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LunchNotification.ensureChannel(applicationContext)

        setContent {
            val settings = remember { ReminderSettings(applicationContext) }
            val config by settings.configFlow.collectAsState(initial = ReminderConfig())

            CuteTheme(themeMode = config.themeMode) {
                LunchReminderApp(
                    settings = settings,
                    config = config,
                )
            }
        }
    }
}

@Composable
private fun LunchReminderApp(
    settings: ReminderSettings,
    config: ReminderConfig,
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context.applicationContext) }
    val historyStore = remember { ReminderHistoryStore(context.applicationContext) }
    val statisticsRepository = remember { StatisticsRepository(historyStore) }
    val historyRecords by historyStore.historyFlow.collectAsState(initial = emptyList())
    val statisticsSummary by statisticsRepository.statisticsFlow.collectAsState(
        initial = StatisticsRepository.calculate(emptyList()),
    )
    val scope = rememberCoroutineScope()
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CuteColors.Background,
        bottomBar = {
            LunchReminderBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab -> currentTab = tab },
            )
        },
    ) { innerPadding ->
        when (currentTab) {
            AppTab.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                config = config,
                hasNotificationPermission = hasNotificationPermission,
                onMealTimeChange = { mealType, hour, minute ->
                    scope.launch {
                        settings.updateMealTime(mealType, hour, minute)
                        scheduler.scheduleAll(config.withMealTime(mealType, hour, minute))
                    }
                },
                onMealEnabledChange = { mealType, checked ->
                    scope.launch {
                        settings.updateMealEnabled(mealType, checked)
                        scheduler.scheduleAll(config.withMealEnabled(mealType, checked))
                    }
                },
                onSkipTodayChange = { checked ->
                    scope.launch {
                        if (checked) {
                            val skippedConfig = config.copy(
                                skippedDateEpochDay = DateUtils.todayEpochDay(),
                            )
                            settings.skipToday()
                            scheduler.scheduleAll(skippedConfig)
                        } else {
                            val restoredConfig = config.copy(skippedDateEpochDay = null)
                            settings.cancelSkipToday()
                            scheduler.scheduleAll(restoredConfig)
                        }
                    }
                },
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                onTestNotification = {
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
            )
            AppTab.HISTORY -> HistoryScreen(
                modifier = Modifier.padding(innerPadding),
                records = historyRecords,
                onClearHistory = {
                    scope.launch { historyStore.clear() }
                },
            )
            AppTab.STATS -> StatsScreen(
                modifier = Modifier.padding(innerPadding),
                summary = statisticsSummary,
                records = historyRecords,
            )
            AppTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                config = config,
                onThemeModeChange = { themeMode ->
                    scope.launch {
                        settings.updateThemeMode(themeMode)
                    }
                },
                onWeekdaysOnlyChange = { checked ->
                    scope.launch {
                        settings.updateWeekdaysOnly(checked)
                        scheduler.scheduleAll(config.copy(weekdaysOnly = checked))
                    }
                },
                onSkipTodayChange = { checked ->
                    scope.launch {
                        if (checked) {
                            val skippedConfig = config.copy(
                                skippedDateEpochDay = DateUtils.todayEpochDay(),
                            )
                            settings.skipToday()
                            scheduler.scheduleAll(skippedConfig)
                        } else {
                            val restoredConfig = config.copy(skippedDateEpochDay = null)
                            settings.cancelSkipToday()
                            scheduler.scheduleAll(restoredConfig)
                        }
                    }
                },
                onMessagesChange = { mealType, messages ->
                    scope.launch {
                        settings.updateMealMessages(mealType, messages)
                    }
                },
            )
        }
    }
}

@Composable
private fun LunchReminderBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    NavigationBar(
        containerColor = CuteColors.NavBackground,
        tonalElevation = 8.dp,
    ) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { CuteTabIcon(icon = tab.icon, selected = currentTab == tab) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CuteColors.Orange,
                    selectedTextColor = CuteColors.Orange,
                    indicatorColor = CuteColors.Lunch,
                    unselectedIconColor = CuteColors.TextSecondary,
                    unselectedTextColor = CuteColors.TextSecondary,
                ),
            )
        }
    }
}
