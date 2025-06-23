package com.bbm.applock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import com.applock.core.logE
import com.applock.domain.usecase.IsCurrentlyBlockedAppUseCase
import com.bbm.applock.R
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
                delay(300)
                val isBlocked = isCurrentlyBlockedApp.invoke(
                    packageName,
                    LocalDate.now(),
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

    private var lastActionedPackage: String = ""
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

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    private fun showOverlay(packageName: String) {
        if (overlayView != null) return // Prevent duplicate overlays

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.blocked_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Only this
            PixelFormat.RGBA_8888
        )

        windowManager.addView(overlayView, params)
        overlayView?.findViewById<TextView>(R.id.tv_close)?.setOnClickListener {
            overlayView?.let {
                windowManager.removeView(it)
                it.invalidate()
                overlayView = null
            }
            removeAppFromScreen()
        }
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
