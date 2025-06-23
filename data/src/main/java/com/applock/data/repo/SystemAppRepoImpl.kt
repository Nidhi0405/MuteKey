package com.applock.data.repo

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.pm.PackageManager
import com.applock.core.isValidPackage
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
                appInfo.packageName.isValidPackage(context)
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
                if (pkg.isValidPackage(context) // to skip our app
                    && launchIntent != null
                    && totalUsage >= 0
                ) {
                    totalScreenTime.timeInMillis += totalUsage
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
                .merge(
                    dayStartTimestamp,
                    stat.totalTimeInForeground
                ) { oldVal, newVal -> oldVal + newVal }
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

    override suspend fun getHourlyAppUsageMapForToday(days: Int): Map<String, List<Long>> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)

        val sessionMap = mutableMapOf<String, MutableList<Pair<Long, Long>>>()
        val resumedMap = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    resumedMap[pkg] = event.timeStamp
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val start = resumedMap.remove(pkg) ?: continue
                    val end = event.timeStamp
                    sessionMap.getOrPut(pkg) { mutableListOf() }.add(start to end)
                }
            }
        }

        val appHourMap = mutableMapOf<String, List<Long>>() // final output map

        for ((pkg, sessions) in sessionMap) {
            val hourBuckets = MutableList(24) { mutableListOf<Pair<Long, Long>>() }

            for ((start, end) in sessions) {
                val cal = Calendar.getInstance().apply { timeInMillis = start }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourBuckets[hour].add(start to end)
            }

            val mergedDurations = hourBuckets.map { sessionList ->
                val merged = sessionList
                    .sortedBy { it.first }
                    .fold(mutableListOf<Pair<Long, Long>>()) { acc, pair ->
                        if (acc.isEmpty()) acc.add(pair)
                        else {
                            val last = acc.last()
                            if (pair.first <= last.second) {
                                acc[acc.lastIndex] = last.first to maxOf(last.second, pair.second)
                            } else acc.add(pair)
                        }
                        acc
                    }
                merged.sumOf { (start, end) -> (end - start) }
                    .coerceAtMost(60 * 60 * 1000L) // max 1 hour per hour bucket
            }

            appHourMap[pkg] = mergedDurations
        }
        appHourMap.entries.joinToString("\n") { "${it.key} => ${it.value.size}" }.logE()
        return appHourMap
    }
}