package com.applock.domain.repo

import com.applock.domain.model.Schedule
import com.applock.domain.model.ScheduleWithDates
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime


interface ScheduleRepo {
    fun getAllSchedules(): Flow<List<Schedule>>
    suspend fun createSchedule(schedule: Schedule)
    suspend fun updateActiveStatus(schedule: Schedule)
    suspend fun deleteSchedule(schedule: Schedule)
    suspend fun createDate(date: Schedule.DateInput): Long
    suspend fun createTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput): Long
    suspend fun updateTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput)
    suspend fun deleteTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput)
    suspend fun createBlockAppList(list: List<Schedule.DateInput.TimeSlotsInput.BlockedAppsInput>)
    fun getScheduleWithDates(scheduleId: Int): Flow<ScheduleWithDates>

    suspend fun isCurrentlyBlockedApp(
        packageName: String,
        date: LocalDate,
        time: LocalTime
    ): Boolean

    suspend fun hasActiveTimeSlotNow(date: LocalDate, time: LocalTime): Boolean
}