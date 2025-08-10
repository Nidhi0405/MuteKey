package com.applock.domain.model

import com.applock.domain.util.formate
import com.applock.domain.util.shortString
import java.time.DayOfWeek
import java.time.LocalTime

data class Schedule(
    val id: Long = 0,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isActive: Boolean,
    val repeatDays: List<DayOfWeek>,
    val apps: List<App> = emptyList()
) {
    data class App(
        val id: Long = 0,
        val appId: String,
        val appName: String
    )

    val startTimeFormat by lazy { startTime.formate }
    val endTimeFormat by lazy { endTime.formate }
    val repeatDaysFormat by lazy { repeatDays.shortString }
}