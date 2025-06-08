package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import javax.inject.Inject

class ToggleScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    suspend operator fun invoke(schedule: Schedule): Result<Unit> {
        return try {
            scheduleRepo.updateActiveStatus(schedule.copy(isActive = !schedule.isActive))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}