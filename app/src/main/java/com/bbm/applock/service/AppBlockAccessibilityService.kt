package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.applock.core.logE
import com.applock.domain.usecase.IsCurrentlyBlockedAppUseCase
import com.bbm.applock.BuildConfig
import com.bbm.applock.hiltmodule.AppBlockAccessibilityModule
import com.bbm.applock.presentation.BlockScreenActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

class AppBlockAccessibilityService : AccessibilityService() {
    private val info = AccessibilityServiceInfo()
    private val throttledInput = throttleStringInput { packageName ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(100)
//                delay(300)
                val rootPkg = rootInActiveWindow?.packageName?.toString().orEmpty()
                if (packageName != rootPkg) return@launch
                val isBlocked = isCurrentlyBlockedApp.invoke(
                    packageName,
                    LocalTime.now()
                )
                if (isBlocked) {
                    withContext(Dispatchers.Main) {
                        openBlockScreen(packageName)
                    }
                }
            } catch (e: Exception) {
                e.stackTraceToString().logE()
            }
        }
    }

    companion object {
        var instance: AppBlockAccessibilityService? = null
    }

    private val isCurrentlyBlockedApp: IsCurrentlyBlockedAppUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext, AppBlockAccessibilityModule::class.java
        ).isCurrentlyBlockedAppUseCase()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val rootWindow = rootInActiveWindow
                val packageName = rootWindow?.packageName?.toString().orEmpty()

                if (!packageName.isValidPackage) return

                throttledInput.invoke(packageName)
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
        "onCreate".logE()
        instance = this
        super.onCreate()
    }

    override fun onServiceConnected() {
        "onServiceConnected".logE()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        serviceInfo = info
        super.onServiceConnected()
    }

    override fun onInterrupt() {
        "onInterrupt".logE()
        instance = null
    }

    override fun onDestroy() {
        "onDestroy".logE()
        instance = null
        super.onDestroy()
    }


}

val String.isValidPackage: Boolean
    get() {
        if (this in listOf(
                "com.android.systemui",
                "com.google.android.googlequicksearchbox"
            )
        ) return false
        if (this.contains("launcher")) return false
        if (this == BuildConfig.APPLICATION_ID) return false
        return true
    }

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
