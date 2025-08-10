package com.applock.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.applock.data.localdb.entity.AppEntity
import com.applock.data.localdb.entity.ScheduleEntity
import com.applock.data.localdb.entity.ScheduleWithApps
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Dao
interface ScheduleDao {
    // CREATE
    @Insert
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert
    suspend fun insertApps(apps: List<AppEntity>)

    // READ
    @Transaction
    @Query("SELECT * FROM schedules")
    fun getAllSchedules(): Flow<List<ScheduleWithApps>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE id = :id")
    fun getScheduleById(id: Long): Flow<ScheduleWithApps?>

    // UPDATE
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Update
    suspend fun updateApps(apps: List<AppEntity>)

    // DELETE
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Long)

    @Query("DELETE FROM apps WHERE scheduleId = :scheduleId")
    suspend fun deleteAppsByScheduleId(scheduleId: Long)

    // CHANGE ACTIVE STATUS
    @Query("UPDATE schedules SET isActive = :isActive WHERE id = :scheduleId")
    suspend fun updateScheduleActiveStatus(scheduleId: Long, isActive: Boolean)

    @Query(
        value = """
        SELECT COUNT(*) FROM schedules s
        INNER JOIN apps a ON s.id = a.scheduleId
        WHERE s.isActive = 1
            AND ((s.startTime <= s.endTime AND :currentTime BETWEEN s.startTime AND s.endTime)
                    OR
                    (s.startTime > s.endTime AND (:currentTime >= s.startTime OR :currentTime <= s.endTime)))
            AND a.appId = :appPackage
        """
    )
    suspend fun isAppRestrictedNowWithActiveSchedule(
        currentTime: Long,
        appPackage: String,
    ): Int

    /*@Query(
        """
        SELECT COUNT(*) FROM schedules s
        INNER JOIN apps a ON s.id = a.scheduleId
        WHERE s.id = :scheduleId
            AND s.isActive = 1
            AND ((s.startTime <= s.endTime AND :currentTime BETWEEN s.startTime AND s.endTime)
                    OR
                    (s.startTime > s.endTime AND (:currentTime >= s.startTime OR :currentTime <= s.endTime)))
        """
    )
    suspend fun isScheduleActiveAndRunning(
        scheduleId: Long,
        currentTime: LocalTime,
    ): Int*/
    @Query(
        """
    SELECT s.* FROM schedules s
    INNER JOIN apps a ON s.id = a.scheduleId
    WHERE s.id = :scheduleId
        AND s.isActive = 1
        AND (
            (s.startTime <= s.endTime AND :currentTime BETWEEN s.startTime AND s.endTime)
            OR
            (s.startTime > s.endTime AND (:currentTime >= s.startTime OR :currentTime <= s.endTime))
        )
    """
    )
    suspend fun getActiveAndRunningSchedule(
        scheduleId: Long,
        currentTime: LocalTime,
    ): ScheduleEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM schedules
            WHERE name = :scheduleName
                OR (startTime = :startTime AND endTime = :endTime)
        )
        """
    )
    fun isScheduleExists(scheduleName: String, startTime: LocalTime, endTime: LocalTime): Boolean
}