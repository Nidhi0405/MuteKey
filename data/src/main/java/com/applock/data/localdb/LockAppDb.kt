package com.applock.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.applock.data.localdb.dao.ControlledAppDao
import com.applock.data.localdb.dao.InstalledAppDao
import com.applock.data.localdb.entity.InstalledAppInfoEntity

@Database(
    entities = [
        InstalledAppInfoEntity::class,
    ],
    version = 1
)
abstract class LockAppDb : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun controlledAppDao(): ControlledAppDao
}