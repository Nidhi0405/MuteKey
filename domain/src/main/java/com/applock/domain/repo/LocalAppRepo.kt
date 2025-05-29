package com.applock.domain.repo

import com.applock.domain.model.AppUsageInfo

interface LocalAppRepo {

    suspend fun getStoredApp(): List<AppUsageInfo>

    suspend fun storeInstalledApps(apps: List<AppUsageInfo>)

    suspend fun deleteUninstalledApps(apps: List<AppUsageInfo>)

    suspend fun getControlledApps(): List<AppUsageInfo>

    suspend fun storeControlledApps(appUsageInfo: AppUsageInfo)

    suspend fun deleteControlledApp(app: AppUsageInfo)
}