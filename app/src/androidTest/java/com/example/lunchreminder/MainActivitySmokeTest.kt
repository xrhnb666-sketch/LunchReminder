package com.example.lunchreminder

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    private lateinit var device: UiDevice

    @Before
    fun launchAppFromLauncherIntent() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        device = UiDevice.getInstance(instrumentation)
        runBlocking {
            context.reminderDataStore.edit { preferences ->
                preferences.clear()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.executeShellCommand(
                "pm grant $TARGET_PACKAGE ${Manifest.permission.POST_NOTIFICATIONS}",
            ).close()
        }
        device.wakeUp()
        device.executeShellCommand("wm dismiss-keyguard")
        device.pressHome()
        val launchIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        assertNotNull(
            "App package did not become visible",
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), TIMEOUT_MS),
        )
    }

    @After
    fun leaveApp() {
        device.pressHome()
    }

    @Test
    fun appLaunchesSplashEndsAndHomeIsVisibleWithoutOpeningFilePicker() {
        waitForTextContains("三餐提醒")

        assertEquals(TARGET_PACKAGE, device.currentPackageName)
        assertAbsentTextContains("Open from")
        assertVisibleTextContains("测试通知")
        assertVisibleTextContains("早餐")
        assertVisibleTextContains("午餐")
        assertVisibleTextContains("晚餐")
    }

    @Test
    fun bottomTabsOpenHomeHistoryStatsAndSettings() {
        waitForTextContains("三餐提醒")

        clickDescriptionContains("历史")
        assertVisibleTextContains("历史记录")

        clickDescriptionContains("统计")
        assertVisibleTextContains("统计分析")

        clickDescriptionContains("设置")
        assertVisibleTextContains("提醒设置")

        clickDescriptionContains("首页")
        assertVisibleTextContains("三餐提醒")
    }

    @Test
    fun settingsSoundEntryOpensSoundChoiceDialogOnlyAfterClick() {
        waitForTextContains("三餐提醒")
        clickDescriptionContains("设置")

        assertVisibleTextContains("提示音")
        assertVisibleTextContains("主题模式")
        assertVisibleTextContains("关于")
        assertAbsentTextContains("选择提示音")
        assertAbsentTextContains("Open from")

        clickTextContains("提示音")

        assertEquals(TARGET_PACKAGE, device.currentPackageName)
        assertVisibleTextContains("选择提示音")
        assertVisibleTextContains("默认铃声")
        assertVisibleTextContains("温柔铃声")
        assertVisibleTextContains("小熊铃声")
        assertVisibleTextContains("轻音乐")
        assertVisibleTextContains("自定义铃声")
    }

    @Test
    fun homeSwitchesCanToggleWithoutCrashing() {
        waitForTextContains("三餐提醒")

        clickDescriptionContains("早餐开关")
        clickDescriptionContains("午餐开关")
        clickDescriptionContains("晚餐开关")
        assertVisibleTextContains("今日跳过全部")

        clickDescriptionContains("今日跳过开关")
        assertVisibleTextContains("今天已跳过全部提醒")

        clickDescriptionContains("今日跳过开关")
        assertVisibleTextContains("下一次提醒")
    }

    @Test
    fun emptyHistoryAndStatsStatesAreVisible() {
        waitForTextContains("三餐提醒")

        clickDescriptionContains("历史")
        assertVisibleTextContains("今天还没有提醒记录哦")
        assertVisibleTextContains("记得按时吃饭")

        clickDescriptionContains("统计")
        assertVisibleTextContains("暂无统计数据")
        assertVisibleTextContains("继续坚持按时吃饭吧")
    }

    @Test
    fun notificationPermissionBranchDoesNotCrashStartup() {
        waitForTextContains("三餐提醒")

        assertEquals(TARGET_PACKAGE, device.currentPackageName)
        assertVisibleTextContains("测试通知")
    }

    private fun waitForTextContains(text: String) {
        assertNotNull(
            "Expected text not found: $text",
            device.wait(Until.findObject(By.textContains(text)), TIMEOUT_MS),
        )
    }

    private fun assertVisibleTextContains(text: String) {
        waitForTextContains(text)
    }

    private fun assertAbsentTextContains(text: String) {
        val node = device.wait(Until.findObject(By.textContains(text)), 500)
        assertEquals(null, node)
    }

    private fun clickTextContains(text: String) {
        waitForTextContains(text)
        device.findObject(By.textContains(text)).click()
    }

    private fun clickDescriptionContains(description: String) {
        assertNotNull(
            "Expected content description not found: $description",
            device.wait(Until.findObject(By.descContains(description)), TIMEOUT_MS),
        )
        device.findObject(By.descContains(description)).click()
    }

    private companion object {
        const val TARGET_PACKAGE = "com.example.lunchreminder"
        const val TIMEOUT_MS = 10_000L
    }
}
