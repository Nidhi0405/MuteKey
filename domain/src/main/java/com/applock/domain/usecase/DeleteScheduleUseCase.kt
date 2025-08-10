package com.applock.domain.usecase

import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor(
    private val repo: ScheduleRepo
) {
    suspend operator fun invoke(
        id: Long
    ): Result<Unit> {
        return result {
            repo.deleteSchedule(id)
        }
    }
}