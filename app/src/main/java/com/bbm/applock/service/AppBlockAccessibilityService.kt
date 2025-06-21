package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
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

class AppBlockAccessibilityService : AccessibilityService() {

    //var currentAppActivityList = mutableSetOf<String>()
    val mUsageStatsManager by lazy { getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager }

    private val info = AccessibilityServiceInfo()

    companion object {
        var instance: AppBlockAccessibilityService? = null
    }

    private val isCurrentlyBlockedApp: IsCurrentlyBlockedAppUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext, AppBlockAccessibilityModule::class.java
        ).isCurrentlyBlockedAppUseCase()
    }

    private var lastActionedPackage: String = ""
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {


            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString().orEmpty()
                if (!packageName.isValidPackage) return
                CoroutineScope(Dispatchers.IO).launch {
                    val isBlocked = isCurrentlyBlockedApp.invoke(
                        packageName,
                        LocalDate.now(),
                        LocalTime.now()
                    )
                    val time = System.currentTimeMillis()
                    val usageEvents = mUsageStatsManager.queryEvents(time - 800, time)
                    val usageEvent = UsageEvents.Event()
                    while (isBlocked && usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(usageEvent)
                        if (usageEvent.packageName == packageName) {
                            when {
                                usageEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                                    /*&& currentAppActivityList.isEmpty() */ -> {
                                    //currentAppActivityList.add(usageEvent.className)
                                    withContext(Dispatchers.Main) {
                                        openBlockScreen(packageName)
                                    }
                                    break
                                }

                                /*usageEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED -> {
                                    if (!currentAppActivityList.contains(usageEvent.className)) {
                                        currentAppActivityList.add(usageEvent.className)
                                        ("$currentAppActivityList-----List--added").logE()
                                    }
                                }

                                usageEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED -> {
                                    if (currentAppActivityList.contains(usageEvent.className)) {
                                        currentAppActivityList.remove(usageEvent.className)
                                        ("$currentAppActivityList-----List--remained").logE()
                                    }
                                }*/
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.stackTraceToString().logE()
        }
    }

    private fun openBlockScreen(packageName: String) {
        val intent = BlockScreenActivity.instance(this, packageName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
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

    override fun onServiceConnected() {
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        serviceInfo = info
        super.onServiceConnected()
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    val String.isValidPackage: Boolean
        get() {
            if (this in listOf(
                    "com.android.systemui",
                    "com.google.android.googlequicksearchbox"
                )
            ) return false
            if (this.contains("launcher")) return false
            if (this == this@AppBlockAccessibilityService.packageName) return false
            return true
        }
}