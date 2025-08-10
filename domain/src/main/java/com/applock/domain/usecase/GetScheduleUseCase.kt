package com.applock.domain.usecase

import com.applock.domain.repo.ScheduleRepo
import javax.inject.Inject

class GetScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    operator fun invoke(scheduleId: Long) = scheduleRepo.getScheduleById(scheduleId)
}