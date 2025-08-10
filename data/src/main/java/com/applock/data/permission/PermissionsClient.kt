package com.applock.data.permission

import android.content.Context
import com.applock.data.model.PermissionDTO
import com.applock.data.model.PermissionTypeDTO
import com.applock.data.model.PermissionsDTO
import com.applock.domain.repo.LocalDataStore
import javax.inject.Inject

internal class PermissionsClient @Inject internal constructor(
    private val preference: LocalDataStore,
    private val appContext: Context,
    private val autoStartPermissionChecker: AutoStartPermission,
    private val notificationPermissionChecker: NotificationPermission,
    private val usagePermission: UsagePermission,
    private val accessibilityPermission: AccessibilityPermission,
) {
    suspend fun getPermissions(config: PermissionsConfiguration): PermissionsDTO {
        val permissions = config.permissionConfigs
            .filter { it.isRequired() }
            .map { permissionConfig ->
                val permissionDTO = when (val permissionType = permissionConfig.permissionType) {
                    PermissionTypeDTO.AUTO_START -> PermissionDTO(
                        permissionType = permissionType,
                        granted = autoStartPermissionChecker.hasPermission(),
                        intent = autoStartPermissionChecker.getComponent(),
                        isOptional = permissionConfig.isOptional
                    )

                    PermissionTypeDTO.POST_NOTIFICATIONS -> PermissionDTO(
                        permissionType = permissionType,
                        granted = notificationPermissionChecker.hasPermission(appContext),
                        isOptional = permissionConfig.isOptional
                    )

                    PermissionTypeDTO.USAGE -> PermissionDTO(
                        permissionType = permissionType,
                        granted = usagePermission.hasPermission(appContext),
                        isOptional = false,
                    )

                    PermissionTypeDTO.ACCESSIBILITY -> PermissionDTO(
                        permissionType = permissionType,
                        granted = accessibilityPermission.hasPermission(
                            appContext,
                            permissionConfig.accessibilityService!!
                        ),
                        isOptional = false,
                    )

                    PermissionTypeDTO.READ_NOTIFICATION -> PermissionDTO(
                        permissionType = permissionType,
                        granted = notificationPermissionChecker.isNotificationReadServiceEnabled(
                            appContext
                        ),
                        isOptional = false,
                    )
                }

                permissionDTO
            }

        val allGranted = permissions.filterNot { it.isOptional }.all { it.granted }

        val permissionsDTO = PermissionsDTO(
            permissions = permissions,
            shouldAskPermission = shouldAskPermissions(),
            allGranted = allGranted
        )

        return permissionsDTO
    }

    suspend fun doNotAskMePermissions(value: Boolean) {
        preference.doNotAskPermission(value)
    }

    private suspend fun shouldAskPermissions(): Boolean {
        return preference.shouldAskPermission()
    }

    suspend fun toggleAutoStartPermission() {
        autoStartPermissionChecker.togglePermission()
    }

    fun isAutoStartSupportedByDevice(devices: Set<DeviceType>, context: Context): Boolean {
        return autoStartPermissionChecker.isSupportedByDevice(devices, context)
    }

}