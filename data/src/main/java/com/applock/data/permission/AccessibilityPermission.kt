package com.applock.data.permission

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.applock.core.logE
import com.applock.core.logV
import javax.inject.Inject

class AccessibilityPermission @Inject constructor() {
    fun hasPermission(context: Context, service: Class<*>): Boolean {
        var accessibilityEnabled = 0
        val service: String =
            context.packageName + "/" + service.name
        service.logE()
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            "accessibilityEnabled = $accessibilityEnabled".logV()
        } catch (e: Settings.SettingNotFoundException) {
            ("Error finding setting, default accessibility to not found: " + e.message).logE()
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            "Accessibility Is Enabled".logV()
            val settingValue: String = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                "AccessibilityService :: $accessibilityService == $service".logV()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    "accessibility is switched on!".logV()
                    return true
                }
            }
        } else {
            "accessibility is disabled".logV()
        }
        return false
    }
}