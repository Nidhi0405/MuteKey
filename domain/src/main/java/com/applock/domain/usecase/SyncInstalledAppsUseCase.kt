package com.applock.domain.usecase

import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class SyncInstalledAppsUseCase @Inject constructor(
    private val localAppRepo: LocalAppRepo, // db operations
    private val systemAppRepo: SystemAppRepo, // context operations
) {
    suspend operator fun invoke(startTime: Long, endTime: Long): List<AppUsageInfo> {
        val sysApps = systemAppRepo.getInstalledApp()
        val localApps = localAppRepo.getStoredApp()

        val sysInstalledPackages = sysApps.map { it.packageName }.toSet()
        val toBeDeleted = localApps.filter { it.packageName !in sysInstalledPackages }
        if (toBeDeleted.isNotEmpty()) {
            localAppRepo.deleteUninstalledApps(toBeDeleted)
        }

        localAppRepo.storeInstalledApps(sysApps)

        val appsWithUsage = systemAppRepo.getInstalledAppsWithUsage(startTime, endTime)

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