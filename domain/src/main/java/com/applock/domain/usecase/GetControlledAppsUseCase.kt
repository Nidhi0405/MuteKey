package com.applock.domain.usecase

import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import javax.inject.Inject

class GetControlledAppsUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo
) {
    suspend operator fun invoke(): List<AppUsageInfo> {
        return localAppRepo.getControlledApps()
    }
}