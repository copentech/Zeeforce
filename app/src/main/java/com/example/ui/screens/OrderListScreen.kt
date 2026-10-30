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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderWithItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    orders: List<OrderWithItems>,
    activeRole: String,
    onViewSummary: (OrderWithItems) -> Unit,
    onNewBookingClick: () -> Unit,
    onScheduleDeliveryClick: (OrderWithItems) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING_SYNC, SCHEDULED, DELIVERED

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val filteredOrders = orders.filter { orderWithItems ->
        val order = orderWithItems.order
        val matchesQuery = searchQuery.isBlank() ||
                order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                order.businessName.contains(searchQuery, ignoreCase = true) ||
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.customerLocation.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "PENDING_SYNC" -> !order.isSynced
            "SCHEDULED" -> order.status == "SCHEDULED"
            "DELIVERED" -> order.status == "DELIVERED"
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_search_input"),
                placeholder = { Text("Search by Order #, Business, or Location...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.outline)
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${orders.size})", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = selectedFilter == "PENDING_SYNC",
                    onClick = { selectedFilter = "PENDING_SYNC" },
                    label = {
                        val unsyncedCount = orders.count { !it.order.isSynced }
                        Text("Unsynced ($unsyncedCount)", fontSize = 12.sp)
                    },
                    modifier = Modifier.testTag("filter_unsynced")
                )
                FilterChip(
                    selected = selectedFilter == "SCHEDULED",
                    onClick = { selectedFilter = "SCHEDULED" },
                    label = {
                        val count = orders.count { it.order.status == "SCHEDULED" }
                        Text("Scheduled ($count)", fontSize = 12.sp)
                    },
                    modifier = Modifier.testTag("filter_scheduled")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (orders.isEmpty()) "No orders booked yet" else "No matching orders found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNewBookingClick,
                            modifier = Modifier.testTag("empty_state_new_booking")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book First Order")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders, key = { it.order.id }) { item ->
                        OrderCardItem(
                            orderWithItems = item,
                            activeRole = activeRole,
                            currencyFormatter = currencyFormatter,
                            onViewSummary = { onViewSummary(item) },
                            onScheduleDelivery = { onScheduleDeliveryClick(item) }
                        )
                    }
                }
            }
        }

        // Floating Action Button for New Booking
        FloatingActionButton(
            onClick = onNewBookingClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_new_booking"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Booking")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Booking", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderCardItem(
    orderWithItems: OrderWithItems,
    activeRole: String,
    currencyFormatter: NumberFormat,
    onViewSummary: () -> Unit,
    onScheduleDelivery: () -> Unit
) {
    val order = orderWithItems.order
    val items = orderWithItems.items
    val orderDate = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(order.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewSummary)
            .testTag("order_card_${order.orderNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Order Number, Date, Sync indicator & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $orderDate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sync icon
                    Icon(
                        imageVector = if (order.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = if (order.isSynced) "Synced" else "Pending Outbox",
                        tint = if (order.isSynced) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (order.status) {
                                    "DELIVERED" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    "SCHEDULED" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                "DELIVERED" -> Color(0xFF047857)
                                "SCHEDULED" -> Color(0xFF1D4ED8)
                                else -> Color(0xFFB45309)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Business & Location
            Text(
                text = order.businessName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = order.customerLocation,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Rep & Contact
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Rep: ${order.accountHolderName} • Contact: ${order.customerMobile}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Items Summary & Logistics info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.totalQuantity} units • ${items.size} item(s)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Exp: ${order.expectedDeliveryDate} (${order.distanceKm}km, ${order.regionalTier.take(6)})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (order.scheduledDeliveryDate.isNotBlank()) {
                        Text(
                            text = "Sched: ${order.scheduledDeliveryDate}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(order.totalAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = order.paymentModel.take(16),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom card actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewSummary,
                    modifier = Modifier.testTag("btn_view_stamp_${order.orderNumber}")
                ) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Stamped Client Copy", fontSize = 11.sp)
                }

                if (activeRole == "ADMIN" && order.status != "DELIVERED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onScheduleDelivery,
                        modifier = Modifier.testTag("btn_admin_schedule_${order.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Schedule", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
