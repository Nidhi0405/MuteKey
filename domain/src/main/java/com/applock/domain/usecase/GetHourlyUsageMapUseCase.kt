package com.applock.domain.usecase

import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class GetHourlyUsageMapUseCase @Inject constructor(
    private val repo: SystemAppRepo
) {
    suspend operator fun invoke(startTime: Long, endTime: Long): Map<String, List<Long>> {
        return repo.getHourlyAppUsageMapForToday(startTime, endTime)
    }
}