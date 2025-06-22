package com.applock.data.mapper

import com.applock.data.localdb.entity.BlockedAppEntity
import com.applock.domain.model.Schedule

fun BlockedAppEntity.toDomain(): Schedule.DateInput.TimeSlotsInput.BlockedAppsInput {
    return Schedule.DateInput.TimeSlotsInput.BlockedAppsInput(
        id = id,
        name = name,
        packageName = packageName,
        timeSlotId = timeSlotId
    )
}

fun Schedule.DateInput.TimeSlotsInput.BlockedAppsInput.toBlockedAppEntity(): BlockedAppEntity {
    return BlockedAppEntity(
        id = id,
        name = name,
        packageName = packageName,
        timeSlotId = timeSlotId
    )
}