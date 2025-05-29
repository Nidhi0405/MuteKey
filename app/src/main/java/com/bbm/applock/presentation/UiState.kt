package com.bbm.applock.presentation

sealed interface UiState {
    data object Ideal : UiState
    data object Loading : UiState
    data class Success<T>(val data: T, val message: String) : UiState
    data class Failure<T>(val data: T, val message: String) : UiState
}