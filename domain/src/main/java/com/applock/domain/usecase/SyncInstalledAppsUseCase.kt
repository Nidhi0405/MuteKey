package com.applock.domain.usecase

import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class SyncInstalledAppsUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo, // db operations
    private val systemAppRepo: SystemAppRepo, // context operations
) {
    suspend operator fun invoke(days: Int): List<AppUsageInfo> {
        val sysApps = systemAppRepo.getInstalledApp()

        val localApps = localAppRepo.getStoredApp()

        val sysInstalledPackages = sysApps.map { it.packageName }.toSet()
        val tobeDeleted = localApps.filter { it.packageName !in sysInstalledPackages }

        if (tobeDeleted.isNotEmpty()) {
            localAppRepo.deleteUninstalledApps(tobeDeleted)
        }

        localAppRepo.storeInstalledApps(sysApps)

        val appsWithUsage = systemAppRepo.getInstalledAppsWithUsage(days)

        val controlledAppPackages = localApps
            .asSequence()
            .filter { it.isControlledApp }
            .map { it.packageName }
            .toSet()

        return appsWithUsage.map { appUsageInfo ->
            appUsageInfo.copy(isControlledApp = appUsageInfo.packageName in controlledAppPackages)
        }
    }
}