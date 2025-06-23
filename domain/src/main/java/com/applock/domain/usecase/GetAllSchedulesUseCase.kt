package com.applock.domain.usecase

import com.applock.domain.model.Schedule
import com.applock.domain.repo.ScheduleRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSchedulesUseCase @Inject constructor(
    private val repo: ScheduleRepo
) {
    operator fun invoke(): Flow<List<Schedule>> {
        return repo.getAllSchedules()
    }
}