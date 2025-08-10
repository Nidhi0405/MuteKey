package com.bbm.applock.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.applock.domain.usecase.IsCurrentlyBlockedAppUseCase
import com.bbm.applock.hiltmodule.AppBlockAccessibilityModule
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

class MyNotificationListenerService : NotificationListenerService() {
    private val isCurrentlyBlockedApp: IsCurrentlyBlockedAppUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext, AppBlockAccessibilityModule::class.java
        ).isCurrentlyBlockedAppUseCase()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.isValidPackage) return
        CoroutineScope(Dispatchers.IO).launch {
            if (isCurrentlyBlockedApp.invoke(sbn.packageName, LocalTime.now())) {
                cancelNotification(sbn.key)
            }
        }
    }
}
