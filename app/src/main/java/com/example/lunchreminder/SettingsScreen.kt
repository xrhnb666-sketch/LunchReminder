package com.example.lunchreminder

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class SettingsPage {
    MAIN,
    MESSAGES,
    THEME,
}

@Composable
fun SettingsScreen(
    config: ReminderConfig,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
    onSkipTodayChange: (Boolean) -> Unit,
    onMessagesChange: (MealType, String) -> Unit,
    onNotificationSoundChange: (NotificationSound) -> Unit,
    onCustomSoundSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val customSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onCustomSoundSelected(uri.toString())
        }
    }

    when (page) {
        SettingsPage.MAIN -> SettingsMainScreen(
            modifier = modifier,
            config = config,
            onWeekdaysOnlyChange = onWeekdaysOnlyChange,
            onSkipTodayChange = onSkipTodayChange,
            onMessagesClick = { page = SettingsPage.MESSAGES },
            onThemeClick = { page = SettingsPage.THEME },
            onCheckUpdateClick = { showUpdateDialog = true },
            onShareClick = { context.shareLunchReminder() },
            onNotificationSoundChange = onNotificationSoundChange,
            onChooseCustomSound = {
                customSoundLauncher.launch(
                    arrayOf(
                        "audio/mpeg",
                        "audio/wav",
                        "audio/x-wav",
                        "audio/mp4",
                        "audio/ogg",
                    ),
                )
            },
        )
        SettingsPage.MESSAGES -> NotificationMessagesScreen(
            modifier = modifier,
            config = config,
            onBack = { page = SettingsPage.MAIN },
            onMessagesChange = onMessagesChange,
        )
        SettingsPage.THEME -> ThemeModeScreen(
            modifier = modifier,
            themeMode = config.themeMode,
            onBack = { page = SettingsPage.MAIN },
            onThemeModeChange = onThemeModeChange,
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("检查更新") },
            text = {
                Text(
                    text = "当前版本：1.0.0\n已是最新版本",
                    color = CuteColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("知道了", color = CuteColors.Orange)
                }
            },
        )
    }
}

@Composable
private fun SettingsMainScreen(
    config: ReminderConfig,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
    onSkipTodayChange: (Boolean) -> Unit,
    onMessagesClick: () -> Unit,
    onThemeClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onShareClick: () -> Unit,
    onNotificationSoundChange: (NotificationSound) -> Unit,
    onChooseCustomSound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showSoundDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UiAssetBackgroundSlot()
            SettingsBackgroundDecor()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CuteDimens.PagePadding)
                    .padding(top = 24.dp, bottom = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    CutePageTitle(title = "设置")
                    Image(
                        painter = UiAssets.painter(UiAssets.plant),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                    )
                }
                SettingGroup(title = "提醒设置", iconRes = UiAssets.breakfast) {
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
                    SettingNavRow(
                        title = "通知文案设置",
                        subtitle = "自定义三餐提醒文案",
                        iconRes = UiAssets.skipCloud,
                        onClick = onMessagesClick,
                    )
                    CuteDividerLine()
                    SettingNavRow(
                        title = "🔔 提示音",
                        subtitle = "当前：${NotificationSoundResolver.displayName(context, config)}",
                        iconRes = UiAssets.stars,
                        onClick = { showSoundDialog = true },
                    )
                }
                SettingGroup(title = "外观设置", iconRes = UiAssets.plant) {
                    SettingNavRow(
                        title = "主题模式",
                        subtitle = config.themeMode.label,
                        iconRes = UiAssets.stars,
                        onClick = onThemeClick,
                    )
                }
                SettingGroup(title = "关于", iconRes = UiAssets.plant) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = UiAssets.painter(UiAssets.appIcon),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "LunchReminder",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CuteColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "版本号：${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CuteColors.TextSecondary,
                            )
                        }
                    }
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "App名称",
                        subtitle = "LunchReminder",
                    )
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "版本号",
                        subtitle = BuildConfig.VERSION_NAME,
                    )
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "GitHub地址",
                        subtitle = "github.com/xrhnb666-sketch/LunchReminder",
                    )
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "开发者信息",
                        subtitle = "LunchReminder Team",
                    )
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "开发说明",
                        subtitle = "用于帮助用户按时吃早餐、午餐和晚餐。",
                    )
                    CuteDividerLine()
                    AboutInfoRow(
                        title = "隐私政策",
                        subtitle = "本应用不收集任何个人信息。所有提醒数据仅保存在本地设备。",
                    )
                    CuteDividerLine()
                    SettingNavRow(
                        title = "检查更新",
                        subtitle = "当前已是最新版本",
                        iconRes = UiAssets.stars,
                        onClick = onCheckUpdateClick,
                    )
                    CuteDividerLine()
                    SettingNavRow(
                        title = "分享应用",
                        subtitle = "把按时吃饭的小提醒分享给朋友",
                        iconRes = UiAssets.skipCloud,
                        onClick = onShareClick,
                    )
                    CuteDividerLine()
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "Made with ❤️ by LunchReminder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CuteColors.TextSecondary,
                    )
                }
            }
        }
    }

    if (showSoundDialog) {
        NotificationSoundDialog(
            selectedSound = config.notificationSound,
            onDismiss = { showSoundDialog = false },
            onSoundSelected = { sound ->
                onNotificationSoundChange(sound)
                showSoundDialog = false
            },
            onChooseCustomSound = {
                showSoundDialog = false
                onChooseCustomSound()
            },
        )
    }
}

