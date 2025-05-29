package com.applock.data.model


data class PermissionsDTO(
    val permissions: List<PermissionDTO>,
    val shouldAskPermission: Boolean,
    val allGranted: Boolean
)

data class PermissionDTO(
    val permissionType: PermissionTypeDTO,
    val intent: PermissionComponentDTO? = null,
    val granted: Boolean,
    val isOptional: Boolean
)

data class PermissionComponentDTO(
    val packageName: String,
    val className: String
)

enum class PermissionTypeDTO {
    USAGE,
    ACCESSIBILITY,
    AUTO_START,
    NOTIFICATIONS;
}

