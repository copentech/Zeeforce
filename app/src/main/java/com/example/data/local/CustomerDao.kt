package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE pendingDeletionRequested = 1 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getPendingDeletionRequests(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): Customer?

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET pendingDeletionRequested = 1, deletionRequestReason = :reason, deletionRequestedBy = :requestedBy WHERE id = :id")
    suspend fun flagCustomerDeletionRequest(id: String, reason: String, requestedBy: String)

    @Query("UPDATE customers SET pendingDeletionRequested = 0, deletionRequestReason = '', deletionRequestedBy = '' WHERE id = :id")
    suspend fun dismissDeletionRequest(id: String)

    @Query("UPDATE customers SET isArchived = 1, pendingDeletionRequested = 0 WHERE id = :id")
    suspend fun archiveCustomer(id: String)

    @Query("UPDATE customers SET isArchived = 0 WHERE id = :id")
    suspend fun restoreCustomer(id: String)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Query("UPDATE customers SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}
