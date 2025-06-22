package com.applock.domain.repo

import com.applock.domain.model.AppUsageInfo

interface SystemAppRepo {
    suspend fun getInstalledApp(): List<AppUsageInfo> // to get from context
    suspend fun getInstalledAppsWithUsage(days: Int): List<AppUsageInfo>
    suspend fun getInstalledAppsWithUsagesInMap(days: Int): Map<String, List<Long>>
    suspend fun getHourlyAppUsageMapForToday(days: Int): Map<String, List<Long>>
}