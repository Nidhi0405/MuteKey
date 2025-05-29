package com.applock.data.permission

import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.os.Process
import androidx.core.app.AppOpsManagerCompat.MODE_ALLOWED
import javax.inject.Inject

class UsagePermission @Inject constructor() {
    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == MODE_ALLOWED
    }
}