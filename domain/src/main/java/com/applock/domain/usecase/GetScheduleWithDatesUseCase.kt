package com.applock.domain.usecase

import com.applock.domain.repo.ScheduleRepo
import javax.inject.Inject

class GetScheduleWithDatesUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    operator fun invoke(scheduleId: Int) = scheduleRepo.getScheduleWithDates(scheduleId)
}