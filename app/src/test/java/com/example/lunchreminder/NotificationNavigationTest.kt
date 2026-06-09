package com.example.lunchreminder

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationNavigationTest {
    @Test
    fun notificationNavigation_usesSingleTopAndClearTopFlags() {
        assertTrue(NotificationNavigation.FLAGS and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertTrue(NotificationNavigation.FLAGS and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }

    @Test
    fun notificationNavigation_usesStableRequestCode() {
        assertEquals(2001, NotificationNavigation.REQUEST_CODE)
    }
}
