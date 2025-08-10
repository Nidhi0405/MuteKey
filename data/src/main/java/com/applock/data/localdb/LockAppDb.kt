package com.applock.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.applock.data.localdb.dao.ControlledAppDao
import com.applock.data.localdb.dao.InstalledAppDao
import com.applock.data.localdb.dao.ScheduleDao
import com.applock.data.localdb.entity.AppEntity
import com.applock.data.localdb.entity.InstalledAppInfoEntity
import com.applock.data.localdb.entity.ScheduleEntity

@Database(
    entities = [
        InstalledAppInfoEntity::class,
        ScheduleEntity::class,
        AppEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class LockAppDb : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun controlledAppDao(): ControlledAppDao
    abstract fun scheduleDao(): ScheduleDao
}