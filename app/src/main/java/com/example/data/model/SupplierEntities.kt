package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val companyName: String,
    val companyTag: String, // Unique tag linking to products
    val contactPerson: String,
    val phone: String,
    val email: String,
    val isApproved: Boolean = false,
    val isActive: Boolean = false
)

@Entity(tableName = "supplier_products")
data class SupplierProduct(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val productName: String,
    val imageUrl: String,
    val price: Double,
    val commission: Double,
    val isActive: Boolean = false
)

@Entity(tableName = "supplier_orders")
data class SupplierOrder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val parentOrderId: String,
    val orderNumber: String,
    val supplierId: String,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, DISPATCHED, DELIVERED
    val totalAmount: Double,
    val adminCommission: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "supplier_order_items")
data class SupplierOrderItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val supplierOrderId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

