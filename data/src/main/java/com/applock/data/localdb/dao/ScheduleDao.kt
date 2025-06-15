package com.applock.data.localdb.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.applock.data.localdb.entity.BlockedAppEntity
import com.applock.data.localdb.entity.DateEntity
import com.applock.data.localdb.entity.ScheduleEntity
import com.applock.data.localdb.entity.ScheduleWithDatesDTO
import com.applock.data.localdb.entity.TimeSlotEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface ScheduleDao {
    @Insert(onConflict = IGNORE)
    suspend fun createSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedule_table")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("UPDATE schedule_table SET isActive = :isActive WHERE id = :scheduleId")
    suspend fun updateActiveStatus(scheduleId: Int, isActive: Boolean)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Transaction
    @Query("SELECT * FROM schedule_table WHERE id = :scheduleId")
    fun getScheduleWithDates(scheduleId: Int): Flow<ScheduleWithDatesDTO>


    @Insert(onConflict = IGNORE)
    suspend fun createDateEntry(date: DateEntity): Long

    @Query("SELECT id FROM date_table WHERE epochDate = :epochDate AND scheduleId = :scheduleId LIMIT 1")
    suspend fun getDateIdIfExists(epochDate: LocalDate, scheduleId: Int): Int?

    @Delete
    suspend fun deleteDateEntry(date: DateEntity)

    @Insert(onConflict = REPLACE)
    suspend fun createTimeSlot(timeSlot: TimeSlotEntity): Long

    @Query("SELECT id FROM time_slot_table WHERE startTime = :startTime AND endTime = :endTime AND dateId = :dateId LIMIT 1")
    suspend fun getTimeSlotIdIfExists(startTime: LocalTime, endTime: LocalTime, dateId: Int): Int?

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlotEntity)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity)

    @Insert(onConflict = IGNORE)
    suspend fun createBlockAppList(list: List<BlockedAppEntity>)


    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM blocked_app_table AS b
            INNER JOIN time_slot_table AS t ON b.timeSlotId = t.id
            INNER JOIN date_table AS d ON t.dateId = d.id
            INNER JOIN schedule_table AS s ON d.scheduleId = s.id
            WHERE b.packageName = :packageName
            AND d.epochDate = :date
            AND :time BETWEEN t.startTime AND t.endTime
            AND s.isActive = 1
        )
        """
    )
    suspend fun isPackageBlocked(packageName: String, date: LocalDate, time: LocalTime): Boolean


    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM time_slot_table AS t
            INNER JOIN date_table AS d ON t.dateId = d.id
            INNER JOIN schedule_table AS s ON d.scheduleId = s.id
            WHERE d.epochDate = :date
            AND :time BETWEEN t.startTime AND t.endTime
            AND s.isActive = 1
        )
        """
    )
    suspend fun hasActiveTimeSlotNow(date: LocalDate, time: LocalTime): Boolean
}