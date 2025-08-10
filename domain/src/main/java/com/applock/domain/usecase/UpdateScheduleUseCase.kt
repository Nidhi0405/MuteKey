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
        return result {
            scheduleRepo.updateSchedule(schedule)
        }
    }
}