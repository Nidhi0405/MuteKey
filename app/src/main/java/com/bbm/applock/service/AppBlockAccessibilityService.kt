package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applock.core.isValidPackage
import com.applock.core.logE
import com.applock.domain.usecase.IsCurrentlyBlockedAppUseCase
import com.bbm.applock.hiltmodule.AppBlockAccessibilityModule
import com.bbm.applock.presentation.BlockScreenActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime


fun throttleStringInput(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    delayMillis: Long = 1000L,
    action: (String) -> Unit
): (String) -> Unit {
    var job: Job? = null

    return { input ->
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            action.invoke(input)
        }
    }
}

class AppBlockAccessibilityService : AccessibilityService() {
    // var currentAppActivityList = mutableSetOf<String>()
    val mUsageStatsManager by lazy { getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager }

    private val info = AccessibilityServiceInfo()

    val throttledInput = throttleStringInput { packageName ->
        println("Action triggered with: $packageName")
        CoroutineScope(Dispatchers.IO).launch {
            val rootWindow = rootInActiveWindow
            val windowPkgName = rootWindow?.packageName?.toString().orEmpty()
            if (windowPkgName != packageName) return@launch
            val isBlocked = isCurrentlyBlockedApp.invoke(
                packageName,
                LocalDate.now(),
                LocalTime.now()
            )
            val time = System.currentTimeMillis()
            val usageEvents = mUsageStatsManager.queryEvents(time - 1500, time)
            val usageEvent = UsageEvents.Event()
            println("Action triggered with $packageName isBlocked: $isBlocked")
            while (isBlocked && usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(usageEvent)
                if (usageEvent.packageName == packageName) {
                    when (usageEvent.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> {
                            withContext(Dispatchers.Main) {
                                println("Action triggered with $packageName isBlocked: $isBlocked open")
                                openBlockScreen(packageName)
                            }
                            break
                        }

                        UsageEvents.Event.ACTIVITY_STOPPED -> {
                            _finishEvent.emit(Unit)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val _finishEvent = MutableSharedFlow<Unit>()
        val finishEvent: SharedFlow<Unit> = _finishEvent
        var instance: AppBlockAccessibilityService? = null
    }

    private val isCurrentlyBlockedApp: IsCurrentlyBlockedAppUseCase by lazy {
        EntryPointAccessors.fromApplication<AppBlockAccessibilityModule>(
            context = applicationContext,
            entryPoint = AppBlockAccessibilityModule::class.java
        ).isCurrentlyBlockedAppUseCase()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString().orEmpty()
                if (!packageName.isValidPackage(this)) return
                throttledInput.invoke(packageName)
            }
        } catch (e: Exception) {
            e.stackTraceToString().logE()
        }
    }

    private fun openBlockScreen(packageName: String) {
        val intent = BlockScreenActivity.instance(this, packageName).apply {
            //addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            //addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
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
}