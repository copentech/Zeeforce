package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StaffActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffActivityDao {
    @Query("SELECT * FROM staff_activity_logs ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<StaffActivityLog>>

    @Query("SELECT * FROM staff_activity_logs WHERE staffId = :staffId ORDER BY timestamp DESC")
    fun getActivitiesByStaff(staffId: String): Flow<List<StaffActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(log: StaffActivityLog)

    @Query("SELECT * FROM staff_activity_logs WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedActivities(): List<StaffActivityLog>

    @Query("UPDATE staff_activity_logs SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long)
}
