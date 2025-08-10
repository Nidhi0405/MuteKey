package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class CreateScheduleUseCase @Inject constructor(
    val repo: ScheduleRepo
) {
    suspend operator fun invoke(schedule: Schedule): Result<Unit> {
        val name = schedule.name
        val start = schedule.startTime
        val end = schedule.endTime
        if (repo.isScheduleExists(name, start, end)) {
            return Result.failure(Throwable("Schedule already exists"))
        }
//        if (scheduleName.isEmpty()) {
//            return Result.failure(Throwable("Schedule name cannot be empty"))
//        }
//        if (scheduleName.length !in 1..20) {
//            return Result.failure(Throwable("Schedule name must me 1 to 20 characters"))
//        }
        return result {
            repo.createSchedule(schedule = schedule)
        }
    }
}