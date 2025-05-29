package com.applock.data.repo

import android.content.Context
import android.os.Build
import com.applock.data.mapper.toDomain
import com.applock.data.model.PermissionTypeDTO
import com.applock.data.permission.DeviceType
import com.applock.data.permission.PermissionConfig
import com.applock.data.permission.PermissionsClient
import com.applock.data.permission.PermissionsConfiguration
import com.applock.domain.model.PermissionInfo
import com.applock.domain.repo.PermissionRepo
import javax.inject.Inject

class PermissionRepoImpl @Inject internal constructor(
    private val context: Context,
    private val permissionsClient: PermissionsClient
) : PermissionRepo {
    override suspend fun checkPermission(accessibilityService: Class<*>): PermissionInfo {
        val autoStartDevices = setOf(
            DeviceType.XIAOMI,
            DeviceType.OPPO,
            DeviceType.VIVO,
            DeviceType.LETV,
            DeviceType.HONOR,
            DeviceType.ONEPLUS
        )

        val config = PermissionsConfiguration.Builder()
            .addPermissionConfig(PermissionConfig(permissionType = PermissionTypeDTO.USAGE))
            .addPermissionConfig(
                PermissionConfig(
                    permissionType = PermissionTypeDTO.ACCESSIBILITY,
                    accessibilityService = accessibilityService
                )
            )
            .addPermissionConfig(
                PermissionConfig(
                    permissionType = PermissionTypeDTO.AUTO_START,
                    supportedDevices = autoStartDevices,
                    isOptional = true,
                    isSupportedByDevice = {
                        permissionsClient.isAutoStartSupportedByDevice(
                            devices = autoStartDevices,
                            context = context
                        )
                    }
                )
            )
            .addPermissionConfig(
                PermissionConfig(
                    permissionType = PermissionTypeDTO.NOTIFICATIONS,
                    minApiLevel = Build.VERSION_CODES.TIRAMISU
                )
            ).build()

        return permissionsClient.getPermissions(config).toDomain()
    }

    override suspend fun toggleAutoStart() {
        permissionsClient.toggleAutoStartPermission()
    }

    override suspend fun doNotAskPermission(value: Boolean) {
        permissionsClient.doNotAskMePermissions(value)
    }
}