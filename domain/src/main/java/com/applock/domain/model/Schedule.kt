package com.applock.domain.model

import java.time.LocalDate
import java.time.LocalTime


data class Schedule(
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = false
) {
    data class DateInput(
        val date: LocalDate,
        val scheduleId: Int, // foreign key
    ) {
        data class TimeSlotsInput(
            val id: Int = 0,
            val start: LocalTime,
            val end: LocalTime,
            val dateId: LocalDate, // foreign key
        ) {
            data class BlockedAppsInput(
                val id: Int,
                val name: String,
                val packageName: String,
                val timeSlotId: Int, // foreign key
            )
        }
    }
}

