package com.applock.data.repo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.pm.PackageManager
import com.applock.core.logE
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.TotalScreenTime
import com.applock.domain.repo.SystemAppRepo
import javax.inject.Inject

class SystemAppRepoImpl @Inject constructor(
    private val context: Context
) : SystemAppRepo {
    override suspend fun getInstalledApp(): List<AppUsageInfo> {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            return apps.filter { appInfo ->
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                appInfo.packageName != context.packageName // to skip our app
                        && launchIntent != null
            }.map {
                AppUsageInfo(
                    name = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    usageTimeInMillis = 0L,
                    totalScreenTime = null
                )
            }
        } catch (e: Exception) {
            e.stackTraceToString().logE()
            return emptyList()
        }
    }

    override suspend fun getInstalledAppsWithUsage(days: Int): List<AppUsageInfo> {
        val usageStatsManager =
            context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()

        val startTime = endTime - days * 24 * 60 * 60 * 1000L

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, // still use INTERVAL_DAILY for better granularity
            startTime,
            endTime
        )

        val pm = context.packageManager
        val usageMap = mutableMapOf<String, Long>()

        usageStatsList?.forEach { stat ->
            usageMap[stat.packageName] =
                (usageMap[stat.packageName] ?: 0L) + stat.totalTimeInForeground
        }

        val appUsageList = mutableListOf<AppUsageInfo>()
        val totalScreenTime = TotalScreenTime(0)
        usageMap.forEach { (pkg, totalUsage) ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                totalScreenTime.timeInMillis += totalUsage
                if (pkg != context.packageName // to skip our app
                    && launchIntent != null
                    && totalUsage >= 0
                ) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    appUsageList.add(
                        AppUsageInfo(
                            appName,
                            pkg,
                            totalUsage,
                            totalScreenTime = totalScreenTime
                        )
                    )
                }
            } catch (e: Exception) {
                e.stackTraceToString().logE()
            }
        }

        return appUsageList
    }
}