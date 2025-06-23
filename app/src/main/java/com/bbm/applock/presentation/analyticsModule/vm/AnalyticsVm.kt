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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    var installedApps : StateFlow<List<AppUsageInfo>> = _installedApps
    val chartJson = MutableStateFlow("")
    private val _hourlyUsageMap = MutableStateFlow<Map<String, List<Long>>>(emptyMap())
    val hourlyUsageMap: StateFlow<Map<String, List<Long>>> get() = _hourlyUsageMap

    fun syncAndGetInstalledApps(days: Int) {
        viewModelScope.launch(dispatchers.io) {
            _state.emit(UiState.Loading)
            val list = syncInstalledAppsUseCase.invoke(days)
            _installedApps.value = list
            _state.emit(UiState.Success(list, "success"))
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
                Log.e("TAG", "fetchAndGenerateChartJson: $dailyUsageDataMap")
                for ((packageName, usageList) in dailyUsageDataMap) {
                    val usageDataPoints = JSONArray()
                    val paddedUsageList = when {
                        usageList.size < numberOfDays -> usageList.toMutableList()
                            .apply { repeat(numberOfDays - usageList.size) { add(0L) } }.toList()

                        usageList.size > numberOfDays -> usageList.takeLast(numberOfDays)
                        else -> usageList
                    }
                    for (usageMillis in paddedUsageList) {
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
                chartJson.value = root.toString()
                Log.d("AnalyticsVm", "Generated Chart JSON (Multi-Day): ${chartJson.value}")
                _state.emit(UiState.Success(null, "Chart data generated successfully"))
            } catch (e: Exception) {
                chartJson.value = ""
                e.printStackTrace()
                Log.e("AnalyticsVm", "Error in fetchAndGenerateChartJson: ${e.message}", e)
            }
        }
    }

    fun syncHourlyUsage(days: Int) {
        viewModelScope.launch(dispatchers.io) {
            val map = getHourlyUsageMapUseCase.invoke(days)
            _hourlyUsageMap.value = map
        }
    }
}