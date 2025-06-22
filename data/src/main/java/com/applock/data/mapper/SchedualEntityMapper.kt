package com.applock.data.mapper

import com.applock.data.localdb.entity.ScheduleEntity
import com.applock.domain.model.Schedule

fun ScheduleEntity.toDomain(): Schedule {
    return Schedule(
        name = name,
        id = id,
        isActive = isActive
    )
}

fun Schedule.toScheduleEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        name = name,
        isActive = isActive
    )
}