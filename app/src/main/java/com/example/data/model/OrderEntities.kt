package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(
    tableName = "orders",
    indices = [Index(value = ["customerId"])]
)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: String, // e.g. "ORD-2026-1048"
    val customerId: String,
    val customerName: String,
    val businessName: String,
    val customerLocation: String,
    val customerMobile: String,
    
    // Rep details
    val accountHolderName: String,
    val accountHolderContact: String,
    val accountHolderEmail: String,
    
    // Order specifications
    val orderType: String, // "Standard Wholesale", "Bulk Distribution", "Consignment", "Express Pickup"
    val paymentModel: String, // "M-Pesa / Mobile Money", "Cash on Delivery", "Bank RTGS / EFT", "Cheque", "30-Day Credit"
    
    // Regional Pricing Settings
    val distanceKm: Int,
    val distanceBand: String, // "0-25 km (Local)", "25-50 km (Metro)", "50-100 km (Regional)", "100+ km (Upcountry)"
    val regionalTier: String, // "Tier 1 - Urban Metro", "Tier 2 - Peri-Urban", "Tier 3 - Remote"
    val regionalFreightSurcharge: Double,
    
    // Delivery Dates
    val expectedDeliveryDate: String, // Date chosen by rep/customer (YYYY-MM-DD)
    val scheduledDeliveryDate: String = "", // Admin set confirmed delivery timestamp
    val deliveryUrgency: String = "Standard Dispatch", // "Standard Dispatch", "Next-Day Priority", "Weekend Express"
    
    // Pricing & Tax
    val totalQuantity: Int,
    val subtotal: Double,
    val taxCategory: String, // "VAT (16%)", "Zero-Rated (0%)", "Exempt (0%)"
    val taxRate: Double, // 0.16 or 0.0
    val taxAmount: Double,
    val totalAmount: Double,
    
    // Workflow & Sync
    val status: String = "BOOKED", // "BOOKED", "SCHEDULED", "IN_TRANSIT", "DELIVERED", "CANCELLED"
    val isPaid: Boolean = false, // Payment approval status
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val stampVerificationCode: String = "STP-${UUID.randomUUID().toString().take(8).uppercase()}",

    // Sales Commission & Admin Payout Tracking
    val commissionType: String = "PERCENTAGE", // "PERCENTAGE" or "FIXED"
    val commissionRate: Double = 0.035, // 3.5%
    val commissionAmount: Double = 0.0,
    val commissionStatus: String = "PENDING", // "PENDING", "APPROVED", "PAID"
    val commissionPaymentCode: String = "", // Payment ref (M-Pesa / Bank ref) entered by Admin upon delivery & payment approval
    val commissionPaidAt: Long = 0L
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productId: String,
    val productName: String,
    val unit: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val supplierId: String? = null
)

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)
