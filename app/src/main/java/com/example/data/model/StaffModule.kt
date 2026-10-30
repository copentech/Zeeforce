package com.example.data.model

enum class StaffModule(
    val title: String,
    val description: String
) {
    INVENTORY("Inventory Management", "Manage stock levels, thresholds, and product catalog"),
    ORDERS("Order Processing", "Review and process customer orders and dispatches"),
    CUSTOMERS("Customer Accounts", "Manage customer profiles, delivery statuses, and credit"),
    STAFF("Staff Management", "Onboard and manage staff, roles, permissions, and PINs"),
    DELIVERY("Delivery & Dispatch", "Track deliveries, transit status, and delivery confirmations")
}