private fun android.content.Context.shareLunchReminder() {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "LunchReminder：按时吃饭，照顾自己。")
    }
    startActivity(Intent.createChooser(shareIntent, "分享 LunchReminder"))
}

@Composable
private fun AboutInfoRow(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

@Composable
private fun NotificationSoundDialog(
    selectedSound: NotificationSound,
    onDismiss: () -> Unit,
    onSoundSelected: (NotificationSound) -> Unit,
    onChooseCustomSound: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择提示音",
                color = CuteColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NotificationSound.entries.filter { it != NotificationSound.CUSTOM }.forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSoundSelected(sound) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = sound.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = CuteColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (sound == selectedSound) "当前提示音" else "点击更换",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CuteColors.TextSecondary,
                            )
                        }
                        if (sound == selectedSound) {
                            Text(
                                text = "●",
                                style = MaterialTheme.typography.titleMedium,
                                color = CuteColors.Orange,
                            )
                        }
                    }
                }
                CuteDividerLine()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChooseCustomSound)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "🔔 自定义铃声",
                            style = MaterialTheme.typography.titleMedium,
                            color = CuteColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (selectedSound == NotificationSound.CUSTOM) {
                                "当前自定义铃声，点击更换"
                            } else {
                                "选择手机本地音频文件"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = CuteColors.TextSecondary,
                        )
                    }
                    CuteArrow()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = CuteColors.Orange)
            }
        },
    )
}

@Composable
private fun NotificationMessagesScreen(
    config: ReminderConfig,
    onBack: () -> Unit,
    onMessagesChange: (MealType, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UiAssetBackgroundSlot()
            SettingsBackgroundDecor(showPlant = false)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CuteDimens.PagePadding)
                    .padding(top = 24.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing),
                ) {
                    BackTitle(title = "通知文案设置", onBack = onBack)
                    CuteCard(containerColor = CuteColors.Card) {
                        Column(
                            modifier = Modifier.padding(CuteDimens.CardPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            MealMessageField(
                                label = "早餐文案",
                                mealType = MealType.BREAKFAST,
                                value = config.breakfastMessages,
                                onValueChange = { onMessagesChange(MealType.BREAKFAST, it) },
                            )
                            MealMessageField(
                                label = "午餐文案",
                                mealType = MealType.LUNCH,
                                value = config.lunchMessages,
                                onValueChange = { onMessagesChange(MealType.LUNCH, it) },
                            )
                            MealMessageField(
                                label = "晚餐文案",
                                mealType = MealType.DINNER,
                                value = config.dinnerMessages,
                                onValueChange = { onMessagesChange(MealType.DINNER, it) },
                            )
                        }
                    }
                }
                CuteGradientButton(
                    text = "恢复默认文案",
                    onClick = {
                        onMessagesChange(MealType.BREAKFAST, ReminderConfig.DEFAULT_BREAKFAST_MESSAGES)
                        onMessagesChange(MealType.LUNCH, ReminderConfig.DEFAULT_LUNCH_MESSAGES)
                        onMessagesChange(MealType.DINNER, ReminderConfig.DEFAULT_DINNER_MESSAGES)
                    },
                )
            }
        }
    }
}

@Composable
private fun ThemeModeScreen(
    themeMode: ThemeMode,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = CuteColors.Background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UiAssetBackgroundSlot()
            SettingsBackgroundDecor(showPlant = false, cloudHeight = 150)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CuteDimens.PagePadding)
                    .padding(top = 24.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(CuteDimens.CardSpacing)) {
                    BackTitle(title = "主题模式", onBack = onBack)
                    ThemeMode.entries.forEach { mode ->
                        CuteCard(containerColor = CuteColors.Card) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onThemeModeChange(mode) }
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CuteColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (mode == themeMode) {
                                    Text(
                                        text = "●",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CuteColors.Orange,
                                    )
                                }
                            }
                        }
                    }
                }
                Image(
                    painter = UiAssets.painter(UiAssets.bear),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                        .size(180.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SettingsBackgroundDecor(
    showPlant: Boolean = true,
    cloudHeight: Int = 130,
) {
    Image(
        painter = UiAssets.painter(UiAssets.cloudBackground),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(cloudHeight.dp)
            .alpha(0.10f),
    )
    Image(
        painter = UiAssets.painter(UiAssets.smallStar),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 18.dp, top = 34.dp)
            .size(14.dp)
            .alpha(0.08f),
    )
    Image(
        painter = UiAssets.painter(UiAssets.smallStar),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 74.dp, top = 78.dp)
            .size(10.dp)
            .alpha(0.06f),
    )
    Image(
        painter = UiAssets.painter(UiAssets.smallStar),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 20.dp)
            .size(12.dp)
            .alpha(0.05f),
    )
    if (showPlant) {
        Image(
            painter = UiAssets.painter(UiAssets.plant),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 22.dp)
                .size(52.dp)
                .alpha(0.90f),
        )
    }
}

@Composable
private fun BackTitle(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.clickable(onClick = onBack),
            text = "‹",
            style = MaterialTheme.typography.headlineMedium,
            color = CuteColors.TextPrimary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = CuteColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = UiAssets.painter(UiAssets.stars),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun MealMessageField(
    label: String,
    mealType: MealType,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CuteColors.TextSecondary,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            minLines = 1,
            maxLines = 3,
            leadingIcon = {
                Image(
                    painter = UiAssets.painter(mealIconRes(mealType)),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            },
        )
    }
}
