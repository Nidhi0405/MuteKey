package com.applock.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.applock.data.localdb.entity.InstalledAppInfoEntity

@Dao
interface InstalledAppDao {

    @Query("SELECT * FROM InstalledAppInfoEntity")
    suspend fun getStoredApp(): List<InstalledAppInfoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun storeInstalledApps(apps: List<InstalledAppInfoEntity>)

    @Query("DELETE FROM InstalledAppInfoEntity WHERE packageName IN (:packages)")
    suspend fun deleteUninstalledApps(packages: List<String>)
}