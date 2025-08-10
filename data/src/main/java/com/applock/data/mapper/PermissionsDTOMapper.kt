package com.applock.data.mapper

import com.applock.data.model.PermissionComponentDTO
import com.applock.data.model.PermissionDTO
import com.applock.data.model.PermissionTypeDTO
import com.applock.data.model.PermissionsDTO
import com.applock.domain.model.PermissionInfo
import com.applock.domain.model.PermissionInfo.PermissionComponent

fun PermissionsDTO.toDomain(): PermissionInfo {
    return PermissionInfo(
        permissions = permissions.map { it.toDomain() },
        allGranted = allGranted,
        shouldAskPermission = shouldAskPermission
    )
}

fun PermissionDTO.toDomain(): PermissionInfo.Permission {
    return PermissionInfo.Permission(
        permissionType = permissionType.toDomain(),
        intent = intent?.toDomain(),
        isGranted = granted,
        isOptional = isOptional
    )
}

fun PermissionTypeDTO.toDomain(): PermissionInfo.PermissionType {
    return when (this) {
        PermissionTypeDTO.USAGE -> PermissionInfo.PermissionType.USAGE
        PermissionTypeDTO.ACCESSIBILITY -> PermissionInfo.PermissionType.ACCESSIBILITY
        PermissionTypeDTO.AUTO_START -> PermissionInfo.PermissionType.AUTO_START
        PermissionTypeDTO.POST_NOTIFICATIONS -> PermissionInfo.PermissionType.NOTIFICATION
        PermissionTypeDTO.READ_NOTIFICATION -> PermissionInfo.PermissionType.READ_NOTIFICATION
    }
}

fun PermissionComponentDTO.toDomain(): PermissionComponent {
    return PermissionComponent(
        packageName = packageName,
        className = className
    )
}