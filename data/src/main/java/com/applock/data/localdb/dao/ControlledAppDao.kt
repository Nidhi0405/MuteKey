package com.applock.data.localdb.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.applock.data.localdb.entity.InstalledAppInfoEntity

@Dao
interface ControlledAppDao {
    @Query("SELECT * FROM InstalledAppInfoEntity WHERE isControlledApp = 1")
    suspend fun getControlledApps(): List<InstalledAppInfoEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertControlledApp(app: InstalledAppInfoEntity)

    @Delete
    suspend fun deleteControlledApp(app: InstalledAppInfoEntity)
}