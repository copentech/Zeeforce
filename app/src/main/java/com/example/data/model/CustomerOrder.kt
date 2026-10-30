package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderDeliveryStatus {
    UNPROCESSED,
    PROCESSED,
    DISPATCHED_IN_TRANSIT,
    DELIVERED,
    REJECTED,
    REJECTED_DELAYED,
    BACKORDERED
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    MOBILE_MONEY,
    CREDIT_CARD
}

@Entity(tableName = "customer_orders")
data class CustomerOrder(
    @PrimaryKey val id: String,
    val serializedOrderNumber: String, // e.g. ORD-00101
    val customerName: String,
    val customerEmail: String,
    val customerSerializedId: String = "CUST-00101",
    val itemsSummary: String,
    val totalPricing: Double,
    val paymentMethod: PaymentMethod,
    val deliveryStatus: OrderDeliveryStatus,
    val assignedSalesRepId: String,
    val assignedSalesRepSerializedId: String = "AYBZ-001-SR",
    val assignedSalesRepName: String,
    val supplierSerializedId: String = "SUPP-00101",
    val orderTimestamp: Long = System.currentTimeMillis(),
    val isBackorderRequest: Boolean = false,
    val hasCommissionApproved: Boolean = false,
    val isCommissionPaidOut: Boolean = false,
    val trackingNumber: String = "TRK-${(100000..999999).random()}"
) {
    companion object {
        fun generateSerializedOrderNumber(sequenceNumber: Int): String {
            return "ORD-${String.format("%05d", sequenceNumber)}"
        }
    }
}
