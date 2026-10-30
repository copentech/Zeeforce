package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StockLevelAlert {
    OUT_OF_STOCK, // 0 items
    CRITICAL,     // <= Critical threshold (e.g., 5 items)
    LOW,          // <= Low threshold (e.g., 15 items)
    HEALTHY       // > Low threshold
}

typealias StockAlertTier = StockLevelAlert

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val unitPrice: Double,
    val currentStock: Int,
    val lowStockThreshold: Int = 15,
    val criticalStockThreshold: Int = 5,
    val imageUrl: String = "",
    val sku: String = "SKU-${id.take(6).uppercase()}",
    val unit: String = "Units",
    val minPrice: Double = unitPrice * 0.9,
    val maxPrice: Double = unitPrice * 1.2,
    val basePrice: Double = unitPrice,
    val stockQty: Int = currentStock,
    val iconKey: String = "",
    val supplierId: String = "SUPP-00101",
    val isInfiniteStock: Boolean = false
) {
    companion object {
        const val INFINITE_STOCK = -1
    }
    val isAlertTriggered: Boolean
        get() = currentStock <= lowStockThreshold

    val stockAlertTier: StockAlertTier
        get() = alertLevel

    val alertLevel: StockLevelAlert
        get() = when {
            isInfiniteStock -> StockLevelAlert.HEALTHY
            currentStock <= 0 -> StockLevelAlert.OUT_OF_STOCK
            currentStock <= criticalStockThreshold -> StockLevelAlert.CRITICAL
            currentStock <= lowStockThreshold -> StockLevelAlert.LOW
            else -> StockLevelAlert.HEALTHY
        }
}

