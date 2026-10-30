package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff_activity_logs")
data class StaffActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val staffId: String,
    val staffName: String,
    val activityType: String, // "LOGIN", "ACCOUNT_MODIFICATION", "ORDER_UPDATE"
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
