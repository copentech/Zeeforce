package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderDeliveryStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

import com.example.data.repository.OrderBookingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAccountScreen(
    repository: OrderBookingRepository,
    customerEmail: String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    val allCustomers by repository.allCustomers.collectAsState(initial = emptyList())
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val companyProfile by repository.companyProfile.collectAsState()

    var selectedCustomer by remember(allCustomers, customerEmail) {
        mutableStateOf(
            allCustomers.firstOrNull { 
                it.businessEmail.equals(customerEmail, ignoreCase = true) || 
                it.companyEmail.equals(customerEmail, ignoreCase = true) 
            } ?: allCustomers.firstOrNull()
        )
    }

    val activeCustomerEmail = selectedCustomer?.businessEmail?.ifEmpty { selectedCustomer?.companyEmail } ?: customerEmail
    val orders by repository.getOrdersForCustomer(activeCustomerEmail).collectAsState(initial = emptyList())

    var showBackorderDialog by remember { mutableStateOf(false) }
    var selectedStockoutProduct by remember { mutableStateOf<Product?>(null) }
    var showCustomerPickerDropdown by remember { mutableStateOf(false) }

    val stockoutProducts = remember(products) {
        products.filter { it.currentStock <= 0 }
    }

    val currencyFormat = remember(companyProfile.currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            currency = java.util.Currency.getInstance(companyProfile.currencyCode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.zayed_logo),
                            contentDescription = "Zayed Corporate Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .padding(end = 8.dp)
                                .testTag("customer_portal_logo")
                        )
                        Text("Customer Order & Delivery Portal", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Selector Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("customer_selector_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Client Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = selectedCustomer?.customerName ?: "No Customer Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedCustomer?.businessName ?: "Independent Client"} • $activeCustomerEmail",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (allCustomers.size > 1) {
                                Box {
                                    OutlinedButton(
                                        onClick = { showCustomerPickerDropdown = true },
                                        modifier = Modifier.testTag("btn_switch_customer")
                                    ) {
                                        Text("Switch Client")
                                    }
                                    DropdownMenu(
                                        expanded = showCustomerPickerDropdown,
                                        onDismissRequest = { showCustomerPickerDropdown = false }
                                    ) {
                                        allCustomers.forEach { cust ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Column {
                                                        Text(cust.customerName, fontWeight = FontWeight.Bold)
                                                        Text("${cust.businessName} (${cust.contactMobile})", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                },
                                                onClick = {
                                                    selectedCustomer = cust
                                                    showCustomerPickerDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Out of Stock Backorder Banner if stockouts exist
            if (stockoutProducts.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().testTag("backorder_notice_banner")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Item Out of Stock?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Certain inventory items are currently sold out. You can push a priority backorder request.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showBackorderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("push_backorder_request_button")
                            ) {
                                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Backorder")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Push Order Request for Out-of-Stock Item")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My Active Orders & Delivery Tracking",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No order history found for ${selectedCustomer?.customerName ?: activeCustomerEmail}")
                    }
                }
            } else {
                items(orders) { orderWithItems ->
                    CustomerOrderCard(orderWithItems = orderWithItems, currencyFormat = currencyFormat)
                }
            }
        }
    }

    if (showBackorderDialog) {
        AlertDialog(
            onDismissRequest = { showBackorderDialog = false },
            title = { Text("Push Backorder Request") },
            text = {
                Column {
                    Text("Select sold-out item to place priority backorder:")
                    Spacer(modifier = Modifier.height(8.dp))

                    stockoutProducts.forEach { prod ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStockoutProduct?.id == prod.id,
                                onClick = { selectedStockoutProduct = prod }
                            )
                            Text(text = "${prod.name} (${currencyFormat.format(prod.unitPrice)})")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStockoutProduct?.let { prod ->
                            coroutineScope.launch {
                                repository.requestBackorder(
                                    productName = prod.name,
                                    customerName = selectedCustomer?.customerName ?: "Valued Customer",
                                    customerEmail = activeCustomerEmail.ifEmpty { "support@zayed.com" }
                                )
                                showBackorderDialog = false
                            }
                        }
                    },
                    enabled = selectedStockoutProduct != null,
                    modifier = Modifier.testTag("submit_backorder_confirm")
                ) {
                    Text("Submit Backorder Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackorderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CustomerOrderCard(
    orderWithItems: com.example.data.model.OrderWithItems,
    currencyFormat: NumberFormat
) {
    val order = orderWithItems.order
    val itemsSummary = orderWithItems.items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
        .ifEmpty { "${order.totalQuantity} items" }
    val deliveryStatus = when (order.status) {
        "DELIVERED" -> OrderDeliveryStatus.DELIVERED
        "IN_TRANSIT" -> OrderDeliveryStatus.DISPATCHED_IN_TRANSIT
        "SCHEDULED" -> OrderDeliveryStatus.PROCESSED
        else -> OrderDeliveryStatus.UNPROCESSED
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormat.format(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Items: $itemsSummary", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Tracking Code: ${order.stampVerificationCode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Status Stepper
            DeliveryStatusStepper(currentStatus = deliveryStatus)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Payment Method Available", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = order.paymentModel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Sales Rep in Charge", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Rep", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = order.accountHolderName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusStepper(currentStatus: OrderDeliveryStatus) {
    val statuses = listOf(
        OrderDeliveryStatus.UNPROCESSED,
        OrderDeliveryStatus.PROCESSED,
        OrderDeliveryStatus.DISPATCHED_IN_TRANSIT,
        OrderDeliveryStatus.DELIVERED
    )

    val currentIndex = statuses.indexOf(currentStatus)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        statuses.forEachIndexed { index, status ->
            val isCompleted = index <= currentIndex
            val color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status.name.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}
