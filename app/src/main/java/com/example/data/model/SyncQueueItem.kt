package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String, // "CUSTOMER", "ORDER"
    val entityId: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // "PENDING", "SYNCED", "FAILED"
    val retryCount: Int = 0,
    val lastError: String? = null
)
