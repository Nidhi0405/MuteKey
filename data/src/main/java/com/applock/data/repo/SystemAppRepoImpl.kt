package com.applock.data.repo

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.pm.PackageManager
import android.util.Log
import com.applock.core.isValidPackage
import com.applock.core.logE
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.TotalScreenTime
import com.applock.domain.repo.SystemAppRepo
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
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
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val oldest = today.minusDays((days - 1).toLong())

        // Build date axis oldest..newest
        val dates: List<java.time.LocalDate> = (0 until days).map { i -> oldest.plusDays(i.toLong()) }

        // Only launchable, valid packages (and exclude our own)
        val selfPkg = context.packageName
        val launchable = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .mapNotNull { app ->
                val hasLauncher = pm.getLaunchIntentForPackage(app.packageName) != null
                if (hasLauncher && app.packageName.isValidPackage(context) && app.packageName != selfPkg)
                    app.packageName
                else null
            }
            .toSet()

        // pkg -> per-day list initialized to 0
        val out = mutableMapOf<String, MutableList<Long>>()
        fun ensurePkg(pkg: String) {
            if (out[pkg] == null) out[pkg] = MutableList(days) { 0L }
        }

        dates.forEachIndexed { idx, d ->
            val start = d.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = d.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

            // Ask the system JUST for this civil day
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) ?: emptyList()

            // Sum per package for the day
            stats.forEach { s ->
                val pkg = s.packageName ?: return@forEach
                if (pkg == selfPkg || pkg !in launchable) return@forEach

                // totalTimeInForeground is already the aggregate in [start, end]
                val ms = s.totalTimeInForeground
                if (ms > 0L) {
                    ensurePkg(pkg)
                    out[pkg]!![idx] = out[pkg]!![idx] + ms
                }
            }
        }

        // Make sure all launchable packages exist in the map (even if all zeros)
        launchable.forEach { ensurePkg(it) }

        return out.mapValues { it.value.toList() } // pkg -> List<Long> oldest..newest
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
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val selfPkg = context.packageName
        val buckets = mutableMapOf<String, LongArray>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in days - 1 downTo 0) {
            val resumed = mutableMapOf<String, Long>()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, -i)
            }

            val startTime = cal.timeInMillis
            val endTime = startTime + TimeUnit.DAYS.toMillis(1)
            val events = usm.queryEvents(startTime, endTime)
            val evt = UsageEvents.Event()
            var totalEvents = 0
            var usefulEvents = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(evt)
                totalEvents++

                val pkg = evt.packageName ?: continue
                if (pkg == selfPkg) continue

                when (evt.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        resumed[pkg] = evt.timeStamp
                        usefulEvents++
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val s = resumed.remove(pkg) ?: continue
                        val e = evt.timeStamp
                        val arr = buckets.getOrPut(pkg) { LongArray(24 * days) }
                        arr.addSpan(s, e, i)
                        usefulEvents++
                    }
                }
            }

            resumed.forEach { (pkg, s) ->
                if (pkg == selfPkg) return@forEach
                val arr = buckets.getOrPut(pkg) { LongArray(24 * days) }
                arr.addSpan(s, endTime, i)
            }

            Log.d("USAGE_STATS_DAY", "📅 Date: ${dateFormat.format(Date(startTime))} | Total Events: $totalEvents | Useful: $usefulEvents")
        }

        return buckets.mapValues { (_, arr) ->
            arr.map { it.coerceAtMost(3_600_000L) }
        }
    }
    private fun LongArray.addSpan(start: Long, end: Long, dayOffset: Int) {
        val split = splitAcrossHours(start, end)
        for (h in 0 until 24) {
            val index = dayOffset * 24 + h
            if (index in indices) this[index] += split[h]
        }
    }






    /** Break a [start-end] interval into millis per civil hour. */
    private fun splitAcrossHours(start: Long, end: Long): LongArray {
        val out = LongArray(24)
        var curStart = start

        while (curStart < end) {
            val cal = Calendar.getInstance().apply { timeInMillis = curStart }
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            // advance to next top-of-hour
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.HOUR_OF_DAY, 1)

            val curEnd = minOf(cal.timeInMillis, end)
            out[hour] += curEnd - curStart
            curStart = curEnd
        }
        return out
    }
}