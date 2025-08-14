package com.applock.core

import android.content.Context

fun String.isValidPackage(context: Context): Boolean {
    if (this == context.packageName) return false
    if (this in setOf(
            "com.android.systemui",
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.messaging"
        )
    ) {
        return false
    }
    if (this.contains("launcher")) {
        return false
    }
    if (contains("dialer")
        || contains("messaging")
        || contains(context.packageName)
    ) {
        return false
    }
    return true
}