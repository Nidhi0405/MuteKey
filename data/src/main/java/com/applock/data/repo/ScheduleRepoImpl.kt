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
import java.time.LocalDate
import java.time.LocalTime
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
        return scheduleDao.getDateIdIfExists(date.date, date.scheduleId)?.toLong()
            ?: scheduleDao.createDateEntry(date.toDateEntity())
    }

    override suspend fun createTimeSlot(timeSlotsInput: Schedule.DateInput.TimeSlotsInput): Long {
        return scheduleDao.getTimeSlotIdIfExists(
            timeSlotsInput.start, timeSlotsInput.end, timeSlotsInput.dateId
        )?.toLong() ?: scheduleDao.createTimeSlot(timeSlotsInput.toTimeSlotEntity())
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

    override suspend fun deleteAppsWithTimeSlotId(id: Int) {
        scheduleDao.deleteAppsWithTimeSlotId(id)
    }

    override fun getScheduleWithDates(sheduleId: Int): Flow<ScheduleWithDates> {
        return scheduleDao.getScheduleWithDates(sheduleId).map { it.toDomain() }
    }

    override suspend fun isCurrentlyBlockedApp(
        packageName: String, date: LocalDate, time: LocalTime
    ): Boolean {
        return scheduleDao.isPackageBlocked(packageName, date, time)
    }

    override suspend fun hasActiveTimeSlotNow(date: LocalDate, time: LocalTime): Boolean {
        return scheduleDao.hasActiveTimeSlotNow(date, time)
    }
}