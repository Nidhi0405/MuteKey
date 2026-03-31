package com.bbm.applock.presentation.analyticsModule.uiState

import java.util.Calendar

data class CalendarUiState(
    val selectedDateMillis: Long?,
    val visibleMonth: Calendar,
    val viewMode: CalendarViewMode
)