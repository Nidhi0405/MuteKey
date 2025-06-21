package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class UpdateTimeSlotUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    suspend operator fun invoke(
        timeSlotsInput: Schedule.DateInput.TimeSlotsInput,
        blockedAppList: List<Schedule.DateInput.TimeSlotsInput.BlockedAppsInput>
    ): Result<Unit> {
        return result {
            scheduleRepo.updateTimeSlot(timeSlotsInput)
            scheduleRepo.deleteAppsWithTimeSlotId(timeSlotsInput.id)
            scheduleRepo.createBlockAppList(
                blockedAppList.map {
                    it.copy(timeSlotId = timeSlotsInput.id)
                }
            )
        }
    }
}