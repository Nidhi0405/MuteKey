package com.applock.data.repo

import com.applock.data.localdb.dao.ScheduleDao
import com.applock.data.mapper.toBlockedAppEntity
import com.applock.data.mapper.toDateEntity
import com.applock.data.mapper.toDomain
import com.applock.data.mapper.toScheduleEntity
import com.applock.data.mapper.toTimeSlotEntity
import com.applock.domain.model.Schedule
import com.applock.domain.model.ScheduleWithDates
import com.applock.domain.repo.ScheduleRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepoImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepo {
    override fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules().map { list ->
            list.map { item ->
                item.toDomain()
            }
        }
    }

    override suspend fun createSchedule(schedule: Schedule) {
        scheduleDao.createSchedule(schedule = schedule.toScheduleEntity())
    }

    override suspend fun updateActiveStatus(schedule: Schedule) {
        scheduleDao.updateActiveStatus(scheduleId = schedule.id, isActive = schedule.isActive)
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toScheduleEntity())
    }

    override suspend fun createDate(date: Schedule.DateInput): Long {
        return scheduleDao.createDateEntry(date.toDateEntity())
    }

    override suspend fun createTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput): Long {
        return scheduleDao.createTimeSlot(timeSlotsInput.toTimeSlotEntity())
    }

    override suspend fun updateTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput) {
        scheduleDao.updateTimeSlot(timeSlotsInput.toTimeSlotEntity())
    }

    override suspend fun deleteTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput) {
        scheduleDao.deleteTimeSlot(timeSlotsInput.toTimeSlotEntity())
    }

    override suspend fun createBlockAppList(list: List<Schedule.DateInput.TimeSlotsInput.BlockedAppsInput>) {
        scheduleDao.createBlockAppList(list.map { it.toBlockedAppEntity() })
    }

    override fun getScheduleWithDates(sheduleId: Int): Flow<ScheduleWithDates> {
        return scheduleDao.getScheduleWithDates(sheduleId).map { it.toDomain() }
    }
}