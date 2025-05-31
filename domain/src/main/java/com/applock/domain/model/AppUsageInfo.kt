package com.applock.domain.model

data class AppUsageInfo(
    val name: String,
    val packageName: String,
    val usageTimeInMillis: Long, // TODO refactor to readable format
    val isControlledApp: Boolean = false
)