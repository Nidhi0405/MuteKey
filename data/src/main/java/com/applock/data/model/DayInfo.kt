package com.applock.data.model

data class DayInfo(
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val monthYearTodayTriple: Triple<Int, Int, Boolean>,
    val offsetFromToday: Int
)
