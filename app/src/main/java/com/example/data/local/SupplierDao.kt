package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Supplier
import com.example.data.model.SupplierProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: String): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: SupplierProduct)

    @Query("SELECT * FROM supplier_products WHERE supplierId = :supplierId")
    fun getProductsBySupplier(supplierId: String): Flow<List<SupplierProduct>>

    @Query("SELECT * FROM supplier_products WHERE supplierId = :supplierId AND productName = :name LIMIT 1")
    suspend fun getProductBySupplierAndName(supplierId: String, name: String): SupplierProduct?
}
