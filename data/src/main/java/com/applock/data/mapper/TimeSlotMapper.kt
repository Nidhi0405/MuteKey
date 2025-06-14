package com.applock.data.mapper

import com.applock.data.localdb.entity.TimeSlotEntity
import com.applock.domain.model.Schedule

fun TimeSlotEntity.toDomain(): Schedule.DateInput.TimeSlotsInput {
    return Schedule.DateInput.TimeSlotsInput(
        id = id,
        start = startTime,
        end = endTime,
        dateId = dateId
    )
}

fun Schedule.DateInput.TimeSlotsInput.toTimeSlotEntity(): TimeSlotEntity {
    return TimeSlotEntity(
        id = id,
        startTime = start,
        endTime = end,
        dateId = dateId
    )
}