package com.example.lunchreminder

import android.content.Intent

object NotificationNavigation {
    const val REQUEST_CODE = 2001
    const val FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
}
