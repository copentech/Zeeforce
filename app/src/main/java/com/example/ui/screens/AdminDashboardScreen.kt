package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.repository.SalesForceRepository
import com.example.ui.components.SecondaryStaffPinDialog
import com.example.ui.components.ThreeLayerStockAlertWidget
import com.example.util.AdminReportPdfPrinter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: SalesForceRepository,
    onNavigateToCommissions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val products by repository.getAllProducts().collectAsState(initial = emptyList())
    val orders by repository.getAllOrders().collectAsState(initial = emptyList())

    var showPinDialog by remember { mutableStateOf(false) }
    var isAuthorized by remember { mutableStateOf(true) } // Verified state

    val total7DayRevenue = remember(orders) {
        orders.sumOf { it.totalPricing }
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zayed Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.testTag("admin_security_lock")
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Security Auth")
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
                    Text(
                        text = "7-Day Sales Performance",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Orders Processed: ${orders.size}",
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
                                lowStockProducts = products.filter { it.alertLevel != com.example.data.model.StockLevelAlert.HEALTHY }
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
                        Text("Export 7-Day Revenue Report to PDF")
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

            // Quick Actions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCommissions,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_commissions_button")
                ) {
                    Text("Commission Slips")
                }

                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_settings_button")
                ) {
                    Text("Settings & Theme")
                }
            }
        }
    }

    if (showPinDialog) {
        SecondaryStaffPinDialog(
            title = "Admin Verification Required",
            onVerifyPin = { it == "1234" },
            onDismiss = { showPinDialog = false },
            onAuthorized = {
                isAuthorized = true
                showPinDialog = false
            }
        )
    }
}
