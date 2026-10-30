package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StaffMember
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY role ASC, name ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: String): StaffMember?

    @Query("SELECT * FROM staff_members WHERE LOWER(email) = LOWER(:query) LIMIT 1")
    suspend fun findStaffByEmailOrPhone(query: String): StaffMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffMember>)

    @Update
    suspend fun updateStaff(staff: StaffMember)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: String)

    @Query("SELECT * FROM staff_members WHERE isTemporaryPassword = 1 ORDER BY name ASC")
    fun getPasswordResetRequests(): Flow<List<StaffMember>>

    @Query("UPDATE staff_members SET isTemporaryPassword = 1 WHERE LOWER(email) = LOWER(:identifier)")
    suspend fun flagPasswordResetRequest(identifier: String): Int

    @Query("UPDATE staff_members SET passwordHash = :tempPass, tempPasswordExpiresAt = :expiresAt, isTemporaryPassword = 1, mustChangePassword = 1 WHERE id = :staffId")
    suspend fun issueTemporaryPassword(staffId: String, tempPass: String, expiresAt: Long)

    @Query("UPDATE staff_members SET passwordHash = :newPassword, isTemporaryPassword = 0, tempPasswordExpiresAt = 0, mustChangePassword = 0 WHERE id = :staffId")
    suspend fun updatePasswordAndClearTemp(staffId: String, newPassword: String)

    @Query("UPDATE staff_members SET secondaryPin = :newPin WHERE id = :staffId")
    suspend fun updateStaffSecurityPin(staffId: String, newPin: String)

    @Query("UPDATE staff_members SET accountStatus = :status WHERE id = :id")
    suspend fun updateAccountStatus(id: String, status: String)

    @Query("UPDATE staff_members SET commissionType = :type, commissionRate = :rate, fixedCommissionAmount = :amount WHERE id = :id")
    suspend fun updateCommissionSettings(id: String, type: String, rate: Double, amount: Double)

    @Query("SELECT COUNT(*) FROM staff_members")
    suspend fun getStaffCount(): Int
}
