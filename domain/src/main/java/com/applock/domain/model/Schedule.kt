package com.applock.domain.model

import com.applock.domain.util.formate
import com.applock.domain.util.shortString
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Schedule(
    val id: Long = 0,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isActive: Boolean,
    val repeatDays: List<DayOfWeek>?,
    val apps: List<App> = emptyList(),
    val isOneTime: Boolean = false,
    val date: LocalDate? = null
) {
    data class App(
        val id: Long = 0,
        val appId: String,
        val appName: String
    )

    val startTimeFormat by lazy { startTime.formate }
    val endTimeFormat by lazy { endTime.formate }
    val repeatDaysFormat by lazy { repeatDays?.shortString }

    val onWhichDay by lazy {
        repeatDaysFormat.takeIf { !it.isNullOrEmpty() } ?: getOneTimeScheduleLabel(date, isActive)
    }

    private fun getOneTimeScheduleLabel(scheduledDate: LocalDate?, isActive: Boolean): String {
        if (scheduledDate == null) return ""
        return when (scheduledDate) {
            LocalDate.now() -> "Today"
            LocalDate.now().plusDays(1) -> "Tomorrow"
            else -> "On ${scheduledDate.format(DateTimeFormatter.ofPattern("dd MMM"))}"
        }
    }
}