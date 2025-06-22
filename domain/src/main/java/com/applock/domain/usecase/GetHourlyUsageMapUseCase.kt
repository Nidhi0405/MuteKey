package com.applock.domain.usecase

import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class GetHourlyUsageMapUseCase @Inject constructor(
    private val repo: SystemAppRepo
) {
    suspend operator fun invoke(days: Int): Map<String, List<Long>> {
        return repo.getHourlyAppUsageMapForToday(days)
    }
}