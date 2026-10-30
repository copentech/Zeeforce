package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SupplierOrder
import com.example.data.model.SupplierOrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierOrder(order: SupplierOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierOrderItems(items: List<SupplierOrderItem>)

    @Query("SELECT * FROM supplier_orders WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getOrdersBySupplier(supplierId: String): Flow<List<SupplierOrder>>

    @Query("SELECT * FROM supplier_orders ORDER BY createdAt DESC")
    fun getAllSupplierOrders(): Flow<List<SupplierOrder>>

    @Query("SELECT * FROM supplier_order_items WHERE supplierOrderId = :orderId")
    suspend fun getItemsForSupplierOrder(orderId: String): List<SupplierOrderItem>

    @Query("UPDATE supplier_orders SET status = :status WHERE id = :orderId")
    suspend fun updateSupplierOrderStatus(orderId: String, status: String)

    @Query("SELECT SUM(totalAmount) FROM supplier_orders WHERE supplierId = :supplierId")
    fun getTotalVolumeBySupplier(supplierId: String): Flow<Double?>

    @Query("SELECT SUM(adminCommission) FROM supplier_orders WHERE supplierId = :supplierId")
    fun getTotalCommissionBySupplier(supplierId: String): Flow<Double?>
}
