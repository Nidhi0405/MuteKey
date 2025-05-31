package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applock.core.logE
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.hiltmodule.AppBlockAccessibilityModule
import com.bbm.applock.presentation.BlockScreenActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * todo with schedule time conditions
 * */
class AppBlockAccessibilityService : AccessibilityService() {
    private val useCase: SyncInstalledAppsUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            AppBlockAccessibilityModule::class.java
        ).syncInstalledAppsUseCase()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    if (useCase.invoke(7)
                            .any { it.packageName == packageName && it.isControlledApp }
                    ) {
                        withContext(Dispatchers.Main) {
                            openBlockScreen()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.stackTraceToString().logE()
        }
    }

    private fun openBlockScreen() {
        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onInterrupt() {}
}