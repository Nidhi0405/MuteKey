package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class UpdateScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    suspend operator fun invoke(
        schedule: Schedule,
    ): Result<Unit> {
        val name = schedule.name
        val start = schedule.startTime
        val end = schedule.endTime
        if (scheduleRepo.isScheduleExists(schedule.id, name, start, end)) {
            return Result.failure(Throwable("Schedule already exists"))
        }
        return result {
            scheduleRepo.updateSchedule(schedule)
        }
    }
}