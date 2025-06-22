package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class DeleteTimeSlotUseCase @Inject constructor(
    private val repo: ScheduleRepo
) {
    suspend operator fun invoke(
        timeSlotsInput: Schedule.DateInput.TimeSlotsInput
    ): Result<Unit> {
        return result {
            repo.deleteTimeSlot(timeSlotsInput)
        }
    }
}