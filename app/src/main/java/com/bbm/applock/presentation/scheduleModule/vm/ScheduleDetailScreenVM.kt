package com.bbm.applock.presentation.scheduleModule.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.applock.domain.model.Schedule
import com.applock.domain.usecase.GetScheduleUseCase
import com.bbm.applock.dispatcher.CoroutineDispatcherProvider
import com.bbm.applock.presentation.base.BaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailScreenVM @Inject constructor(
    private val dispatcher: CoroutineDispatcherProvider,
    private val getSchedule: GetScheduleUseCase,
    savedStateHandle: SavedStateHandle,
    val imageLoader: ImageLoader
) : BaseVM() {
    private val _schedule: MutableStateFlow<Schedule?> = MutableStateFlow(null)
    val schedule: StateFlow<Schedule?> = _schedule

    init {
        val id = savedStateHandle.get<Long>("scheduleId") ?: -1
        viewModelScope.launch(dispatcher.io) {
            getSchedule(scheduleId = id).collect {
                _schedule.value = it
            }
        }
    }
}