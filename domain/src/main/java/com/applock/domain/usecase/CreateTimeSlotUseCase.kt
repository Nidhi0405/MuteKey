package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import com.applock.domain.util.result
import javax.inject.Inject

class CreateTimeSlotUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepo
) {
    suspend operator fun invoke(
        selectedDate: Schedule.DateInput,
        timeSlotsInput: Schedule.DateInput.TimeSlotsInput,
        list: List<Schedule.DateInput.TimeSlotsInput.BlockedAppsInput>
    ): Result<Unit> {
        return result {
            scheduleRepo.createDate(selectedDate)
            val timeSlotId =
                scheduleRepo.createTimeSlot(timeSlotsInput.copy(dateId = selectedDate.date))
            scheduleRepo.createBlockAppList(list.map { it.copy(timeSlotId = timeSlotId.toInt()) })
        }
    }
}