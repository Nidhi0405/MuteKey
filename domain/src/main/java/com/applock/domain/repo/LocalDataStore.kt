package com.applock.domain.repo

interface LocalDataStore {
    suspend fun isLoggedIn(): Boolean
    suspend fun setLoggedIn(value: Boolean)
    suspend fun shouldAskPermission(): Boolean
    suspend fun doNotAskPermission(value: Boolean)
    suspend fun toggleAutoStartPermission()
    suspend fun shouldAskAutostartPermission(): Boolean
}