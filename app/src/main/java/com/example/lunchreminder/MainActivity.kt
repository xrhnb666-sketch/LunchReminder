package com.example.lunchreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LunchNotification.ensureChannel(applicationContext)

        setContent {
            val settings = remember { ReminderSettings(applicationContext) }
            val config by settings.configFlow.collectAsState(initial = ReminderConfig())
            var showSplash by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1000)
                showSplash = false
            }
            LaunchedEffect(config.notificationSound, config.customSoundUri) {
                if (
                    config.notificationSound == NotificationSound.CUSTOM &&
                    !NotificationSoundResolver.isCustomSoundUsable(
                        applicationContext,
                        config.customSoundUri,
                    )
                ) {
                    settings.updateNotificationSound(NotificationSound.DEFAULT)
                    settings.updateCustomSoundUri(null)
                    LunchNotification.recreateChannel(applicationContext, ReminderConfig())
                } else {
                    LunchNotification.ensureChannel(applicationContext, config)
                }
            }

            CuteTheme(themeMode = config.themeMode) {
                if (showSplash) {
                    FullScreenSplash()
                } else {
                    LunchReminderApp(
                        settings = settings,
                        config = config,
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F2)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
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
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

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
            CuteNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab -> currentTab = tab },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { bottomTabTransition() },
            label = "bottomTabTransition",
        ) { tab ->
            when (tab) {
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
                    onNotificationSoundChange = { notificationSound ->
                        scope.launch {
                            settings.updateNotificationSound(notificationSound)
                            LunchNotification.recreateChannel(
                                context.applicationContext,
                                config.copy(notificationSound = notificationSound),
                            )
                        }
                    },
                    onCustomSoundSelected = { customSoundUri ->
                        scope.launch {
                            settings.updateCustomSoundUri(customSoundUri)
                            LunchNotification.recreateChannel(
                                context.applicationContext,
                                config.copy(
                                    notificationSound = NotificationSound.CUSTOM,
                                    customSoundUri = customSoundUri,
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}
