package com.bbm.applock.presentation.analyticsModule.vm

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.usecase.GetHourlyUsageMapUseCase
import com.applock.domain.usecase.SyncInstalledAppsUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.analyticsModule.uiState.CalendarViewMode
import com.bbm.applock.presentation.analyticsModule.uiState.UsageUiState
import com.bbm.applock.presentation.base.BaseVM
import com.bbm.applock.util.aggregateHourlyToDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

fun AnalyticsVm.CalendarViewMode.toUi(): CalendarViewMode {
    return when (this) {
        AnalyticsVm.CalendarViewMode.DAY -> CalendarViewMode.DAY
        AnalyticsVm.CalendarViewMode.WEEK -> CalendarViewMode.WEEK
        AnalyticsVm.CalendarViewMode.MONTH -> CalendarViewMode.MONTH
    }
}

@HiltViewModel
class AnalyticsVm @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val getHourlyUsageMapUseCase: GetHourlyUsageMapUseCase,
    val imageLoader: ImageLoader,
) : BaseVM() {

    private var _installedApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val installedApps: StateFlow<List<AppUsageInfo>> = _installedApps

    private val _visibleMonth = MutableStateFlow(YearMonth.now())
    val visibleMonth: StateFlow<YearMonth> = _visibleMonth

    private val _hourlyUsageRawMap = MutableStateFlow<Map<String, List<Long>>>(emptyMap())
    val hourlyUsageMap: StateFlow<Map<String, List<Long>>> = _hourlyUsageRawMap

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTappedAgain = MutableStateFlow(false)
    val isTappedAgain: StateFlow<Boolean> = _isTappedAgain


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
        .map { hourlyMap ->
            val combined = MutableList(24) { 0L }
            hourlyMap.values.forEach { appList ->
                appList.forEachIndexed { index, millis ->
                    val hourOfDay = index % 24
                    combined[hourOfDay] += millis
                }
            }
            combined
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.Lazily, List(24) { 0L })

    private val _todayHourlyUsageMap = MutableStateFlow<Map<String, List<Long>>>(emptyMap())
    val todayHourlyUsageMap: StateFlow<Map<String, List<Long>>> = _todayHourlyUsageMap
    val todayHourlyUsage: StateFlow<List<Long>> = todayHourlyUsageMap
        .map { hourlyMap ->
            val combined = MutableList(24) { 0L }
            hourlyMap.values.forEach { list ->
                list.forEachIndexed { index, millis ->
                    if (index < 24) combined[index] += millis
                }
            }
            combined
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.Lazily, List(24) { 0L })

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode

    private val _radarSelectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val radarSelectedDateMillis: StateFlow<Long> = _radarSelectedDateMillis

    private val _usageSelectedDateMillis = MutableStateFlow<Long?>(null)
    val usageSelectedDateMillis: StateFlow<Long?> = _usageSelectedDateMillis

    private val _selectedRadarData = MutableStateFlow<List<Long>?>(null)
    val selectedRadarData: StateFlow<List<Long>?> = _selectedRadarData

    private val _selectedRadarMap = MutableStateFlow<Map<String, List<Long>>?>(null)
    val selectedRadarMap: StateFlow<Map<String, List<Long>>?> = _selectedRadarMap

    fun setVisibleMonth(newMonth: YearMonth) {
        _visibleMonth.value = newMonth
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    init {
        viewModelScope.launch {
            syncCurrentMonth()
            syncTodayHourlyUsage()
            syncUsageForSelectedDate()
        }
    }

    val uiState: StateFlow<UsageUiState> = combine(
        installedApps,
        topAppsForUsageTab,
        totalUsageTimeForUsageTab,
        usageSelectedDateMillis,
        viewMode
    ) { apps, topApps, totalTime, dateMillis, mode ->

        val (hours, minutes) = totalTime

        val filteredApps = apps
            .filter { it.usageTimeInMillis > 0L }
            .sortedByDescending { it.usageTimeInMillis }

        val emptyMessage = when (mode) {
            CalendarViewMode.DAY -> "No usage data for selected day."
            CalendarViewMode.WEEK -> "No usage data for selected week."
            CalendarViewMode.MONTH -> "No usage data for selected month."
        }

        UsageUiState(
            appUsageList = filteredApps,
            chartApps = topApps,
            totalHours = hours,
            totalMinutes = minutes,
            selectedDateMillis = dateMillis,
            viewMode = mode.toUi(),
            emptyMessage = emptyMessage
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UsageUiState(
                appUsageList = emptyList(),
                chartApps = emptyList(),
                totalHours = 0,
                totalMinutes = 0,
                selectedDateMillis = null,
                viewMode = CalendarViewMode.MONTH.toUi(),
                emptyMessage = ""
            )
        )

    fun syncTodayHourlyUsage() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        val end = System.currentTimeMillis()

        viewModelScope.launch(dispatchers.io) {
            val hourlyMap = getHourlyUsageMapUseCase.invoke(start, end)
            _todayHourlyUsageMap.value = hourlyMap
        }
    }

    fun syncAndGetInstalledApps(startTime: Long, endTime: Long, isTodayMap: Boolean = false) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            try {
                val list = syncInstalledAppsUseCase.invoke(startTime, endTime)
                _installedApps.value = list
                val hourlyMap = getHourlyUsageMapUseCase.invoke(startTime, endTime)
                /*if (isTodayMap) {
                    _todayHourlyUsageMap.value = hourlyMap
                } else {
                    _hourlyUsageRawMap.value = hourlyMap
                }*/
                _hourlyUsageRawMap.value = hourlyMap
                _todayHourlyUsageMap.value = hourlyMap
                val appsTotal = list.sumOf { it.usageTimeInMillis } //sum of every app’s usage individually, sum of every app’s usage individually
                val hourlyTotal = hourlyMap.values.flatten().sum() //sum of all hourly buckets, counting overlapping usage only once per hour,
                val realTotal = hourlyMap.values.flatten().sum() //the actual device usage time without double-counting overlapping app usage
                Log.d("REAL_USAGE_TOTAL", "Real Total Usage = ${realTotal / 60000} min")
                Log.d("USAGE_TOTAL", "Apps Total (sum of all apps) = ${appsTotal / 60000} min")
                Log.d("USAGE_TOTAL", "Hourly Total (sum per hour, overlapping counted once) = ${hourlyTotal / 60000} min")

                _state.emit(UiState.Success(list, "Apps synced successfully"))
            } catch (e: Exception) {
                _state.emit(UiState.Failure(e, "Error syncing apps: ${e.message}"))
                Log.e("AnalyticsVm", "Error syncing installed apps: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isSameAsSelectedUsageDate(date: Calendar): Boolean {
        val selectedCal = Calendar.getInstance().apply { timeInMillis = _usageSelectedDateMillis.value ?: 0L }
        return isSameDay(selectedCal, date)
    }

    fun onRadarDateTapped(date: Calendar) {
        val newMillis = date.timeInMillis
        val currentSelectedCal = Calendar.getInstance().apply {
            timeInMillis = _radarSelectedDateMillis.value
        }
        val isSameDate = isSameDay(currentSelectedCal, date)
        _radarSelectedDateMillis.value = newMillis
        _viewMode.value = CalendarViewMode.DAY
        Log.d("VM", "Radar date updated: $date")
        syncRadarForSelectedDate()
    }

    fun onUsageDateTapped(date: Calendar) {
        val newMillis = date.timeInMillis
        val currentSelectedCal = Calendar.getInstance().apply {
            _usageSelectedDateMillis.value?.let { timeInMillis = it }
        }
        val isSameDate = isSameDay(currentSelectedCal, date)
        _isTappedAgain.value = isSameDate
        _usageSelectedDateMillis.value = newMillis
        _viewMode.value = if (isSameDate) {
            CalendarViewMode.WEEK
        } else {
            CalendarViewMode.DAY
        }
        syncUsageForSelectedDate()
    }

    fun syncUsageForSelectedDate() {
        val selectedMillis = _usageSelectedDateMillis.value ?: return
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }

        val (start, end) = when (_viewMode.value) {
            CalendarViewMode.DAY -> calendar.startEndOfDay()
            CalendarViewMode.WEEK -> calendar.startEndOfWeek()
            CalendarViewMode.MONTH -> calendar.startEndOfMonth()
        }

        val isToday = isSameDay(Calendar.getInstance(), calendar) &&
                _viewMode.value == CalendarViewMode.DAY

        syncAndGetInstalledApps(start, end, isToday)
    }

    fun syncRadarForSelectedDate() {
        val selectedMillis = _radarSelectedDateMillis.value ?: return
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }

        val (start, end) = when (_viewMode.value) {
            CalendarViewMode.DAY -> calendar.startEndOfDay()
            CalendarViewMode.WEEK -> calendar.startEndOfWeek()
            CalendarViewMode.MONTH -> calendar.startEndOfMonth()
        }

        val isToday = isSameDay(Calendar.getInstance(), calendar) &&
                _viewMode.value == CalendarViewMode.DAY

        syncAndGetInstalledApps(start, end, isToday)
    }

    private fun Calendar.startEndOfDay(): Pair<Long, Long> {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        val start = timeInMillis
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        val end = timeInMillis
        return start to end
    }

    private fun Calendar.startEndOfWeek(): Pair<Long, Long> {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = startEndOfDay().first
        add(Calendar.DAY_OF_WEEK, 6)
        val end = startEndOfDay().second
        return start to end
    }

    private fun Calendar.startEndOfMonth(): Pair<Long, Long> {
        set(Calendar.DAY_OF_MONTH, 1)
        val start = startEndOfDay().first
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = startEndOfDay().second
        return start to end
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
                val dailyUsageMap = getAllDailyUsage(context)
                val today = Calendar.getInstance()
                val last7DaysTimestamps = (0..6).map {
                    val cal = today.clone() as Calendar
                    cal.add(Calendar.DAY_OF_YEAR, -it)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.reversed()

                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                labels = last7DaysTimestamps.map { ts -> dateFormat.format(Date(ts)) }

                series = dailyUsageMap.map { (pkg, usageMap) ->
                    val values = last7DaysTimestamps.map { dayTs ->
                        (usageMap[dayTs] ?: 0L) / 60000f
                    }
                    pkg to values
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