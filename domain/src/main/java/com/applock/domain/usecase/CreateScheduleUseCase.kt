package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import javax.inject.Inject

class CreateScheduleUseCase @Inject constructor(
    val repo: ScheduleRepo
) {
    suspend operator fun invoke(scheduleName: String): Result<Unit> {
        if (scheduleName.isEmpty()) {
            return Result.failure(Throwable("Schedule name cannot be empty"))
        }
        if (scheduleName.length !in 1..20) {
            return Result.failure(Throwable("Schedule name must me 1 to 20 characters"))
        }
        return try {
            repo.createSchedule(Schedule(name = scheduleName))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}