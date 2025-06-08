package com.applock.domain.repo

import com.applock.domain.model.Schedule
import kotlinx.coroutines.flow.Flow


interface ScheduleRepo {
    fun getAllSchedules(): Flow<List<Schedule>>
    suspend fun createSchedule(schedule: Schedule)
    suspend fun updateActiveStatus(schedule: Schedule)
    suspend fun deleteSchedule(schedule: Schedule)
}