package com.bbm.applock.presentation.base

import androidx.lifecycle.ViewModel
import com.bbm.applock.presentation.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseVM : ViewModel() {
    protected val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState.Ideal)
    val state: StateFlow<UiState> = _state
}