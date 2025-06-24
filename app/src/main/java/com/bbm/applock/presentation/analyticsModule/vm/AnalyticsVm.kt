package com.bbm.applock.presentation.analyticsModule.vm

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
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
}