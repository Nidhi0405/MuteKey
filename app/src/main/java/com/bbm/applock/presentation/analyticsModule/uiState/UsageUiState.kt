package com.bbm.applock.presentation.analyticsModule.uiState

import com.applock.domain.model.AppUsageInfo
import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm

data class UsageUiState(
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val chartApps: List<AppUsageInfo> = emptyList(),
    val totalHours: Long = 0,
    val totalMinutes: Long = 0,
    val selectedDateMillis: Long = 0L,
    val viewMode: CalendarViewMode = CalendarViewMode.DAY,
    val emptyMessage: String = ""
)

enum class CalendarViewMode {
    DAY, WEEK, MONTH
}