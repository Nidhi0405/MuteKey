package com.applock.domain.repo

import com.applock.domain.model.AppUsageInfo

interface SystemAppRepo {
    suspend fun getInstalledApp(): List<AppUsageInfo> // to get from context
    suspend fun getInstalledAppsWithUsage(startTime: Long, endTime: Long): List<AppUsageInfo>
    suspend fun getInstalledAppsWithUsagesInMap(days: Int): Map<String, List<Long>>
    suspend fun getHourlyAppUsageMapForToday(startTime: Long, endTime: Long): Map<String, List<Long>>
}