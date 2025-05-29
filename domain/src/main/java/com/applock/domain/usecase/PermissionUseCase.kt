package com.applock.domain.usecase

import com.applock.domain.repo.PermissionRepo
import javax.inject.Inject

class PermissionUseCase @Inject constructor(
    val permissionRepository: PermissionRepo
) {
    suspend fun checkPermission(accessibilityService: Class<*>) =
        permissionRepository.checkPermission(accessibilityService)
}