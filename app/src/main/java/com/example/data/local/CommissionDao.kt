package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommissionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: CommissionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(commissions: List<CommissionRecord>)

    @Update
    suspend fun updateCommission(commission: CommissionRecord)

    @Query("SELECT * FROM commission_records ORDER BY timestamp DESC")
    fun getAllCommissions(): Flow<List<CommissionRecord>>

    @Query("SELECT * FROM commission_records WHERE salesRepId = :repId ORDER BY timestamp DESC")
    fun getCommissionsByRep(repId: String): Flow<List<CommissionRecord>>

    @Query("SELECT * FROM commission_records WHERE orderId = :orderId LIMIT 1")
    suspend fun getCommissionByOrderId(orderId: String): CommissionRecord?

    @Query("""
        UPDATE commission_records 
        SET isPaidOut = 1,
            paymentCode = :paymentCode,
            paidAt = :paidAt
        WHERE orderId = :orderId
    """)
    suspend fun approveAndPayCommission(orderId: String, paymentCode: String, paidAt: Long)

    @Query("SELECT * FROM commission_records WHERE isPaidOut = 0")
    suspend fun getUnsyncedCommissions(): List<CommissionRecord>

    @Query("UPDATE commission_records SET isPaidOut = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("""
        SELECT COALESCE(SUM(calculatedCommission), 0.0) 
        FROM commission_records 
        WHERE salesRepId = :repId AND isPaidOut = 1
    """)
    fun getTotalCommissionEarned(repId: String): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(calculatedCommission), 0.0) 
        FROM commission_records 
        WHERE salesRepId = :repId AND isPaidOut = 0
    """)
    fun getTotalCommissionPending(repId: String): Flow<Double?>

    @Query("DELETE FROM commission_records WHERE orderId = :orderId")
    suspend fun deleteByOrderId(orderId: String)

    @Query("SELECT * FROM commission_records WHERE commissionType = :commissionType ORDER BY timestamp DESC")
    fun getCommissionsByMode(commissionType: String): Flow<List<CommissionRecord>>

    @Query("""
        UPDATE commission_records 
        SET commissionType = :commissionType,
            commissionRate = :rateOrFee,
            calculatedCommission = :calculatedAmount
        WHERE orderId = :orderId
    """)
    suspend fun updateCommissionCalculation(
        orderId: String,
        commissionType: String,
        rateOrFee: Double,
        calculatedAmount: Double
    )

    @Query("""
        SELECT COALESCE(SUM(calculatedCommission), 0.0) 
        FROM commission_records 
        WHERE commissionType = :commissionType AND isPaidOut = 1
    """)
    fun getTotalCommissionEarnedByMode(commissionType: String): Flow<Double?>
}
