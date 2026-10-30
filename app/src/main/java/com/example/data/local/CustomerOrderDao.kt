package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderDeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerOrderDao {
    @Query("SELECT * FROM customer_orders ORDER BY orderTimestamp DESC")
    fun getAllOrders(): Flow<List<CustomerOrder>>

    @Query("SELECT * FROM customer_orders WHERE customerEmail = :email ORDER BY orderTimestamp DESC")
    fun getOrdersForCustomer(email: String): Flow<List<CustomerOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CustomerOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<CustomerOrder>)

    @Update
    suspend fun updateOrder(order: CustomerOrder)

    @Query("UPDATE customer_orders SET deliveryStatus = :status WHERE id = :orderId")
    suspend fun updateDeliveryStatus(orderId: String, status: OrderDeliveryStatus)
}
