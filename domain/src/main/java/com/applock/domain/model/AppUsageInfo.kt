package com.applock.domain.model

data class AppUsageInfo(
    val name: String,
    val packageName: String,
    val usageTimeInMillis: Long,
    val isControlledApp: Boolean = false,
    val totalScreenTime: TotalScreenTime? = null
)

data class TotalScreenTime(
    var timeInMillis: Long
)