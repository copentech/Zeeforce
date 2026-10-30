package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val businessName: String,
    val fullLocation: String, // e.g. "General Motors Mombasa Road"
    val businessEmail: String,
    
    // Contact person details
    val contactPersonName: String,
    val contactPosition: String,
    val contactMobile: String,
    val contactSecondaryPhone: String = "",
    val companyEmail: String,
    
    // Tracking & Sync
    val capturedByAccountHolder: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,

    // Deletion & Archival governance (Archive replaces deletion to preserve order records)
    val isArchived: Boolean = false,
    val pendingDeletionRequested: Boolean = false,
    val deletionRequestReason: String = "",
    val deletionRequestedBy: String = "",
    // Customer Portal Authentication & Account Governance
    val customerPin: String = "1234",
    val accountStatus: String = "ACTIVE" // ACTIVE, SUSPENDED
)
