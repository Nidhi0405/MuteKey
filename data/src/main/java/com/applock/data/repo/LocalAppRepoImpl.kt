package com.applock.data.repo

import com.applock.data.localdb.dao.ControlledAppDao
import com.applock.data.localdb.dao.InstalledAppDao
import com.applock.data.mapper.toDomain
import com.applock.data.mapper.toInstalledAppInfoEntity
import com.applock.domain.model.AppUsageInfo
import com.applock.domain.repo.LocalAppRepo
import javax.inject.Inject

class LocalAppRepoImpl @Inject constructor(
    private val dao: InstalledAppDao,
    private val controlledAppDao: ControlledAppDao
) : LocalAppRepo {
    override suspend fun getStoredApp(): List<AppUsageInfo> {
        return dao.getStoredApp().map { it.toDomain() }
    }

    override suspend fun storeInstalledApps(apps: List<AppUsageInfo>) {
        dao.storeInstalledApps(apps = apps.map { it.toInstalledAppInfoEntity() })
    }

    override suspend fun deleteUninstalledApps(apps: List<AppUsageInfo>) {
        dao.deleteUninstalledApps(packages = apps.map { it.packageName })
    }

    override suspend fun getControlledApps(): List<AppUsageInfo> {
        return controlledAppDao.getControlledApps().map { it.toDomain() }
    }

    override suspend fun storeControlledApps(appUsageInfo: AppUsageInfo) {
        controlledAppDao.insertControlledApp(app = appUsageInfo.toInstalledAppInfoEntity())
    }

    override suspend fun deleteControlledApp(app: AppUsageInfo) {
        controlledAppDao.deleteControlledApp(app = app.toInstalledAppInfoEntity())
    }
}