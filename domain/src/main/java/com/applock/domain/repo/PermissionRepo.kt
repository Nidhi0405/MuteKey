package com.applock.domain.repo

import com.applock.domain.model.PermissionInfo

interface PermissionRepo {
    suspend fun checkPermission(accessibilityService: Class<*>): PermissionInfo
    suspend fun toggleAutoStart()
    suspend fun doNotAskPermission(value: Boolean)
}