package com.applock.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.applock.data.localdb.dao.ControlledAppDao
import com.applock.data.localdb.dao.InstalledAppDao
import com.applock.data.localdb.dao.ScheduleDao
import com.applock.data.localdb.entity.BlockedAppEntity
import com.applock.data.localdb.entity.DateEntity
import com.applock.data.localdb.entity.InstalledAppInfoEntity
import com.applock.data.localdb.entity.ScheduleEntity
import com.applock.data.localdb.entity.TimeSlotEntity

@Database(
    entities = [
        InstalledAppInfoEntity::class,
        ScheduleEntity::class,
        DateEntity::class,
        TimeSlotEntity::class,
        BlockedAppEntity::class
    ],
    version = 1
)
abstract class LockAppDb : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun controlledAppDao(): ControlledAppDao
    abstract fun scheduleDao(): ScheduleDao
}