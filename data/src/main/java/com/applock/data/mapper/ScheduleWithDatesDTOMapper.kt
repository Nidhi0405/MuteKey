package com.applock.data.mapper

import com.applock.data.localdb.entity.DateItemDTO
import com.applock.data.localdb.entity.ScheduleWithDatesDTO
import com.applock.data.localdb.entity.TimeSlotItemDTO
import com.applock.domain.model.ScheduleWithDates

fun ScheduleWithDatesDTO.toDomain(): ScheduleWithDates {
    return ScheduleWithDates(
        schedule = schedule.toDomain(),
        dates = dates.map { it.toDomain() }
    )
}

fun DateItemDTO.toDomain(): ScheduleWithDates.DateItem {
    return ScheduleWithDates.DateItem(
        date = date.toDomain(),
        timeSlots = timeSlots.map { it.toDomain() }
    )
}

fun TimeSlotItemDTO.toDomain(): ScheduleWithDates.DateItem.TimeSlotItem {
    return ScheduleWithDates.DateItem.TimeSlotItem(
        timeSlot = timeSlot.toDomain(),
        blockedApps = blockedApps.map { it.toDomain() }
    )
}

