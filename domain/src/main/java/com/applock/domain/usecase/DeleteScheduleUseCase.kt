package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    val repo: ScheduleRepo
) {
    suspend operator fun invoke(schedule: Schedule): Result<Unit> {
        return try {
            repo.deleteSchedule(schedule)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}