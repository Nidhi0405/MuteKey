package com.bbm.applock.presentation.scheduleModule.vm

import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.core.logE
import com.applock.core.logI
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.model.Schedule
import com.applock.domain.usecase.CreateScheduleUseCase
import com.applock.domain.usecase.DeleteScheduleUseCase
import com.applock.domain.usecase.GetControlledAppsUseCase
import com.applock.domain.usecase.GetScheduleUseCase
import com.applock.domain.usecase.HasActiveTimeSlotsNow
import com.applock.domain.usecase.ToggleScheduleActiveStatusUseCase
import com.applock.domain.usecase.UpdateScheduleUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CreateOrUpdateScheduleVM @Inject constructor(
    private val dispatcher: CoroutineDispatcherProvider,
    private val getControlledAppsUseCase: GetControlledAppsUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val getSchedule: GetScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val hasActiveTimeSlotsNow: HasActiveTimeSlotsNow,
    private val toggleScheduleActiveStatusUseCase: ToggleScheduleActiveStatusUseCase,
    val imageLoader: ImageLoader,
) : BaseVM() {
    private val _controlledApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val controlledApps: StateFlow<List<AppUsageInfo>> = _controlledApps

    data object DeleteSuccess

    init {
        viewModelScope.launch(dispatcher.io) {
            val apps = getControlledAppsUseCase.invoke()
            _controlledApps.value = apps
        }
    }

    private val _selectedTimeSlot = MutableStateFlow(
        LocalTime.now() to LocalTime.now().plusHours(1)
    )
    val selectedTimeSlot: StateFlow<Pair<LocalTime, LocalTime>> = _selectedTimeSlot
    fun onSelectTimeSlot(start: LocalTime, end: LocalTime) {
        _selectedTimeSlot.value = start to end
    }

    private val _selectedControlledApps =
        MutableStateFlow<LinkedHashSet<AppUsageInfo>>(LinkedHashSet())
    val selectedControlledApps: StateFlow<Set<AppUsageInfo>> = _selectedControlledApps
    fun onAppSelectToggleClick(app: AppUsageInfo) {
        _selectedControlledApps.update { current ->
            val updated = LinkedHashSet(current)
            if (!updated.add(app)) {
                updated.remove(app)
            }
            updated
        }
    }

    private val _selectedDays =
        MutableStateFlow<LinkedHashSet<DayOfWeek>>(LinkedHashSet())
    val selectedDays: StateFlow<Set<DayOfWeek>> = _selectedDays
    fun onDaySelectToggleClick(day: DayOfWeek) {
        _selectedDays.update { current ->
            val updated = LinkedHashSet(current)
            if (!updated.add(day)) {
                updated.remove(day)
            }
            updated
        }
    }


    private val _scheduleName = MutableStateFlow("")
    val scheduleName: StateFlow<String> = _scheduleName
    fun onScheduleNameChange(name: String) {
        _scheduleName.value = name
    }

    private val _isCreateScheduleDialogVisible = MutableStateFlow(false)
    val isCreateScheduleDialogVisible: StateFlow<Boolean> = _isCreateScheduleDialogVisible

    fun toggleCreateScheduleDialog() {
        _isCreateScheduleDialogVisible.value = !_isCreateScheduleDialogVisible.value
    }

    fun createSchedule() {
        if (!validate()) return
        val selectedApps = _selectedControlledApps.value
        val selectedDays = _selectedDays.value
        val selectedTImeSlot = _selectedTimeSlot.value
        val scheduleName = _scheduleName.value
        viewModelScope.launch(dispatcher.io) {
            createScheduleUseCase.invoke(
                Schedule(
                    name = scheduleName,
                    apps = selectedApps.map {
                        Schedule.App(
                            appId = it.packageName,
                            appName = it.name
                        )
                    },
                    repeatDays = selectedDays.toList(),
                    startTime = selectedTImeSlot.first,
                    endTime = selectedTImeSlot.second,
                    isActive = false
                )
            ).fold(
                onSuccess = {
                    clear()
                    _isCreateScheduleDialogVisible.emit(false)
                },
                onFailure = {
                    it.stackTraceToString().logE()
                    _isCreateScheduleDialogVisible.emit(true)
                    _state.emit(UiState.Failure(null, it.message.orEmpty()))
                }
            )
        }
    }

    fun fillUpdateScheduleFields(schedule: Schedule) {
        _scheduleName.value = schedule.name
        _selectedTimeSlot.value = schedule.startTime to schedule.endTime
        val selectedAppsId = schedule.apps.map { it.appId }.toSet()
        _selectedControlledApps.value = LinkedHashSet(
            controlledApps.value.filter {
                selectedAppsId.contains(it.packageName)
            }
        )
        _selectedDays.value = LinkedHashSet(schedule.repeatDays)
    }

    fun updateSchedule(schedule: Schedule) {
        if (!validate()) return
        val selectedApps = _selectedControlledApps.value
        val selectedDays = _selectedDays.value
        val selectedTImeSlot = _selectedTimeSlot.value
        val scheduleName = _scheduleName.value
        viewModelScope.launch(dispatcher.io) {
            if (hasActiveTimeSlotsNow.invoke(
                    scheduleId = schedule.id,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            ) {
                _state.emit(UiState.Failure(null, "Can not update active schedule!"))
                _isCreateScheduleDialogVisible.emit(false)
                clear()
                return@launch
            }
            updateScheduleUseCase.invoke(
                Schedule(
                    id = schedule.id,
                    name = scheduleName,
                    apps = selectedApps.map {
                        Schedule.App(
                            appId = it.packageName,
                            appName = it.name
                        )
                    },
                    repeatDays = selectedDays.toList(),
                    startTime = selectedTImeSlot.first,
                    endTime = selectedTImeSlot.second,
                    isActive = schedule.isActive
                )
            ).fold(
                onSuccess = {
                    clear()
                    _isCreateScheduleDialogVisible.emit(false)
                },
                onFailure = {
                    it.stackTraceToString().logE()
                    _isCreateScheduleDialogVisible.emit(true)
                    _state.emit(UiState.Failure(null, it.message.orEmpty()))
                }
            )
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            if (hasActiveTimeSlotsNow.invoke(
                    scheduleId = schedule.id,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            ) {
                _state.emit(UiState.Failure(null, "Can not delete active schedule!"))
                _isCreateScheduleDialogVisible.emit(false)
                return@launch
            }
            deleteScheduleUseCase.invoke(schedule.id).fold(
                onSuccess = {
                    _state.emit(UiState.Success(DeleteSuccess, "Delete success"))
                    clear()
                    _isCreateScheduleDialogVisible.emit(false)
                },
                onFailure = {
                    it.stackTraceToString().logE()
                    _isCreateScheduleDialogVisible.emit(true)
                    _state.emit(UiState.Failure(null, it.message.orEmpty()))
                }
            )
        }
    }

    fun toggleActiveStatusSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            if (schedule.isActive &&
                hasActiveTimeSlotsNow.invoke(
                    scheduleId = schedule.id,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            ) {
                _state.emit(
                    UiState.Failure(
                        null,
                        "Cannot turn off schedule while it has active time slots."
                    )
                )
                return@launch
            }
            toggleScheduleActiveStatusUseCase.invoke(schedule).fold(
                onSuccess = {
                    "Schedule ${schedule.name} toggled".logI()
                },
                onFailure = {
                    it.stackTraceToString().logE()
                }
            )
        }
    }

    private fun validate(): Boolean {
        if (_scheduleName.value.isEmpty() || _scheduleName.value.length < 3) return false
        if (_selectedDays.value.isEmpty()) return false
        if (_selectedControlledApps.value.isEmpty()) return false

        return true
    }

    fun clear() {
        _selectedTimeSlot.value = LocalTime.now() to LocalTime.now().plusHours(1)
        _selectedControlledApps.value = LinkedHashSet()
        _selectedDays.value = LinkedHashSet()
        _scheduleName.value = ""
    }
}