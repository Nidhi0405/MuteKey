package com.applock.domain.usecase

import com.applock.domain.repo.ScheduleRepo
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class IsCurrentlyBlockedAppUseCase @Inject constructor(
    private val localAppRepo: ScheduleRepo
) {
    suspend operator fun invoke(
        packageName: String,
        time: LocalTime,
    ): Boolean {
        return localAppRepo.isAppRestrictedWithActiveSchedule(
            time,
            packageName,
        )
    }
}