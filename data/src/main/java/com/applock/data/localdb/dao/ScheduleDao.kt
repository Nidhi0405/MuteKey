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

    @Delete
    suspend fun deleteDateEntry(date: DateEntity)

    @Insert(onConflict = REPLACE)
    suspend fun createTimeSlot(timeSlot: TimeSlotEntity): Long

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlotEntity)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity)

    @Insert(onConflict = IGNORE)
    suspend fun createBlockAppList(list: List<BlockedAppEntity>)
}