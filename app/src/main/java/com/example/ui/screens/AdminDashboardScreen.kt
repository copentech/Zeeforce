package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Product
import com.example.data.repository.OrderBookingRepository
import com.example.ui.components.SecondaryStaffPinDialog
import com.example.ui.components.ThreeLayerStockAlertWidget
import com.example.util.AdminReportPdfPrinter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: OrderBookingRepository,
    onNavigateToCommissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToDispatch: () -> Unit,
    onNavigateToSuppliers: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val products by repository.allProducts.collectAsState(initial = emptyList())
    val orders by repository.allOrders.collectAsState(initial = emptyList())
    val companyProfile by repository.companyProfile.collectAsState()

    val total7DayRevenue = remember(orders) {
        orders.sumOf { it.order.totalAmount }
    }

    // Dynamic currency formatter based on settings
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
                            contentDescription = "Zayed Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .padding(end = 10.dp)
                                .testTag("admin_top_app_bar_logo")
                        )
                        Column {
                            Text("Zayed Admin Console", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(companyProfile.tagline, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Executive Summary Revenue Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total System Revenue (Real-time)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormat.format(total7DayRevenue),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.zayed_logo),
                            contentDescription = "Zayed Corporate Emblem",
                            modifier = Modifier
                                .size(56.dp)
                                .padding(start = 8.dp)
                                .testTag("revenue_banner_logo")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Active Orders: ${orders.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            AdminReportPdfPrinter.exportAdminReport(
                                context = context,
                                weeklyRevenue = total7DayRevenue,
                                orderCount = orders.size,
                                lowStockProducts = products.filter { it.alertLevel != com.example.data.model.StockLevelAlert.HEALTHY },
                                currencyCode = companyProfile.currencyCode
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Export",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Generate Executive PDF Report")
                    }
                }
            }

            // 3-Layer Inventory Low-Stock Alert System Widget
            ThreeLayerStockAlertWidget(
                products = products,
                onUpdateStock = { productId, newStock ->
                    coroutineScope.launch {
                        repository.updateStock(productId, newStock)
                    }
                }
            )

            Text("Administrative Modules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Admin Modules Grid
            val modules = listOf(
                ModuleItem("Analytics", Icons.Default.BarChart, onNavigateToAnalytics, "admin_nav_analytics"),
                ModuleItem("Staff", Icons.Default.People, onNavigateToStaff, "admin_nav_staff"),
                ModuleItem("Inventory", Icons.Default.Inventory, onNavigateToInventory, "admin_nav_inventory"),
                ModuleItem("Orders", Icons.Default.ReceiptLong, onNavigateToDispatch, "admin_nav_orders"),
                ModuleItem("Audit Logs", Icons.Default.History, onNavigateToLogs, "admin_nav_logs"),
                ModuleItem("Customers", Icons.Default.ContactPage, onNavigateToCustomers, "admin_nav_customers"),
                ModuleItem("Suppliers", Icons.Default.LocalShipping, onNavigateToSuppliers, "admin_nav_suppliers"),
                ModuleItem("Commissions", Icons.Default.Payments, onNavigateToCommissions, "admin_nav_commissions"),
                ModuleItem("Settings", Icons.Default.Settings, onNavigateToSettings, "admin_nav_settings")
            )

            modules.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { module ->
                        AdminModuleCard(
                            item = module,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty space if row is not full
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

data class ModuleItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val onClick: () -> Unit, val tag: String)

@Composable
fun AdminModuleCard(item: ModuleItem, modifier: Modifier = Modifier) {
    Card(
        onClick = item.onClick,
        modifier = modifier
            .aspectRatio(1f)
            .testTag(item.tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
