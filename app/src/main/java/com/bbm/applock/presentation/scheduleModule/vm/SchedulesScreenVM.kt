package com.bbm.applock.presentation.scheduleModule.vm

import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.core.logE
import com.applock.core.logI
import com.applock.domain.model.Schedule
import com.applock.domain.usecase.DeleteScheduleUseCase
import com.applock.domain.usecase.GetAllSchedulesUseCase
import com.applock.domain.usecase.HasActiveTimeSlotsNow
import com.applock.domain.usecase.ToggleScheduleActiveStatusUseCase
import com.bbm.applock.R
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SchedulesScreenVM @Inject constructor(
    private val dispatcher: CoroutineDispatcherProvider,
    private val getAllSchedulesUseCase: GetAllSchedulesUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val toggleScheduleActiveStatusUseCase: ToggleScheduleActiveStatusUseCase,
    private val hasActiveTimeSlotsNow: HasActiveTimeSlotsNow,
    val imageLoader: ImageLoader,
) : BaseVM() {

    private val _schedulesList = MutableStateFlow<List<Schedule>>(emptyList())
    val schedulesList: StateFlow<List<Schedule>> = _schedulesList

    private val _isScheduleSettingsDialogVisible =
        MutableStateFlow<Pair<Boolean, Schedule?>>(false to null)
    val isScheduleSettingsDialogVisible: StateFlow<Pair<Boolean, Schedule?>> =
        _isScheduleSettingsDialogVisible

    private val _errorAlertMsg = MutableSharedFlow<Int>()
    val errorAlertMsg: SharedFlow<Int> = _errorAlertMsg

    init {
        getAllSchedules()
    }

    private fun getAllSchedules() {
        viewModelScope.launch(dispatcher.io) {
            _state.emit(UiState.Loading)
            getAllSchedulesUseCase.invoke().collect {
                _state.emit(UiState.Success(it, "success"))
                _schedulesList.emit(it)
            }
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            if (schedule.isActive &&
                hasActiveTimeSlotsNow.invoke(
                    scheduleId = schedule.id,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            ) {
                _errorAlertMsg.emit(R.string.can_not_turn_of_active_schedule)
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

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            toggleScheduleSettingsDialog(null)
            if (schedule.isActive
                && hasActiveTimeSlotsNow.invoke(
                    scheduleId = schedule.id,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            ) {
                _errorAlertMsg.emit(R.string.can_not_delete_active_schedule)
                return@launch
            }
            deleteScheduleUseCase.invoke(schedule.id).fold(
                onSuccess = {
                    "Schedule ${schedule.name} toggled".logI()
                },
                onFailure = {
                    it.stackTraceToString().logE()
                }
            )
        }
    }

    fun toggleScheduleSettingsDialog(schedule: Schedule?) {
        if (!_isScheduleSettingsDialogVisible.value.first) {
            _isScheduleSettingsDialogVisible.value = true to schedule
        } else {
            _isScheduleSettingsDialogVisible.value = false to null
        }
    }
}