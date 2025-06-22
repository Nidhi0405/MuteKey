package com.bbm.applock.presentation.scheduleModule.vm

import androidx.lifecycle.viewModelScope
import com.applock.core.logE
import com.applock.core.logI
import com.applock.domain.model.Schedule
import com.applock.domain.usecase.CreateScheduleUseCase
import com.applock.domain.usecase.DeleteScheduleUseCase
import com.applock.domain.usecase.GetAllSchedulesUseCase
import com.applock.domain.usecase.ToggleScheduleUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.UiState
import com.bbm.applock.presentation.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesScreenVM @Inject constructor(
    private val dispatcher: CoroutineDispatcherProvider,
    private val getAllSchedulesUseCase: GetAllSchedulesUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val toggleScheduleUseCase: ToggleScheduleUseCase,
) : BaseVM() {

    private val _schedulesList = MutableStateFlow<List<Schedule>>(emptyList())
    val schedulesList: StateFlow<List<Schedule>> = _schedulesList

    private val _isCreateScheduleDialogVisible = MutableStateFlow(false)
    val isCreateScheduleDialogVisible: StateFlow<Boolean> = _isCreateScheduleDialogVisible

    private val _isScheduleSettingsDialogVisible =
        MutableStateFlow<Pair<Boolean, Schedule?>>(false to null)
    val isScheduleSettingsDialogVisible: StateFlow<Pair<Boolean, Schedule?>> =
        _isScheduleSettingsDialogVisible

    private val _scheduleName = MutableStateFlow("")
    val scheduleName: StateFlow<String> = _scheduleName

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

    fun createSchedule() {
        viewModelScope.launch(dispatcher.io) {
            createScheduleUseCase.invoke(_scheduleName.value).fold(
                onSuccess = {
                    "Schedule ${_scheduleName.value} created".logI()
                    _scheduleName.value = ""
                    _isCreateScheduleDialogVisible.emit(false)
                },
                onFailure = {
                    it.stackTraceToString().logE()
                    _state.emit(UiState.Failure(null, it.message.orEmpty()))
                    _isCreateScheduleDialogVisible.emit(true)
                }
            )
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            toggleScheduleUseCase.invoke(schedule).fold(
                onSuccess = {
                    "Schedule ${schedule.name} toggled".logI()
                },
                onFailure = {
                    it.stackTraceToString().logE()
                }
            )
        }
    }

    fun toggleCreateScheduleDialog() {
        _scheduleName.value = ""
        _isCreateScheduleDialogVisible.value = !_isCreateScheduleDialogVisible.value
    }

    fun onNewScheduleNameChange(name: String) {
        _scheduleName.value = name
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch(dispatcher.io) {
            toggleScheduleSettingsDialog(null)
            deleteScheduleUseCase.invoke(schedule).fold(
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