package com.applock.data.localdb.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val repeatDays: List<DayOfWeek>?,
    val isActive: Boolean = false,
    val isOneTime: Boolean = false,
    val scheduledDate: LocalDate? = null // only used for one-time schedules
)

@Entity(
    tableName = "apps",
    foreignKeys = [ForeignKey(
        entity = ScheduleEntity::class,
        parentColumns = ["id"],
        childColumns = ["scheduleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("scheduleId")]
)
data class AppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val appId: String,
    val appName: String
)


data class ScheduleWithApps(
    @Embedded val schedule: ScheduleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId"
    )
    val apps: List<AppEntity>
)