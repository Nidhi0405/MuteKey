package com.bbm.applock.presentation.analyticsModule.view

import java.util.Calendar

sealed class CalendarEvent {
    data class OnDateSelected(val date: Calendar) : CalendarEvent()
    data object OnPrevMonth : CalendarEvent()
    data object OnNextMonth : CalendarEvent()
}