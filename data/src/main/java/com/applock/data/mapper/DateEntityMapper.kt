package com.applock.data.mapper

import com.applock.data.localdb.entity.DateEntity
import com.applock.domain.model.Schedule

fun Schedule.DateInput.toDateEntity(): DateEntity {
    return DateEntity(
        epochDate = date,
        scheduleId = scheduleId
    )
}

fun DateEntity.toDomain(): Schedule.DateInput {
    return Schedule.DateInput(
        date = epochDate,
        scheduleId = scheduleId
    )
}