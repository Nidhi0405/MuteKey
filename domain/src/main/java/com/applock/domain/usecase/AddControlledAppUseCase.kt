package com.applock.domain.usecase

import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import javax.inject.Inject

class AddControlledAppUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo,
) {
    suspend operator fun invoke(app: AppUsageInfo) {
        localAppRepo.storeControlledApps(app)
    }
}