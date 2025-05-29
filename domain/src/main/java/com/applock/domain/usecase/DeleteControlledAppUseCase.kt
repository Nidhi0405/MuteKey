package com.applock.domain.usecase

import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import javax.inject.Inject

class DeleteControlledAppUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo,
) {
    suspend operator fun invoke(app: AppUsageInfo) {
        localAppRepo.deleteControlledApp(app)
    }
}