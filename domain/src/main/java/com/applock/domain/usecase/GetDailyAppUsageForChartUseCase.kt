package com.applock.domain.usecase

import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class GetDailyAppUsageForChartUseCase @Inject constructor(
    private val systemAppRepo: SystemAppRepo
) {
    suspend operator fun invoke(days: Int): Map<String, List<Long>> {
        return systemAppRepo.getInstalledAppsWithUsagesInMap(days)
    }
}