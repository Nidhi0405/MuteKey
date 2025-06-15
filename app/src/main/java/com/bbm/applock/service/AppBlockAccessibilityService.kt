package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applock.core.logE
import com.applock.domain.usecase.IsCurrentlyBlockedAppUseCase
import com.bbm.applock.hiltmodule.AppBlockAccessibilityModule
import com.bbm.applock.presentation.BlockScreenActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime


/**
 * todo with schedule time conditions
 * */
class AppBlockAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AppBlockAccessibilityService? = null
    }

    private val isCurrentlyBlockedApp: IsCurrentlyBlockedAppUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext, AppBlockAccessibilityModule::class.java
        ).isCurrentlyBlockedAppUseCase()
    }

    @Volatile
    private var currentlyBlockedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString().orEmpty()
                "AppBlockAccessibilityService: check: $packageName".logE()

                CoroutineScope(Dispatchers.IO).launch {
                    val isBlocked = isCurrentlyBlockedApp.invoke(
                        packageName,
                        LocalDate.now(),
                        LocalTime.now()
                    )
                    "AppBlockAccessibilityService: check: $packageName -> $isBlocked".logE()
                    if (isBlocked) {
                        withContext(Dispatchers.Main) {
                            openBlockScreen(packageName)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.stackTraceToString().logE()
        }
    }

    private fun openBlockScreen(packageName: String) {
        val intent = BlockScreenActivity.getIntent(this, packageName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            /*            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        */
        }
        startActivity(intent)
    }

    fun removeAppFromScreen() {
        performGlobalAction(GLOBAL_ACTION_HOME) // Press home
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}