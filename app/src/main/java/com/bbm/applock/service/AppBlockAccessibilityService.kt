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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class AppBlockAccessibilityService : AccessibilityService() {
    private val info = AccessibilityServiceInfo()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.stackTraceToString().logE()
    }
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler)
    private var pendingPackageCheck: Job? = null
    private var lastProcessedPackageName: String? = null

    companion object {
        var instance: AppBlockAccessibilityService? = null
        private const val PACKAGE_CHECK_DELAY_MS = 180L
        val ignoredPackages = setOf(
            "com.android.systemui",
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.messaging",
            BuildConfig.APPLICATION_ID
        )
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
                if (packageName == lastProcessedPackageName) return

                pendingPackageCheck?.cancel()
                pendingPackageCheck = serviceScope.launch {
                    delay(PACKAGE_CHECK_DELAY_MS)
                    val rootPkg = rootInActiveWindow?.packageName?.toString().orEmpty()
                    if (packageName != rootPkg) return@launch

                    val isBlocked = withContext(Dispatchers.IO) {
                        isCurrentlyBlockedApp.invoke(packageName, LocalTime.now())
                    }
                    if (isBlocked) {
                        lastProcessedPackageName = packageName
                        withContext(Dispatchers.Main) {
                            openBlockScreen(packageName)
                        }
                    } else if (lastProcessedPackageName == packageName) {
                        lastProcessedPackageName = null
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
        lastProcessedPackageName = null
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
        pendingPackageCheck?.cancel()
        instance = null
    }

    override fun onDestroy() {
        "onDestroy".logE()
        pendingPackageCheck?.cancel()
        serviceScope.coroutineContext[Job]?.cancel()
        instance = null
        super.onDestroy()
    }
}

val String.isValidPackage: Boolean
    get() {
        if (this in AppBlockAccessibilityService.ignoredPackages) {
            return false
        }
        if (this.contains("launcher")) return false
        return true
    }
