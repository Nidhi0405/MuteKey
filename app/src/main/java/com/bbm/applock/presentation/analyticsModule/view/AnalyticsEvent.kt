package com.bbm.applock.presentation.analyticsModule.view

import com.bbm.applock.presentation.analyticsModule.vm.AnalyticsVm
import java.util.Calendar

sealed class AnalyticsEvent {
    data class OnDateSelected(val date: Calendar) : AnalyticsEvent()
    object OnPrevMonth : AnalyticsEvent()
    object OnNextMonth : AnalyticsEvent()
    data class OnViewModeChanged(val viewMode: AnalyticsVm.CalendarViewMode) : AnalyticsEvent()
}