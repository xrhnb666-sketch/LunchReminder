package com.example.lunchreminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderSettingsModelTest {
    @Test
    fun reminderConfig_updatesMealTimesAndEnabledFlagsWithoutAffectingOtherMeals() {
        val config = ReminderConfig()
            .withMealEnabled(MealType.BREAKFAST, true)
            .withMealTime(MealType.BREAKFAST, 7, 15)
            .withMealEnabled(MealType.DINNER, true)
            .withMealTime(MealType.DINNER, 19, 30)

        assertTrue(config.breakfastEnabled)
        assertEquals(7, config.breakfastHour)
        assertEquals(15, config.breakfastMinute)
        assertFalse(config.lunchEnabled)
        assertTrue(config.dinnerEnabled)
        assertEquals(19, config.dinnerHour)
        assertEquals(30, config.dinnerMinute)
    }

    @Test
    fun reminderConfig_persistsReleaseSettingsInModel() {
        val skippedDate = LocalDate.of(2026, 6, 11)
        val config = ReminderConfig(
            breakfastEnabled = true,
            breakfastHour = 7,
            breakfastMinute = 10,
            lunchEnabled = true,
            lunchHour = 11,
            lunchMinute = 50,
            dinnerEnabled = true,
            dinnerHour = 18,
            dinnerMinute = 40,
            weekdaysOnly = true,
            skippedDateEpochDay = skippedDate.toEpochDay(),
            themeMode = ThemeMode.DARK,
            notificationSound = NotificationSound.CUSTOM,
            customSoundUri = "content://sounds/lunch.ogg",
        )

        assertTrue(config.enabled)
        assertTrue(config.weekdaysOnly)
        assertEquals(skippedDate.toEpochDay(), config.skippedDateEpochDay)
        assertEquals(ThemeMode.DARK, config.themeMode)
        assertEquals(NotificationSound.CUSTOM, config.notificationSound)
        assertEquals("content://sounds/lunch.ogg", config.customSoundUri)
    }

    @Test
    fun notificationSound_roundTripsStorageKeysAndFallsBackToDefault() {
        NotificationSound.entries.forEach { sound ->
            assertEquals(sound, NotificationSound.fromStorageKey(sound.storageKey))
        }

        assertEquals(NotificationSound.DEFAULT, NotificationSound.fromStorageKey(null))
        assertEquals(NotificationSound.DEFAULT, NotificationSound.fromStorageKey("missing"))
    }

    @Test
    fun notificationSound_bundledSoundsHaveResourcesAndCustomDoesNot() {
        assertTrue(NotificationSound.DEFAULT.rawResId != null)
        assertTrue(NotificationSound.GENTLE.rawResId != null)
        assertTrue(NotificationSound.BEAR.rawResId != null)
        assertTrue(NotificationSound.MUSIC.rawResId != null)
        assertEquals(null, NotificationSound.CUSTOM.rawResId)
    }
}
