package com.applock.data.mapper

import com.applock.data.localdb.entity.InstalledAppInfoEntity
import com.applock.domain.model.AppUsageInfo

fun InstalledAppInfoEntity.toDomain(): AppUsageInfo {
    return AppUsageInfo(
        name = name,
        packageName = packageName,
        usageTime = 0L,
        isControlledApp = isControlledApp
    )
}

fun AppUsageInfo.toInstalledAppInfoEntity(): InstalledAppInfoEntity {
    return InstalledAppInfoEntity(
        name = name,
        packageName = packageName,
        isControlledApp = isControlledApp
    )
}