package com.applock.data.repo

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.pm.PackageManager
import com.applock.core.logE
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.TotalScreenTime
import com.applock.domain.repo.SystemAppRepo
import java.util.Calendar
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
            UsageStatsManager.INTERVAL_DAILY,
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

    override suspend fun getInstalledAppsWithUsagesInMap(days: Int): Map<String, List<Long>> {
        val usageStatsManager =
            context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - days * 24 * 60 * 60 * 1000L
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        val tempDailyUsageMap = mutableMapOf<String, MutableMap<Long, Long>>()
        usageStatsList?.forEach { stat ->
            val dayStartTimestamp = getStartOfDay(stat.firstTimeStamp)
            val packageName = stat.packageName
            tempDailyUsageMap
                .getOrPut(packageName) { mutableMapOf() }
                .merge(dayStartTimestamp, stat.totalTimeInForeground) { oldVal, newVal -> oldVal + newVal }
        }
        val pm = context.packageManager
        val appNameMap = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                pm.getLaunchIntentForPackage(appInfo.packageName) != null && appInfo.packageName != context.packageName
            }
            .associate { it.packageName to pm.getApplicationLabel(it).toString() }
        val chartDayTimestamps = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = endTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        for (i in 0 until days) {
            chartDayTimestamps.add(0, calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val resultDailyUsage = mutableMapOf<String, MutableList<Long>>()

        for ((pkgName, appName) in appNameMap) {
            val usageForAppAcrossDays = mutableListOf<Long>()
            for (dayTimestamp in chartDayTimestamps) {
                val usageOnDay = tempDailyUsageMap[pkgName]?.get(dayTimestamp) ?: 0L
                usageForAppAcrossDays.add(usageOnDay)
            }
            resultDailyUsage[pkgName] = usageForAppAcrossDays
        }
        val currentlyInstalledAppNames = getInstalledApp().map { it.packageName }.toSet()
        for (appName in currentlyInstalledAppNames) {
            if (!resultDailyUsage.containsKey(appName)) {
                resultDailyUsage[appName] = MutableList(days) { 0L }
            }
        }
        return resultDailyUsage.mapValues { it.value.toList() }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}