package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    val repo: ScheduleRepo
) {
    suspend operator fun invoke(schedule: Schedule): Result<Unit> {
        return result {
            repo.deleteSchedule(schedule)
        }
    }
}