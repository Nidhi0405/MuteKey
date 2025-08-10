package com.applock.data.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import javax.inject.Inject

internal class NotificationPermission @Inject constructor() {
    fun hasPermission(appContext: Context): Boolean {
        return (ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun isNotificationReadServiceEnabled(context: Context): Boolean {
        with(context) {
            val pkgName = packageName
            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            return !flat.isNullOrEmpty() && flat.contains(pkgName)
        }
    }
}
