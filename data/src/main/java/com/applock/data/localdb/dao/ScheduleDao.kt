package com.applock.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.applock.data.localdb.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = IGNORE)
    suspend fun createSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedule_table")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("UPDATE schedule_table SET isActive = :isActive WHERE id = :scheduleId")
    suspend fun updateActiveStatus(scheduleId: Int, isActive: Boolean)
    /*
        @Transaction
        @Query("SELECT * FROM schedule_table WHERE id = :scheduleId")
        fun observeScheduleWithDetails(scheduleId: Int): Flow<ScheduleWithDates>*/
}