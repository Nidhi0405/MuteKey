package com.bbm.applock.presentation.scheduleModule.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.core.logE
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.Schedule
import com.applock.domain.model.ScheduleWithDates
import com.applock.domain.usecase.CreateTimeSlotUseCase
import com.applock.domain.usecase.GetControlledAppsUseCase
import com.applock.domain.usecase.GetScheduleWithDatesUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.base.BaseVM
import com.bbm.applock.util.CalenderViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailScreenVM @Inject constructor(
    private val dispatcher: CoroutineDispatcherProvider,
    private val getControlledAppsUseCase: GetControlledAppsUseCase,
    private val createTimeSlotUseCase: CreateTimeSlotUseCase,
    private val getScheduleWithDatesUseCase: GetScheduleWithDatesUseCase,
    private val savedStateHandle: SavedStateHandle,
    val imageLoader: ImageLoader
) : BaseVM() {
    private val schedule = Schedule(
        id = savedStateHandle.get<Int>("id") ?: 0,
        name = savedStateHandle.get<String>("name") ?: "",
        isActive = savedStateHandle.get<Boolean>("isActive") == true,
    )
    private val scheduleWithDates by lazy { getScheduleWithDatesUseCase(schedule.id) }

    val currentMonth by lazy { YearMonth.now()!! }
    val startMonth by lazy { currentMonth.minusMonths(50)!! }
    val endMonth by lazy { currentMonth.plusMonths(50)!! }

    val currentDate by lazy { LocalDate.now()!! }
    val startDate by lazy { currentDate.minusDays(100)!! }
    val endDate by lazy { currentDate.plusDays(100)!! }

    private val _selectedDay = MutableStateFlow<LocalDate>(currentDate)
    val selectedDay: StateFlow<LocalDate> = _selectedDay

    private val _calenderViewType = MutableStateFlow(CalenderViewType.WEEKLY)
    val calenderViewType: StateFlow<CalenderViewType> = _calenderViewType

    private val _controlledApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val controlledApps: StateFlow<List<AppUsageInfo>> = _controlledApps

    private val _selectedControlledApps =
        MutableStateFlow<LinkedHashSet<AppUsageInfo>>(LinkedHashSet())
    val selectedControlledApps: StateFlow<Set<AppUsageInfo>> = _selectedControlledApps

    private val _isTimeSlotDialogVisible = MutableStateFlow(false)
    val isTimeSlotDialogVisible: StateFlow<Boolean> = _isTimeSlotDialogVisible
    private val _selectedTimeSlot = MutableStateFlow<Pair<LocalTime, LocalTime>>(
        LocalTime.now() to LocalTime.now().plusHours(1)
    )
    val selectedTimeSlot: StateFlow<Pair<LocalTime, LocalTime>> = _selectedTimeSlot

    val scheduleDatesMap: StateFlow<Map<LocalDate, ScheduleWithDates.DateItem>> =
        scheduleWithDates
            .map { swd ->
                swd.dates.associateBy {
                    it.date.date.also {
                        "$it".logE()
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(dispatcher.io)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    val currentSelectedDateData = selectedDay
        .combine(scheduleDatesMap) { day, map ->
            map[day].also {
                "$it".logE()
            }
        }
        .distinctUntilChanged()
        .flowOn(dispatcher.io)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val totalBlockedDuration: StateFlow<Duration> = currentSelectedDateData
        .map { dateItem ->
            dateItem?.timeSlots?.map { it.timeSlot }?.let { timeSlots ->
                calculateTotalBlockedHours(timeSlots)
            } ?: Duration.ZERO
        }
        .distinctUntilChanged()
        .flowOn(dispatcher.io)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Duration.ZERO)

    init {
        viewModelScope.launch(dispatcher.io) {
            val apps = getControlledAppsUseCase.invoke()
            _controlledApps.value = apps
        }
    }

    fun onChangeCalenderViewType(type: CalenderViewType) {
        _calenderViewType.value = type
    }

    fun onSelectedDayChanged(date: LocalDate) {
        _selectedDay.value = date
    }

    fun createTimeSlots() {
        viewModelScope.launch(dispatcher.io) {
            val dateInput = Schedule.DateInput(
                date = _selectedDay.value,
                scheduleId = schedule.id,
            )
            val timeSlotsInput = Schedule.DateInput.TimeSlotsInput(
                start = _selectedTimeSlot.value.first,
                end = _selectedTimeSlot.value.second,
            )
            val blockedAppsInput = _selectedControlledApps.value.map { app ->
                Schedule.DateInput.TimeSlotsInput.BlockedAppsInput(
                    id = 0,
                    name = app.name,
                    packageName = app.packageName,
                    timeSlotId = 0,
                )
            }
            clearNewTimeSlotData()
            createTimeSlotUseCase.invoke(dateInput, timeSlotsInput, blockedAppsInput)
        }
    }

    fun onTimeSlotDialogToggle() {
        _isTimeSlotDialogVisible.value = !_isTimeSlotDialogVisible.value
    }

    fun onSelectTimeSlot(start: LocalTime, end: LocalTime) {
        _selectedTimeSlot.value = start to end
    }

    fun onAppSelectToggleClick(app: AppUsageInfo) {
        _selectedControlledApps.update { current ->
            val updated = LinkedHashSet(current)
            if (!updated.add(app)) {
                updated.remove(app)
            }
            updated
        }
    }

    fun clearNewTimeSlotData() {
        _selectedControlledApps.update { LinkedHashSet() }
        _selectedTimeSlot.value = LocalTime.now() to LocalTime.now().plusHours(1)
        _isTimeSlotDialogVisible.value = false
    }

    private fun calculateTotalBlockedHours(timeSlots: List<Schedule.DateInput.TimeSlotsInput>): Duration {
        if (timeSlots.isEmpty()) return Duration.ZERO

        val sorted = timeSlots.sortedBy { it.start }
        val merged = mutableListOf<Pair<LocalTime, LocalTime>>()

        var currentStart = sorted.first().start
        var currentEnd = sorted.first().end

        for (i in 1 until sorted.size) {
            val slot = sorted[i]
            if (slot.start <= currentEnd) {
                currentEnd = maxOf(currentEnd, slot.end)
            } else {
                merged.add(currentStart to currentEnd)
                currentStart = slot.start
                currentEnd = slot.end
            }
        }
        merged.add(currentStart to currentEnd)

        return merged.fold(Duration.ZERO) { acc, (start, end) ->
            acc + Duration.between(start, end)
        }
    }

}