package com.applock.data.repo

import com.applock.data.localdb.dao.ScheduleDao
import com.applock.data.mapper.toDomain
import com.applock.data.mapper.toScheduleEntity
import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepoImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepo {
    override suspend fun getAllSchedules(): Flow<List<Schedule>> {
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
}