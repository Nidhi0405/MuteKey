package com.bbm.applock.presentation.analyticsModule.vm

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.usecase.GetDailyAppUsageForChartUseCase
import com.applock.domain.usecase.GetHourlyUsageMapUseCase
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AnalyticsVm @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val getDailyAppUsageForChartUseCase: GetDailyAppUsageForChartUseCase,
    private val getHourlyUsageMapUseCase: GetHourlyUsageMapUseCase,
    val imageLoader: ImageLoader,
    @ApplicationContext val context: Context
) : BaseVM() {

    private var _installedApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val installedApps: StateFlow<List<AppUsageInfo>> = _installedApps

    private val _chartRawJson = MutableStateFlow("")
    val chartJson: StateFlow<String> = _chartRawJson

    private val _hourlyUsageRawMap = MutableStateFlow<Map<String, List<Long>>>(emptyMap())
    val hourlyUsageMap: StateFlow<Map<String, List<Long>>> = _hourlyUsageRawMap

    val topAppsForUsageTab: StateFlow<List<AppUsageInfo>> = installedApps
        .combine(_hourlyUsageRawMap) { apps, _ -> // _hourlyUsageRawMap is used to trigger updates
            apps.filter { it.usageTimeInMillis > 0L }
                .sortedByDescending { it.usageTimeInMillis }
                .take(7)
        }
        .flowOn(dispatchers.default) // Heavy filtering/sorting on default dispatcher
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList()) // Convert to StateFlow

    val totalUsageTimeForUsageTab: StateFlow<Pair<Long, Long>> = topAppsForUsageTab
        .combine(_hourlyUsageRawMap) { topApps, _ -> // _hourlyUsageRawMap is used to trigger updates
            val totalTimeMillis = topApps.sumOf { it.usageTimeInMillis }
            val hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60
            hours to minutes
        }
        .flowOn(dispatchers.default) // Summation on default dispatcher
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L to 0L) // Convert to StateFlow

    val parsedChartData: StateFlow<Pair<List<String>, List<Pair<String, List<Float>>>>> = chartJson
        .combine(_hourlyUsageRawMap) { json, _ -> // _hourlyUsageRawMap is used to trigger updates
            if (json.isNotBlank()) {
                parseChartJson(json)
            } else {
                emptyList<String>() to emptyList()
            }
        }
        .flowOn(dispatchers.default) // JSON parsing on default dispatcher
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList<String>() to emptyList()
        ) // Convert to StateFlow

    val combinedHourlyUsage: StateFlow<List<Long>> = hourlyUsageMap
        .combine(_installedApps) { hourlyMap, _ -> // _installedApps is used to trigger updates
            val combined = MutableList(24) { 0L }
            hourlyMap.values.forEach { appList ->
                appList.forEachIndexed { hour, millis ->
                    if (hour < combined.size) { // Ensure index is within bounds
                        combined[hour] += millis
                    }
                }
            }
            combined
        }
        .flowOn(dispatchers.default) // Summation on default dispatcher
        .stateIn(viewModelScope, SharingStarted.Lazily, List(24) { 0L }) // Convert to StateFlow

    private val dailyCache =
        MutableStateFlow<Map<java.time.LocalDate, Map<String, Long>>>(emptyMap())

    // at top-level fields
    private val _isCacheReady = MutableStateFlow(false)
    val isCacheReady: StateFlow<Boolean> = _isCacheReady

    init {
        viewModelScope.launch {
            fetchAndGenerateChartJson(7)
            syncHourlyUsage(1)
        }
    }

    fun syncAndGetInstalledApps(days: Int) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            try {
                val list = syncInstalledAppsUseCase.invoke(days)
                _installedApps.value = list
                _state.emit(UiState.Success(list, "Apps synced successfully"))
            } catch (e: Exception) {
                _state.emit(UiState.Failure(e, "Error syncing apps: ${e.message}"))
                Log.e("AnalyticsVm", "Error syncing installed apps: ${e.message}", e)
            }
        }
    }

    fun fetchAndGenerateChartJson(days: Int) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            try {
                val dailyUsageDataMap = getDailyAppUsageForChartUseCase.invoke(days)
                val root = JSONObject()
                val numberOfDays = days
                val calendar = Calendar.getInstance()
                val dates = mutableListOf<String>()
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

                // Set calendar to the start of the first day to include
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -(numberOfDays - 1))
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                for (i in 0 until numberOfDays) {
                    if (i == numberOfDays - 1) {
                        dates.add("Today")
                    } else {
                        dates.add(dateFormat.format(calendar.time))
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                root.put("dates", JSONArray(dates))

                val seriesArray = JSONArray()
                for ((packageName, usageList) in dailyUsageDataMap) {
                    val usageDataPoints = JSONArray()
                    // Pad or truncate usageList to match numberOfDays
                    val processedUsageList = when {
                        usageList.size < numberOfDays -> usageList.toMutableList()
                            .apply { repeat(numberOfDays - usageList.size) { add(0L) } }.toList()

                        usageList.size > numberOfDays -> usageList.takeLast(numberOfDays)
                        else -> usageList
                    }

                    for (usageMillis in processedUsageList) {
                        val usageInHours = usageMillis / (1000f * 60f * 60f)
                        usageDataPoints.put(String.format("%.2f", usageInHours).toFloat())
                    }
                    val seriesObj = JSONObject().apply {
                        put("name", packageName)
                        put("data", usageDataPoints)
                    }
                    seriesArray.put(seriesObj)
                }
                root.put("series", seriesArray)
                _chartRawJson.value = root.toString()
                Log.d("AnalyticsVm", "Generated Chart JSON (Multi-Day): ${_chartRawJson.value}")
                _state.emit(UiState.Success(null, "Chart data generated successfully"))
            } catch (e: Exception) {
                _chartRawJson.value = ""
                e.printStackTrace()
                Log.e("AnalyticsVm", "Error in fetchAndGenerateChartJson: ${e.message}", e)
                _state.emit(UiState.Failure(e, "Error generating chart data: ${e.message}"))
            }
        }
    }

    fun syncHourlyUsage(days: Int) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val map = getHourlyUsageMapUseCase.invoke(days)
                _hourlyUsageRawMap.value = map
                _state.emit(UiState.Success(null, "Hourly usage synced"))
            } catch (e: Exception) {
                _state.emit(UiState.Failure(e, "Error syncing hourly usage: ${e.message}"))
                Log.e("AnalyticsVm", "Error syncing hourly usage: ${e.message}", e)
            }
        }
    }

    private fun parseChartJson(json: String): Pair<List<String>, List<Pair<String, List<Float>>>> {
        val jsonObject = JSONObject(json)
        val datesArray = jsonObject.getJSONArray("dates")
        val labels = List(datesArray.length()) { i -> datesArray.getString(i) }
        val seriesArray = jsonObject.getJSONArray("series")
        val seriesList = mutableListOf<Pair<String, List<Float>>>()
        for (i in 0 until seriesArray.length()) {
            val seriesObj = seriesArray.getJSONObject(i)
            val name = seriesObj.getString("name")
            val dataArray = seriesObj.getJSONArray("data")
            val dataList = List(dataArray.length()) { j ->
                dataArray.optDouble(j, 0.0).toFloat()
            }
            seriesList.add(name to dataList)
        }
        val top5 = seriesList
            .sortedByDescending { it.second.sum() }
            .take(5)
        return labels to top5
    }

    // replace your primeDailyCache with this version (adds onReady + ready flag + logs)
    fun primeDailyCache(days: Int = 16, onReady: (() -> Unit)? = null) {
        viewModelScope.launch(dispatchers.io) {
            _isCacheReady.value = false
            try {
                val perAppSeries =
                    getDailyAppUsageForChartUseCase.invoke(days) // pkg → List<Long> (oldest..newest)
                val zone = java.time.ZoneId.systemDefault()
                val today = java.time.LocalDate.now(zone)

                // Oldest..newest dates (today - (days-1) .. today)
                val dates = (0 until days).map { i -> today.minusDays((days - 1 - i).toLong()) }
                val dateMap = mutableMapOf<java.time.LocalDate, MutableMap<String, Long>>()
                dates.forEach { d -> dateMap[d] = mutableMapOf() }
                perAppSeries.forEach { (pkg, series) ->
                    android.util.Log.d("AnalyticsVm", "Raw [$pkg]: ${series.joinToString()}")
                }
                // repo returns exactly 'days' buckets per pkg in oldest..newest order
                perAppSeries.forEach { (pkg, raw) ->
                    val aligned = raw.takeLast(days)
                    dates.forEachIndexed { idx, d ->
                        val ms = aligned[idx]
                        if (ms > 0L) {
                            dateMap[d]!![pkg] = (dateMap[d]!![pkg] ?: 0L) + ms
                        }
                    }
                }

                // logs
                android.util.Log.d(
                    "AnalyticsVm",
                    "primeDailyCache: cached=${dateMap.size} (oldest..newest)"
                )
                dateMap.keys.sorted().forEach { d ->
                    val totalMs = dateMap[d]!!.values.sum()
                    android.util.Log.d("AnalyticsVm", "  $d total=${formatH(totalMs)}")
                }

                dailyCache.value = dateMap
                _isCacheReady.value = true
                onReady?.let { withContext(dispatchers.main) { it() } }
            } catch (e: Exception) {
                android.util.Log.e("AnalyticsVm", "primeDailyCache error: ${e.message}", e)
                dailyCache.value = emptyMap()
                _isCacheReady.value = false
            }
        }
    }

    private fun formatH(ms: Long): String {
        val h = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(ms)
        val m = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        return "${h}h ${m}m"
    }

    fun showDay(date: java.time.LocalDate) {
        viewModelScope.launch(dispatchers.default) {
            if (!_isCacheReady.value) {
                android.util.Log.w("AnalyticsVm", "showDay called before cache ready")
                return@launch
            }
            val bucket = dailyCache.value[date].orEmpty()
            val pm = context.packageManager

            val list = bucket.entries
                .map { (pkg, ms) ->
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val name = pm.getApplicationLabel(appInfo).toString()
                    AppUsageInfo(name, pkg, ms, false)
                }
                .filter { it.usageTimeInMillis > 0L }
                .sortedByDescending { it.usageTimeInMillis }

            android.util.Log.d(
                "AnalyticsVm",
                "showDay($date): pkgs=${bucket.size}, total=${formatH(bucket.values.sum())}"
            )
            _installedApps.value = list
            _hourlyUsageRawMap.value = emptyMap()
        }
    }

    /*fun showWeekChunk(weekOffset: Int) {
        viewModelScope.launch(dispatchers.default) {
            if (!_isCacheReady.value) {
                Log.w("AnalyticsVm", "showWeekChunk called before cache ready")
                return@launch
            }
            val datesSorted = dailyCache.value.keys.sorted() // oldest..newest
            if (datesSorted.isEmpty()) {
                _installedApps.value = emptyList()
                return@launch
            }

            val today = LocalDate.now()
            val idxToday = datesSorted.indexOf(today).takeIf { it >= 0 } ?: datesSorted.lastIndex

            // current week: [idxToday-6..idxToday], previous: shift by 7
            val end = (idxToday - (7 * weekOffset)).coerceAtMost(datesSorted.lastIndex)
            val start = (end - 6).coerceAtLeast(0)

            Log.d(
                "AnalyticsVm",
                "showWeekChunk($weekOffset): ${datesSorted[start]}..${datesSorted[end]} idx=[$start..$end] todayIdx=$idxToday"
            )

            val sumPerPkg = mutableMapOf<String, Long>()
            for (i in start..end) {
                val d = datesSorted[i]
                dailyCache.value[d].orEmpty().forEach { (pkg, ms) ->
                    sumPerPkg[pkg] = (sumPerPkg[pkg] ?: 0L) + ms
                }
            }
            Log.d(
                "AnalyticsVm",
                "showWeekChunk($weekOffset): total=${formatH(sumPerPkg.values.sum())}"
            )
            val pm = context.packageManager
            val list = sumPerPkg.entries
                .map { (pkg, ms) ->
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val name = pm.getApplicationLabel(appInfo).toString()
                    AppUsageInfo(name, pkg, ms, false)
                }
                .filter { it.usageTimeInMillis > 0L }
                .sortedByDescending { it.usageTimeInMillis }

            _installedApps.value = list
            _hourlyUsageRawMap.value = emptyMap()
        }
    }*/

    fun showWeekChunk(weekOffset: Int) {
        viewModelScope.launch(dispatchers.default) {
            if (!_isCacheReady.value) {
                Log.w("AnalyticsVm", "showWeekChunk called before cache ready")
                return@launch
            }
            val datesSorted = dailyCache.value.keys.sorted()
            if (datesSorted.isEmpty()) {
                _installedApps.value = emptyList()
                return@launch
            }

            val today = LocalDate.now()
            val currentDayOfWeek = today.dayOfWeek
            val idxToday = datesSorted.indexOf(today).takeIf { it >= 0 } ?: datesSorted.lastIndex

            val targetEndIndex: Int
            val targetStartIndex: Int

            if (weekOffset == 0) { // Current week
                targetEndIndex = idxToday
                // Start from Monday of the current week, but not before the first cached date
                // And not after today's index
                targetStartIndex =
                    (idxToday - (currentDayOfWeek.value - java.time.DayOfWeek.MONDAY.value))
                        .coerceAtLeast(0)
                        .coerceAtMost(idxToday)
            } else { // Past weeks
                // Calculate the end of the target past week
                // (idxToday - days from today to last Sunday) gets us to the end of last week (Sunday)
                // Then shift back by (weekOffset - 1) * 7 days for further past weeks
                val daysToLastSunday =
                    currentDayOfWeek.value // Sunday is 7, Monday is 1. If today is Tue (2), shift back 2 days.
                targetEndIndex = (idxToday - daysToLastSunday - ((weekOffset - 1) * 7))
                    .coerceAtMost(datesSorted.lastIndex)
                    .coerceAtLeast(0) // Ensure not negative if cache is too small

                // Start of that target past week (6 days before its end, i.e., Monday)
                targetStartIndex = (targetEndIndex - 6).coerceAtLeast(0)
            }

            // Ensure start index is not greater than end index, can happen if cache is very sparse or offset is too large
            val validStartIndex = targetStartIndex.coerceAtMost(targetEndIndex)

            Log.d(
                "AnalyticsVm",
                "showWeekChunk($weekOffset): ${datesSorted.getOrNull(validStartIndex)}..${
                    datesSorted.getOrNull(
                        targetEndIndex
                    )
                } idx=[$validStartIndex..$targetEndIndex] todayIdx=$idxToday, dayOfWeek=${currentDayOfWeek.value}"
            )

            val sumPerPkg = mutableMapOf<String, Long>()
            if (validStartIndex <= targetEndIndex && validStartIndex < datesSorted.size && targetEndIndex < datesSorted.size) { // Check bounds
                for (i in validStartIndex..targetEndIndex) {
                    val d = datesSorted[i]
                    dailyCache.value[d].orEmpty().forEach { (pkg, ms) ->
                        sumPerPkg[pkg] = (sumPerPkg[pkg] ?: 0L) + ms
                    }
                }
            } else {
                Log.w(
                    "AnalyticsVm",
                    "showWeekChunk($weekOffset): Invalid date range or indices out of bounds after calculation. Start: $validStartIndex, End: $targetEndIndex, CacheSize: ${datesSorted.size}"
                )
            }

            Log.d(
                "AnalyticsVm",
                "showWeekChunk($weekOffset): total=${formatH(sumPerPkg.values.sum())}"
            )
            val pm = context.packageManager
            val list = sumPerPkg.entries
                .mapNotNull { (pkg, ms) ->
                    try {
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        val name = pm.getApplicationLabel(appInfo).toString()
                        AppUsageInfo(name, pkg, ms, false)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                }
                .filter { it.usageTimeInMillis > 0L }
                .sortedByDescending { it.usageTimeInMillis }

            _installedApps.value = list
            _hourlyUsageRawMap.value = emptyMap()
        }
    }

}