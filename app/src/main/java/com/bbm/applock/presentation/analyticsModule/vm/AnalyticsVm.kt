package com.bbm.applock.presentation.analyticsModule.vm

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.usecase.GetHourlyUsageMapUseCase
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import com.bbm.applock.util.aggregateHourlyToDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class AnalyticsVm @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val getHourlyUsageMapUseCase: GetHourlyUsageMapUseCase,
    val imageLoader: ImageLoader,
) : BaseVM() {

    private var _installedApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val installedApps: StateFlow<List<AppUsageInfo>> = _installedApps

    private val _visibleMonth = MutableStateFlow(Calendar.getInstance())
    val visibleMonth: StateFlow<Calendar> = _visibleMonth

    private val _hourlyUsageRawMap = MutableStateFlow<Map<String, List<Long>>>(emptyMap())
    val hourlyUsageMap: StateFlow<Map<String, List<Long>>> = _hourlyUsageRawMap

    val topAppsForUsageTab: StateFlow<List<AppUsageInfo>> = installedApps
        .combine(_hourlyUsageRawMap) { apps, _ ->
            apps.filter { it.usageTimeInMillis > 0L }
                .sortedByDescending { it.usageTimeInMillis }
                .take(7)
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalUsageTimeForUsageTab: StateFlow<Pair<Long, Long>> = topAppsForUsageTab
        .combine(_hourlyUsageRawMap) { topApps, _ ->
            val totalTimeMillis = topApps.sumOf { it.usageTimeInMillis }
            val hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60
            hours to minutes
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L to 0L)

    private val _parsedChartData =
        MutableStateFlow<Pair<List<String>, List<Pair<String, List<Float>>>>>(
            Pair(emptyList(), emptyList())
        )

    val parsedChartData: StateFlow<Pair<List<String>, List<Pair<String, List<Float>>>>> =
        _parsedChartData

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

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis


    fun setVisibleMonth(calendar: Calendar) {
        _visibleMonth.value = calendar
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    init {
        viewModelScope.launch {
            viewModelScope.launch {
                syncCurrentMonth()
            }
        }
    }

    fun syncAndGetInstalledApps(startTime: Long, endTime: Long) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            try {
                val list = syncInstalledAppsUseCase.invoke(startTime, endTime)
                _installedApps.value = list
                val hourlyMap = getHourlyUsageMapUseCase.invoke(startTime, endTime)
                _hourlyUsageRawMap.value = hourlyMap
                _state.emit(UiState.Success(list, "Apps synced successfully"))
            } catch (e: Exception) {
                _state.emit(UiState.Failure(e, "Error syncing apps: ${e.message}"))
                Log.e("AnalyticsVm", "Error syncing installed apps: ${e.message}", e)
            }
        }
    }

    fun onDateTapped(date: Calendar) {
        val newMillis = date.timeInMillis
        val isSameDate = _selectedDateMillis.value == newMillis

        _selectedDateMillis.value = newMillis

        _viewMode.value = if (isSameDate) {
            when (_viewMode.value) {
                CalendarViewMode.DAY -> CalendarViewMode.WEEK
                CalendarViewMode.WEEK -> CalendarViewMode.DAY
                else -> CalendarViewMode.DAY
            }
        } else {
            CalendarViewMode.DAY
        }

        syncUsageForSelectedDate()
    }

    fun syncUsageForSelectedDate() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _selectedDateMillis.value
        }

        val (start, end) = when (_viewMode.value) {
            CalendarViewMode.DAY -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = calendar.timeInMillis

                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                start to calendar.timeInMillis
            }

            CalendarViewMode.WEEK -> {
                calendar.apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = calendar.timeInMillis

                calendar.apply {
                    add(Calendar.DAY_OF_WEEK, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                start to calendar.timeInMillis
            }

            CalendarViewMode.MONTH -> {
                calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = calendar.timeInMillis

                calendar.apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                start to calendar.timeInMillis
            }
        }
        syncAndGetInstalledApps(start, end)
    }

    enum class CalendarViewMode { MONTH, WEEK, DAY }

    fun syncCurrentMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis

        syncAndGetInstalledApps(startTime, endTime)
    }

    fun loadUsage(mode: String) {
        viewModelScope.launch(dispatchers.io) {
            val end = System.currentTimeMillis()
            val start = when (mode) {
                "DAILY" ->
                    end - 24 * 60 * 60 * 1000L

                "WEEKLY" ->
                    end - (7L * 24 * 60 * 60 * 1000L)

                "MONTHLY" ->
                    end - (180L * 24 * 60 * 60 * 1000L)

                else -> end
            }
            try {
                val hourlyMap =
                    getHourlyUsageMapUseCase.invoke(start, end)
                val processed = when (mode) {
                    "DAILY" -> hourlyMap
                    "WEEKLY" ->
                        aggregateHourlyToDays(hourlyMap, 7)

                    "MONTHLY" ->
                        aggregateHourlyToDays(hourlyMap, 30)

                    else -> hourlyMap
                }
                _hourlyUsageRawMap.value = processed
            } catch (e: Exception) {
                Log.e("AnalyticsVm", "Error loading usage", e)
            }
        }
    }

    fun debugWeeklyUsage(context: Context, startTime: Long, endTime: Long) {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val totalUsageByApp = stats.groupBy { it.packageName }.mapValues { (_, list) ->
            list.sumOf { it.totalTimeInForeground }
        }

        totalUsageByApp.forEach { (pkg, totalMillis) ->
            Log.d("UsageStatsCheck", "$pkg -> ${totalMillis / 60000} min")
        }
    }

    fun getStartDate(context: Context): Calendar {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = 0L
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        )
        val cal = Calendar.getInstance()
        if (stats.isNullOrEmpty()) {
            Log.d("UsageStart", "No usage data available")
            return cal
        }
        val earliest = stats.minOf { it.firstTimeStamp }
        cal.timeInMillis = earliest
        Log.d("UsageStart", "Earliest usage timestamp = $earliest")
        Log.d("UsageStart", "Earliest usage date = ${cal.time}")
        return cal
    }

    fun getAlignedSeriesFromHourlyMap(
        context: Context,
        hourlyUsageMap: Map<String, List<Long>>,
        usageMode: String
    ): Pair<List<String>, List<Pair<String, List<Float>>>> {
        val labels: List<String>
        val series: List<Pair<String, List<Float>>>

        when (usageMode) {
            "Daily" -> {
                val totalHours = 24
                val maxLabels = 8
                val step = ceil(totalHours.toFloat() / maxLabels).toInt()

                labels = (0 until totalHours step step).map { "${it}h" } + "24h"

                series = hourlyUsageMap.map { (pkg, hourlyList) ->
                    val sampledValues = (0 until totalHours step step).map { idx ->
                        hourlyList.getOrElse(idx) { 0L } / 60000f
                    }
                    val lastValue = hourlyList.getOrElse(totalHours - 1) { 0L } / 60000f
                    pkg to (sampledValues + lastValue)
                }
            }

            "Weekly" -> {
                val weeklyMap = getAllWeeklyUsage(context)

                val startDate = getStartDate(context)
                val calendar = startDate.clone() as Calendar
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.minimalDaysInFirstWeek = 1

                val now = Calendar.getInstance()

                val allWeeks = mutableSetOf<Int>()
                val tempCal = startDate.clone() as Calendar
                while (tempCal <= now) {
                    val yearWeek =
                        tempCal.get(Calendar.YEAR) * 100 + tempCal.get(Calendar.WEEK_OF_YEAR)
                    allWeeks.add(yearWeek)
                    tempCal.add(Calendar.WEEK_OF_YEAR, 1)
                }

                val currentYearWeek = now.get(Calendar.YEAR) * 100 + now.get(Calendar.WEEK_OF_YEAR)
                allWeeks.add(currentYearWeek)

                val sortedWeeks = allWeeks.sorted()
                Log.d("WeeklyDebug", "All weeks = $sortedWeeks")

                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                labels = sortedWeeks.mapIndexed { index, yearWeek ->
                    val year = yearWeek / 100
                    val week = yearWeek % 100

                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.WEEK_OF_YEAR, week)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                    val startLabelDate =
                        if (index == 0 && startDate.after(calendar)) startDate.time else calendar.time

                    val endCal = calendar.clone() as Calendar
                    endCal.add(Calendar.DAY_OF_WEEK, 6)
                    val endLabelDate = endCal.time

                    val label =
                        "${dateFormat.format(startLabelDate)} – ${dateFormat.format(endLabelDate)}"
                    Log.d("WeeklyDebug", "Label for $yearWeek = $label")
                    label
                }

                val totalUsagePerApp = weeklyMap.mapValues { it.value.values.sum() }
                val top5Apps = totalUsagePerApp.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key }

                series = top5Apps.map { pkg ->
                    val weekMap = weeklyMap[pkg] ?: emptyMap()
                    val values = sortedWeeks.map { weekKey ->
                        (weekMap[weekKey] ?: 0L) / 60000f
                    }
                    Log.d("WeeklyDebug", "Series for $pkg = $values")
                    pkg to values
                }
            }

            "Monthly" -> {
                val startDate = getStartDate(context)
                val calendar = startDate.clone() as Calendar
                calendar.set(Calendar.DAY_OF_MONTH, 1)

                val now = Calendar.getInstance()
                val monthLabels = mutableListOf<String>()
                val monthKeys = mutableListOf<Pair<Int, Int>>()
                val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

                while (calendar <= now) {
                    monthLabels.add(dateFormat.format(calendar.time))
                    monthKeys.add(calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH))
                    calendar.add(Calendar.MONTH, 1)
                }

                val dailyMap = getAllDailyUsage(context)
                val totalUsagePerApp = dailyMap.mapValues { it.value.values.sum() }
                val top5Apps = totalUsagePerApp.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key }

                series = top5Apps.map { pkg ->
                    val usageMap = dailyMap[pkg] ?: emptyMap()
                    val values = monthKeys.map { (year, month) ->
                        usageMap.filter { (ts, _) ->
                            val cal = Calendar.getInstance().apply { timeInMillis = ts }
                            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                        }.values.sum() / 60000f
                    }
                    pkg to values
                }
                labels = monthLabels
            }

            else -> {
                labels = emptyList()
                series = emptyList()
            }
        }

        return labels to series
    }

    fun getAllDailyUsage(context: Context): Map<String, Map<Long, Long>> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            0L,
            System.currentTimeMillis()
        )

        val dailyMap = mutableMapOf<String, MutableMap<Long, Long>>()
        val calendar = Calendar.getInstance()

        stats.forEach { stat ->
            // Round timestamp to start of day (midnight)
            calendar.timeInMillis = stat.firstTimeStamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStartTs = calendar.timeInMillis

            val pkgMap = dailyMap.getOrPut(stat.packageName) { mutableMapOf() }
            pkgMap[dayStartTs] = (pkgMap[dayStartTs] ?: 0L) + stat.totalTimeInForeground
        }

        return dailyMap
    }

    fun getAllWeeklyUsage(context: Context): Map<String, Map<Int, Long>> {

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            0L,
            System.currentTimeMillis()
        )

        Log.d("WeeklyDebug", "Total stats received = ${stats.size}")

        val weeklyMap = mutableMapOf<String, MutableMap<Int, Long>>()
        val calendar = Calendar.getInstance()

        stats.forEach { stat ->

            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.minimalDaysInFirstWeek = 1
            calendar.timeInMillis = stat.lastTimeStamp

            val year = calendar.get(Calendar.YEAR)
            val week = calendar.get(Calendar.WEEK_OF_YEAR)

            val yearWeek = year * 100 + week

            Log.d(
                "WEEK_DEBUG",
                "Pkg=${stat.packageName} Date=${calendar.time} Week=$yearWeek Usage=${stat.totalTimeInForeground}"
            )

            val pkgMap = weeklyMap.getOrPut(stat.packageName) { mutableMapOf() }

            pkgMap[yearWeek] =
                (pkgMap[yearWeek] ?: 0L) + stat.totalTimeInForeground
        }

        return weeklyMap
    }


    fun generateColor(index: Int): Int {
        val hue = (index * 47f) % 360f
        return Color.hsv(hue, 0.8f, 0.95f).toArgb()
    }
}