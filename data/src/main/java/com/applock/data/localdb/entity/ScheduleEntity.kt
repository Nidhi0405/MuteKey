package com.applock.data.localdb.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(tableName = "schedule_table")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = false
)

@Entity(
    tableName = "date_table",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class DateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scheduleId: Int,
    val epochDate: Long
)

@Entity(
    tableName = "time_slot_table",
    foreignKeys = [
        ForeignKey(
            entity = DateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ],
    indices = [Index("dateId")]
)
data class TimeSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val dateId: Int
)

@Entity(
    tableName = "blocked_app_table",
    foreignKeys = [
        ForeignKey(
            entity = TimeSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeSlotId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ],
    indices = [Index("timeSlotId")]
)
data class BlockedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val packageName: String,
    val timeSlotId: Int
)


/**
 * Mapper classes for Query
 * */
data class BlockedAppItem(
    val id: Int,
    val name: String,
    val packageName: String,
    val timeSlotId: Int
)

data class TimeSlotItem(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val dateId: Int,
    @Relation(
        parentColumn = "id",
        entityColumn = "timeSlotId"
    )
    val blockedApps: List<BlockedAppEntity>
)

data class DateItem(
    val id: Int,
    val scheduleId: Int,
    val epochDate: Long,
    @Relation(
        parentColumn = "id",
        entityColumn = "dateId",
        entity = TimeSlotEntity::class
    )
    val timeSlots: List<TimeSlotItem>
)

data class ScheduleWithDates(
    @Embedded
    val schedule: ScheduleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId",
        entity = DateEntity::class
    )
    val dates: List<DateItem>
)