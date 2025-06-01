package com.applock.domain.usecase

import com.applock.domain.repo.LocalAppRepo
import javax.inject.Inject

class IsCurrentlyBlockedAppUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo
) {
    suspend operator fun invoke(packageName: String): Boolean {
        return localAppRepo.isCurrentlyBlockedApp(packageName)
    }
}