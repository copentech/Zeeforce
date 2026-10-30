package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CommissionType {
    FLAT_FIXED,        // Fixed amount per order (e.g., $25 per order)
    PERCENTAGE_BASED,  // Percentage of total order value (e.g., 5% of order value)
    TIERED_PERFORMANCE // Tiered structure based on performance
}

@Entity(tableName = "commission_records")
data class CommissionRecord(
    @PrimaryKey val id: String,
    val salesRepId: String,
    val salesRepName: String,
    val orderId: String,
    val orderAmount: Double,
    val commissionType: CommissionType,
    val commissionRate: Double, // Fixed $ amount or percentage (e.g. 0.05 for 5%)
    val calculatedCommission: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isPaidOut: Boolean = false,
    val orderNumber: String = orderId,
    val repId: String = salesRepId,
    val repName: String = salesRepName,
    val repEmail: String = "",
    val rateOrFee: Double = commissionRate,
    val calculatedAmount: Double = calculatedCommission,
    val status: String = "PENDING",
    val paymentCode: String = "",
    val paidAt: Long = 0L
) {
    companion object {
        const val TYPE_FIXED = "FLAT_FIXED"
        const val TYPE_PERCENTAGE = "PERCENTAGE"
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PAID = "PAID"

        fun calculateCommission(
            orderAmount: Double,
            type: CommissionType,
            rate: Double
        ): Double {
            return when (type) {
                CommissionType.FLAT_FIXED -> rate
                CommissionType.PERCENTAGE_BASED -> orderAmount * (rate / 100.0)
                CommissionType.TIERED_PERFORMANCE -> {
                    if (orderAmount > 1000.0) orderAmount * ((rate + 2.0) / 100.0)
                    else orderAmount * (rate / 100.0)
                }
            }
        }

        fun calculate(
            salesRepId: String,
            salesRepName: String,
            orderId: String,
            orderAmount: Double,
            type: CommissionType,
            rate: Double
        ): CommissionRecord {
            val commission = when (type) {
                CommissionType.FLAT_FIXED -> rate
                CommissionType.PERCENTAGE_BASED -> orderAmount * (rate / 100.0)
                CommissionType.TIERED_PERFORMANCE -> {
                    if (orderAmount > 1000.0) orderAmount * ((rate + 2.0) / 100.0)
                    else orderAmount * (rate / 100.0)
                }
            }
            return CommissionRecord(
                id = "comm_${System.currentTimeMillis()}_${(1000..9999).random()}",
                salesRepId = salesRepId,
                salesRepName = salesRepName,
                orderId = orderId,
                orderAmount = orderAmount,
                commissionType = type,
                commissionRate = rate,
                calculatedCommission = commission,
                calculatedAmount = commission
            )
        }

        fun calculate(
            orderNumber: String,
            repId: String,
            repName: String,
            repEmail: String,
            orderTotalAmount: Double,
            rateOrFee: Double,
            typeStr: String
        ): CommissionRecord {
            val type = if (typeStr.equals("FIXED", true) || typeStr.equals("FLAT_FIXED", true)) CommissionType.FLAT_FIXED else CommissionType.PERCENTAGE_BASED
            return calculate(repId, repName, orderNumber, orderTotalAmount, type, rateOrFee).copy(
                orderNumber = orderNumber,
                repId = repId,
                repName = repName,
                repEmail = repEmail,
                rateOrFee = rateOrFee,
                calculatedAmount = calculate(repId, repName, orderNumber, orderTotalAmount, type, rateOrFee).calculatedCommission
            )
        }
    }
}

