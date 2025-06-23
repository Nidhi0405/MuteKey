package com.applock.domain.usecase

import com.applock.domain.repo.ScheduleRepo
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class HasActiveTimeSlotsNow @Inject constructor(
    private val repo: ScheduleRepo
) {
    suspend operator fun invoke(scheduleId: Int, date: LocalDate, time: LocalTime): Boolean {
        return repo.hasActiveTimeSlotNow(scheduleId = scheduleId, date = date, time = time)
    }
}