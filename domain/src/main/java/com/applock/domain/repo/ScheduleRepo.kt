package com.applock.domain.repo

import com.applock.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime


interface ScheduleRepo {
    fun getAllSchedules(): Flow<List<Schedule>>
    fun getScheduleById(id: Long): Flow<Schedule?>
    suspend fun createSchedule(schedule: Schedule): Long
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(scheduleId: Long)
    suspend fun isScheduleExists(
        scheduleName: String,
        startTime: LocalTime,
        endTime: LocalTime
    ): Boolean

    suspend fun updateActiveStatus(
        scheduleId: Long,
        isActive: Boolean
    )

    suspend fun isAppRestrictedWithActiveSchedule(
        currentTimeMillis: LocalTime,
        appPackage: String
    ): Boolean

    suspend fun isScheduleActiveAndRunning(
        scheduleId: Long,
        currentTimeMillis: LocalTime,
    ): Boolean
}