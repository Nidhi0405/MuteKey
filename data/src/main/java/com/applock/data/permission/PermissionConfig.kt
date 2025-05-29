package com.applock.data.permission

import android.os.Build
import com.applock.data.model.PermissionTypeDTO

internal enum class DeviceType {
    XIAOMI,
    OPPO,
    VIVO,
    LETV,
    HONOR,
    ONEPLUS
}

internal data class PermissionConfig(
    val permissionType: PermissionTypeDTO,
    val minApiLevel: Int = Build.VERSION_CODES.BASE,
    val supportedDevices: Set<DeviceType> = emptySet(),
    val isOptional: Boolean = false,
    val isSupportedByDevice: () -> Boolean = { true },
    val accessibilityService: Class<*>? = null
) {
    private fun isRequiredByApi(): Boolean {
        return Build.VERSION.SDK_INT >= this.minApiLevel
    }

    fun isRequired(): Boolean {
        return isRequiredByApi() && isSupportedByDevice()
    }
}

internal class PermissionsConfiguration private constructor(
    val permissionConfigs: List<PermissionConfig>
) {
    class Builder() {
        private val configs: MutableList<PermissionConfig> = mutableListOf()
        fun addPermissionConfig(config: PermissionConfig): Builder {
            configs.add(config)
            return this
        }

        fun build(): PermissionsConfiguration {
            return PermissionsConfiguration(configs.toList())
        }
    }
}
