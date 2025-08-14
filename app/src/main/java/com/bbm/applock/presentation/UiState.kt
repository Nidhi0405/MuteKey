package com.bbm.applock.presentation

import androidx.annotation.StringRes
import java.util.UUID

sealed class UiState {
    var hasBeenHandled = false
    fun <S : UiState> S.consumeOnce(): S? =
        if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            this
        }

    data object Ideal : UiState()

    data object Loading : UiState()

    data class Success<T>(val data: T, val message: String) : UiState()

    data class ValidationError(
        @StringRes val message: Int,
        val id: String = UUID.randomUUID().toString()
    ) : UiState()

    data class Failure<T>(
        val data: T,
        val message: String,
        val id: String = UUID.randomUUID().toString()
    ) : UiState()
}