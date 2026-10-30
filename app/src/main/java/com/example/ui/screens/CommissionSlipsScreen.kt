package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CommissionRecord
import com.example.data.model.CommissionType
import com.example.data.repository.SalesForceRepository
import com.example.util.CommissionPdfPrinter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionSlipsScreen(
    repository: SalesForceRepository,
    salesRepId: String = "rep_101",
    salesRepName: String = "Ahmad Al-Mansoor"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val commissions by repository.getCommissionsForRep(salesRepId).collectAsState(initial = emptyList())

    var showAddCommissionModal by remember { mutableStateOf(false) }

    val totalCommission = remember(commissions) {
        commissions.sumOf { it.calculatedCommission }
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commission Slips & Earnings", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCommissionModal = true },
                modifier = Modifier.testTag("add_commission_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Commission")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Earnings Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().testTag("commission_earnings_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Representative: $salesRepName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Accumulated Earnings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currencyFormat.format(totalCommission),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            CommissionPdfPrinter.generateCommissionSlip(
                                context = context,
                                salesRepName = salesRepName,
                                salesRepId = salesRepId,
                                records = commissions
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_commission_slip_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Official Commission Slip (PDF)")
                    }
                }
            }

            Text(
                text = "Multi-Mode Commission Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (commissions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No commission records found.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(commissions) { record ->
                        CommissionRow(record = record, currencyFormat = currencyFormat)
                    }
                }
            }
        }
    }

    if (showAddCommissionModal) {
        var orderIdText by remember { mutableStateOf("ord_${(100..999).random()}") }
        var orderAmountText by remember { mutableStateOf("1000.0") }
        var selectedType by remember { mutableStateOf(CommissionType.PERCENTAGE_BASED) }
        var rateText by remember { mutableStateOf("5.0") }

        AlertDialog(
            onDismissRequest = { showAddCommissionModal = false },
            title = { Text("Record Sale Commission") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = orderIdText,
                        onValueChange = { orderIdText = it },
                        label = { Text("Order ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = orderAmountText,
                        onValueChange = { orderAmountText = it },
                        label = { Text("Order Amount ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Commission Structure Mode:", style = MaterialTheme.typography.labelMedium)

                    CommissionType.values().forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    rateText = if (type == CommissionType.FLAT_FIXED) "25.0" else "5.0"
                                }
                            )
                            Text(text = type.name.replace("_", " "))
                        }
                    }

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text(if (selectedType == CommissionType.FLAT_FIXED) "Flat $ Rate" else "Percentage % Rate") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = orderAmountText.toDoubleOrNull() ?: 0.0
                    val rate = rateText.toDoubleOrNull() ?: 0.0

                    coroutineScope.launch {
                        repository.recordCommission(
                            salesRepId = salesRepId,
                            salesRepName = salesRepName,
                            orderId = orderIdText,
                            orderAmount = amount,
                            type = selectedType,
                            rate = rate
                        )
                        showAddCommissionModal = false
                    }
                }) {
                    Text("Calculate & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCommissionModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CommissionRow(
    record: CommissionRecord,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Order #${record.orderId}", fontWeight = FontWeight.Bold)
                Text(
                    text = "Mode: ${record.commissionType.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Order Total: ${currencyFormat.format(record.orderAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${currencyFormat.format(record.calculatedCommission)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (record.commissionType == CommissionType.FLAT_FIXED) "$${record.commissionRate} flat" else "${record.commissionRate}% rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
