package com.bbm.applock.presentation

import androidx.annotation.StringRes

sealed interface UiState {
    data object Ideal : UiState
    data object Loading : UiState
    data class Success<T>(val data: T, val message: String) : UiState
    data class ValidationError(@StringRes val message: Int) : UiState
    data class Failure<T>(val data: T, val message: String) : UiState
}