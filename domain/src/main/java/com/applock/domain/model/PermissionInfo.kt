package com.applock.domain.model

data class PermissionInfo(
    val permissions: List<Permission>,
    val allGranted: Boolean,
    val shouldAskPermission: Boolean
) {
    data class Permission(
        val permissionType: PermissionType,
        val intent: PermissionComponent?,
        val isGranted: Boolean,
        val isOptional: Boolean
    )

    data class PermissionComponent(
        val packageName: String,
        val className: String
    )

    enum class PermissionType(val permissionName: String) {
        NOTIFICATION("Notification Permission"),
        USAGE("Usage Permission"),
        ACCESSIBILITY("Accessibility Permission"),
        AUTO_START("Auto Start Permission"),
        READ_NOTIFICATION("Notification Block Permission")
    }
}