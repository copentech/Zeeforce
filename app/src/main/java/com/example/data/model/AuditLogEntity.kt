package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val staffId: String,
    val staffSerializedId: String,
    val staffName: String,
    val staffRole: String,
    val actionType: String, // e.g. "PIN_RESET", "STAFF_TERMINATION", "COMMISSION_PAYOUT", "ORDER_DISPATCHED", "DELIVERY_COMPLETED"
    val details: String,
    val isSyncedToCloud: Boolean = true
)
