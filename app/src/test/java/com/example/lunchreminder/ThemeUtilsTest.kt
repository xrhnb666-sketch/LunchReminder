package com.example.lunchreminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeUtilsTest {
    @Test
    fun shouldUseDarkTheme_followsSystemWhenSystemModeSelected() {
        assertTrue(
            ThemeUtils.shouldUseDarkTheme(
                themeMode = ThemeMode.SYSTEM,
                systemInDarkTheme = true,
            ),
        )
        assertFalse(
            ThemeUtils.shouldUseDarkTheme(
                themeMode = ThemeMode.SYSTEM,
                systemInDarkTheme = false,
            ),
        )
    }

    @Test
    fun shouldUseDarkTheme_usesExplicitLightMode() {
        assertFalse(
            ThemeUtils.shouldUseDarkTheme(
                themeMode = ThemeMode.LIGHT,
                systemInDarkTheme = true,
            ),
        )
    }

    @Test
    fun shouldUseDarkTheme_usesExplicitDarkMode() {
        assertTrue(
            ThemeUtils.shouldUseDarkTheme(
                themeMode = ThemeMode.DARK,
                systemInDarkTheme = false,
            ),
        )
    }

    @Test
    fun supportsDynamicColor_isOnlyTrueFromAndroid12() {
        assertFalse(ThemeUtils.supportsDynamicColor(30))
        assertTrue(ThemeUtils.supportsDynamicColor(31))
    }
}
