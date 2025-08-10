package com.applock.data.repo

import com.applock.data.localdb.dao.ScheduleDao
import com.applock.data.localdb.entity.AppEntity
import com.applock.data.mapper.toDomain
import com.applock.data.mapper.toEntity
import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class ScheduleRepoImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepo {
    override fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getScheduleById(id: Long): Flow<Schedule?> {
        return scheduleDao.getScheduleById(id)
            .map { it?.toDomain() }
    }

    override suspend fun createSchedule(schedule: Schedule): Long {
        val scheduleEntity = schedule.toEntity()
        val scheduleId = scheduleDao.insertSchedule(scheduleEntity)

        val appEntities = schedule.apps.map {
            AppEntity(scheduleId = scheduleId, appId = it.appId, appName = it.appName)
        }
        scheduleDao.insertApps(appEntities)

        return scheduleId
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        val scheduleEntity = schedule.toEntity()
        scheduleDao.updateSchedule(scheduleEntity)

        // Replace apps
        scheduleDao.deleteAppsByScheduleId(schedule.id)
        val newAppEntities = schedule.apps.map {
            AppEntity(scheduleId = schedule.id, appId = it.appId, appName = it.appName)
        }
        scheduleDao.insertApps(newAppEntities)
    }

    override suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteSchedule(scheduleId)
    }

    override suspend fun isScheduleExists(
        scheduleName: String,
        startTime: LocalTime,
        endTime: LocalTime
    ): Boolean {
        return scheduleDao.isScheduleExists(scheduleName, startTime, endTime)
    }

    override suspend fun updateActiveStatus(scheduleId: Long, isActive: Boolean) {
        scheduleDao.updateScheduleActiveStatus(scheduleId, isActive)
    }

    override suspend fun isAppRestrictedWithActiveSchedule(
        currentTimeMillis: LocalTime,
        appPackage: String
    ): Boolean {
        val dayOfWeek = LocalDate
            .now()
            .atTime(currentTimeMillis)
            .dayOfWeek
            .getDisplayName(
                TextStyle.FULL,
                Locale.ENGLISH
            )
        return scheduleDao.isAppRestrictedNowWithActiveSchedule(
            currentTime = currentTimeMillis,
            appPackage = appPackage
        )?.repeatDays
            ?.any {
                it.toString().equals(dayOfWeek, ignoreCase = true)
            } == true
    }

    override suspend fun isScheduleActiveAndRunning(
        scheduleId: Long,
        currentTimeMillis: LocalTime,
    ): Boolean {
        val dayOfWeek = LocalDate
            .now()
            .atTime(currentTimeMillis)
            .dayOfWeek
            .getDisplayName(
                TextStyle.FULL,
                Locale.ENGLISH
            )
        return scheduleDao.getActiveAndRunningSchedule(
            scheduleId = scheduleId,
            currentTime = currentTimeMillis
        )
            ?.repeatDays
            ?.any {
                it.toString().equals(dayOfWeek, ignoreCase = true)
            } == true
    }
}