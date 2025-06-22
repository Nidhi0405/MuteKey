package com.applock.domain.model

data class ScheduleWithDates(
    val schedule: Schedule,
    val dates: List<DateItem>
) {
    data class DateItem(
        val date: Schedule.DateInput,
        val timeSlots: List<TimeSlotItem>
    ) {
        data class TimeSlotItem(
            val timeSlot: Schedule.DateInput.TimeSlotsInput,
            val blockedApps: List<Schedule.DateInput.TimeSlotsInput.BlockedAppsInput>
        )
    }
}