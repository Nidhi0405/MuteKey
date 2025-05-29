package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.bbm.applock.presentation.BlockScreenActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppBlockAccessibilityService : AccessibilityService() {
    private val blockedApps = listOf(
        "com.google.android.youtube",
        "com.whatsapp",
        "com.instagram.android"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (blockedApps.contains(packageName)) {
                val intent = Intent(this, BlockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onInterrupt() {}
}