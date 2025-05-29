package com.bbm.applock.util

fun formatUsageTime(ms: Long): String {
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60

    return when {
        hours > 0 -> "$hours hr $minutes min"
        minutes > 0 -> "$minutes min"
        else -> "<1 min"
    }
}