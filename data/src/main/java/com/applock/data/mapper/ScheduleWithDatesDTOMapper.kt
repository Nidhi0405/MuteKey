package com.applock.data.mapper

import com.applock.data.localdb.entity.ScheduleEntity
import com.applock.data.localdb.entity.ScheduleWithApps
import com.applock.domain.model.Schedule


fun ScheduleEntity.toDomain() = Schedule(
    id = id,
    name = name,
    startTime = startTime,
    endTime = endTime,
    isActive = isActive,
    repeatDays = repeatDays
)

fun ScheduleWithApps.toDomain() = Schedule(
    id = schedule.id,
    name = schedule.name,
    startTime = schedule.startTime,
    endTime = schedule.endTime,
    isActive = schedule.isActive,
    repeatDays = schedule.repeatDays,
    isOneTime = schedule.isOneTime,
    apps = apps.map { app ->
        Schedule.App(
            id = app.id,
            appId = app.appId,
            appName = app.appName
        )
    },
    date = schedule.scheduledDate
)

fun Schedule.toEntity() = ScheduleEntity(
    id = id,
    name = name,
    startTime = startTime,
    endTime = endTime,
    isActive = isActive,
    repeatDays = repeatDays
)

