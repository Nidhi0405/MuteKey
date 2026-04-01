package com.bbm.applock.presentation.analyticsModule.uiState

import java.time.YearMonth
import java.util.Calendar

data class CalendarUiState(
    val selectedDateMillis: Long?,
    val visibleMonth: YearMonth,
    val viewMode: CalendarViewMode
)