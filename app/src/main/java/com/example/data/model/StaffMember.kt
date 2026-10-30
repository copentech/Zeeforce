package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    SALES_REP,
    CUSTOMER,
    SUPPLIER
}

@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey val id: String,
    val serializedAccountId: String = "AYBZ-001-SR", // e.g. AYBZ-001-SR, CUST-00192, SUPP-00123
    val name: String,
    val email: String,
    val role: UserRole = UserRole.SALES_REP,
    val phone: String = "+971 4 200 8888",
    val password: String = "password",
    val passwordHash: String = "password",
    val adminPin: String = "1234",
    val customRoleLabel: String = "Sales Representative",
    val secondaryPin: String = "1234",
    val isBiometricEnabled: Boolean = true,
    val isTemporaryPassword: Boolean = false,
    val tempPasswordExpiresAt: Long = 0L,
    val mustChangePassword: Boolean = false,
    val isActive: Boolean = true,
    val accountStatus: String = "ACTIVE",
    val canBookOrders: Boolean = true,
    val canManageInventory: Boolean = false,
    val canScheduleDelivery: Boolean = false,
    val canAccessInventory: Boolean = true,
    val canAccessOrders: Boolean = true,
    val canAccessCustomers: Boolean = true,
    val canOnboardCustomers: Boolean = true,
    val canPlaceOrders: Boolean = true,
    val canApproveSuppliers: Boolean = false,
    val canManageStaff: Boolean = false,
    val commissionType: String = "PERCENTAGE",
    val commissionRate: Double = 0.05,
    val fixedCommissionAmount: Double = 0.0,
    val isAdmin: Boolean = role == UserRole.ADMIN,
    val passwordResetRequested: Boolean = false
) {
    fun isSuspended(): Boolean = accountStatus.equals("SUSPENDED", ignoreCase = true) || !isActive
    fun isTerminated(): Boolean = accountStatus.equals("TERMINATED", ignoreCase = true)

    val tempPassword: String
        get() = password

    val isTempPasswordValid: Boolean
        get() = isTemporaryPassword && System.currentTimeMillis() <= tempPasswordExpiresAt


    val isTempPasswordExpired: Boolean
        get() = isTemporaryPassword && System.currentTimeMillis() > tempPasswordExpiresAt

    fun updateAccountStatus(status: String): StaffMember = copy(accountStatus = status)

    fun updateCommissionSettings(type: String, rate: Double, fixed: Double): StaffMember = copy(
        commissionType = type,
        commissionRate = rate,
        fixedCommissionAmount = fixed
    )

    companion object {
        fun generateSerializedId(role: UserRole, sequenceNumber: Int): String {
            val formattedSeq = String.format("%03d", sequenceNumber)
            return when (role) {
                UserRole.ADMIN -> "AYBZ-$formattedSeq-ADM"
                UserRole.SALES_REP -> "AYBZ-$formattedSeq-SR"
                UserRole.CUSTOMER -> "CUST-$formattedSeq"
                UserRole.SUPPLIER -> "SUPP-$formattedSeq"
            }
        }
    }
}

