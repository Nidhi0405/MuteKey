package com.applock.domain.util


inline fun <T> result(block: () -> T): Result<T> {
    return try {
        val result = block()
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}