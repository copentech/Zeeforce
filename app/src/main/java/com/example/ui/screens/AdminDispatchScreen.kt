package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CompanyProfile
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import com.example.data.model.StaffMember
import com.example.ui.components.AdminCommissionApprovalDialog
import com.example.ui.components.AdminSalesDashboardWidget
import com.example.ui.components.CommissionSlipDialog
import com.example.ui.components.ThreeLayerStockAlertWidget
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdminDispatchScreen(
    orders: List<OrderWithItems>,
    products: List<Product>,
    staffList: List<StaffMember>,
    pendingDeletionCount: Int,
    onScheduleDelivery: (orderId: String, scheduledDate: String) -> Unit,
    onUpdateStatus: (orderId: String, status: String) -> Unit,
    onQuickRestock: (productId: String, addedUnits: Int) -> Unit,
    onApproveDeliveryAndCommission: (orderId: String, paymentCode: String, commissionAmount: Double) -> Unit,
    onViewSummary: (OrderWithItems) -> Unit,
    onOpenStaffManagement: () -> Unit,
    onOpenPermissionManager: () -> Unit,
    onOpenDeletionRequests: () -> Unit,
    onOpenSupplierPerformance: () -> Unit,
    onOpenSalesAnalytics: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenAboutDialog: () -> Unit,
    companyProfile: CompanyProfile = CompanyProfile(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderToSchedule by remember { mutableStateOf<OrderWithItems?>(null) }
    var orderForCommissionApproval by remember { mutableStateOf<OrderWithItems?>(null) }
    var orderForCommissionSlip by remember { mutableStateOf<OrderWithItems?>(null) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val totalValue = orders.sumOf { it.order.totalAmount }
    val pendingDispatchCount = orders.count { it.order.status == "BOOKED" }
    val scheduledCount = orders.count { it.order.status == "SCHEDULED" }
    val deliveredCount = orders.count { it.order.status == "DELIVERED" }

    val filteredOrders = orders.filter {
        searchQuery.isBlank() ||
                it.order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                it.order.businessName.contains(searchQuery, ignoreCase = true) ||
                it.order.accountHolderName.contains(searchQuery, ignoreCase = true) ||
                it.order.customerLocation.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Admin Overview Header with Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Zayed SalesForce Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Al Yasir Bin Zayed Executive Console", fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenAboutDialog,
                    modifier = Modifier.testTag("admin_open_about_btn")
                ) {
                    Icon(Icons.Default.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedButton(
                    onClick = onOpenPermissionManager,
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.testTag("admin_permissions_btn")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Permissions", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.testTag("admin_app_settings_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "App Settings", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = onOpenSupplierPerformance,
                    modifier = Modifier.testTag("admin_supplier_performance_btn")
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Supplier Performance", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = onOpenSalesAnalytics,
                    modifier = Modifier.testTag("admin_sales_analytics_btn")
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = "Sales Revenue Analytics", tint = Color(0xFFD4AF37))
                }

                Button(
                    onClick = onOpenStaffManagement,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.testTag("admin_staff_management_btn")
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Staff", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lazy Column hosting Dashboard Chart Widget and Order List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Item 0: 3-Layer Low-Stock Alert System
            item {
                ThreeLayerStockAlertWidget(
                    products = products,
                    onUpdateStock = onQuickRestock
                )
            }

            // Item 1: Native Compose Recharts Sales & Low-Stock Suite
            item {
                AdminSalesDashboardWidget(
                    orders = orders,
                    products = products,
                    onQuickRestock = onQuickRestock,
                    companyProfile = companyProfile
                )
            }

            // Item 2: Customer Deletion Requests Banner if pending
            if (pendingDeletionCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDeletionRequests() }
                            .testTag("banner_admin_pending_deletions"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$pendingDeletionCount customer deletion request(s) awaiting approval",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Button(
                                onClick = onOpenDeletionRequests,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text("Review", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Item 3: Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_dispatch_search"),
                    placeholder = { Text("Filter by Rep, Customer, Location or Order #...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Order Cards List
            items(filteredOrders, key = { it.order.id }) { item ->
                val order = item.order
                val isPaid = order.commissionStatus == "PAID"
                val rep = staffList.find { it.email.equals(order.accountHolderEmail, ignoreCase = true) || order.accountHolderName.contains(it.name, ignoreCase = true) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isPaid) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF064E3B)
                                    ) {
                                        Text(
                                            text = "COMMISSION PAID",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (order.status) {
                                        "DELIVERED" -> Color(0xFF059669)
                                        "SCHEDULED" -> Color(0xFF2563EB)
                                        else -> Color(0xFFD97706)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = order.businessName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Location: ${order.customerLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(text = "Sales Rep: ${order.accountHolderName} (${order.accountHolderContact})", fontSize = 11.sp)
                        Text(
                            text = "Regional: ${order.regionalTier} • Distance: ${order.distanceKm} km • Expected: ${order.expectedDeliveryDate}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (order.scheduledDeliveryDate.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Admin Scheduled: ${order.scheduledDeliveryDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        if (isPaid && order.commissionPaymentCode.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Commission Paid Ref: ${order.commissionPaymentCode} (${currencyFormatter.format(order.commissionAmount)})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${order.totalQuantity} items • ${order.paymentModel}", fontSize = 11.sp)
                                Text(
                                    text = currencyFormatter.format(order.totalAmount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onViewSummary(item) },
                                    modifier = Modifier.testTag("admin_view_stamp_${order.orderNumber}"),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Invoice", fontSize = 10.sp)
                                }

                                if (isPaid) {
                                    Button(
                                        onClick = { orderForCommissionSlip = item },
                                        modifier = Modifier.testTag("admin_view_slip_${order.orderNumber}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color(0xFFD4AF37)),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Slip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { orderForCommissionApproval = item },
                                        modifier = Modifier.testTag("admin_approve_commission_${order.orderNumber}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Pay Comm.", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { selectedOrderToSchedule = item },
                                    modifier = Modifier.testTag("admin_set_schedule_${order.orderNumber}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(if (order.scheduledDeliveryDate.isBlank()) "Schedule" else "Resched.", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Schedule Delivery Dialog
    selectedOrderToSchedule?.let { item ->
        ScheduleDeliveryDialog(
            order = item.order,
            onDismiss = { selectedOrderToSchedule = null },
            onConfirm = { dateStr, status ->
                onScheduleDelivery(item.order.id, dateStr)
                if (status != item.order.status) {
                    onUpdateStatus(item.order.id, status)
                }
                selectedOrderToSchedule = null
            }
        )
    }

    // Admin Commission Approval Dialog
    orderForCommissionApproval?.let { approvalItem ->
        val rep = staffList.find {
            it.email.equals(approvalItem.order.accountHolderEmail, ignoreCase = true) ||
                    approvalItem.order.accountHolderName.contains(it.name, ignoreCase = true)
        }
        AdminCommissionApprovalDialog(
            orderWithItems = approvalItem,
            staffRep = rep,
            onDismiss = { orderForCommissionApproval = null },
            onApproveDeliveryAndCommission = { paymentCode, commissionAmount ->
                onApproveDeliveryAndCommission(approvalItem.order.id, paymentCode, commissionAmount)
                orderForCommissionApproval = null
            }
        )
    }

    // Official Commission Payout Slip Modal
    orderForCommissionSlip?.let { slipItem ->
        val rep = staffList.find {
            it.email.equals(slipItem.order.accountHolderEmail, ignoreCase = true) ||
                    slipItem.order.accountHolderName.contains(it.name, ignoreCase = true)
        }
        CommissionSlipDialog(
            orderWithItems = slipItem,
            staffRep = rep,
            onDismiss = { orderForCommissionSlip = null }
        )
    }
}

@Composable
fun ScheduleDeliveryDialog(
    order: com.example.data.model.OrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (scheduledDate: String, newStatus: String) -> Unit
) {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var scheduleDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd 09:00 AM", Locale.getDefault()).format(calendar.time))
    }
    var vehicleRoute by remember { mutableStateOf("Zayed Logistics Fleet #04 - Mombasa Road Corridor") }
    var selectedStatus by remember { mutableStateOf("SCHEDULED") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Schedule Delivery Dispatch", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(text = "Order: ${order.orderNumber} • ${order.businessName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Destination: ${order.customerLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(text = "Expected by Client: ${order.expectedDeliveryDate} (${order.deliveryUrgency})", fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = scheduleDate,
                    onValueChange = { scheduleDate = it },
                    label = { Text("Confirmed Delivery Date & Time") },
                    modifier = Modifier.fillMaxWidth().testTag("input_admin_schedule_date"),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vehicleRoute,
                    onValueChange = { vehicleRoute = it },
                    label = { Text("Logistics Fleet & Dispatch Route") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onConfirm(scheduleDate, selectedStatus)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_schedule_dispatch"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirm Dispatch")
                    }
                }
            }
        }
    }
}
