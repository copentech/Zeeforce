package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.StockLevelAlert

@Composable
fun ThreeLayerStockAlertWidget(
    products: List<Product>,
    onUpdateStock: (productId: String, newStock: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<StockLevelAlert?>(null) }

    val outOfStockCount = products.count { it.alertLevel == StockLevelAlert.OUT_OF_STOCK }
    val criticalCount = products.count { it.alertLevel == StockLevelAlert.CRITICAL }
    val lowCount = products.count { it.alertLevel == StockLevelAlert.LOW }

    val filteredProducts = remember(products, selectedFilter) {
        if (selectedFilter == null) {
            products.filter { it.alertLevel != StockLevelAlert.HEALTHY }
        } else {
            products.filter { it.alertLevel == selectedFilter }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_alert_widget"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3-Layer Inventory Low-Stock System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3-Layer Filter Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Out of Stock Badge
                AlertBadgeCard(
                    title = "Out of Stock",
                    count = outOfStockCount,
                    color = Color(0xFFDC2626),
                    icon = Icons.Default.Error,
                    isSelected = selectedFilter == StockLevelAlert.OUT_OF_STOCK,
                    onClick = {
                        selectedFilter = if (selectedFilter == StockLevelAlert.OUT_OF_STOCK) null else StockLevelAlert.OUT_OF_STOCK
                    },
                    modifier = Modifier.weight(1f).testTag("filter_out_of_stock")
                )

                // Critical Stock Badge
                AlertBadgeCard(
                    title = "Critical",
                    count = criticalCount,
                    color = Color(0xFFD97706),
                    icon = Icons.Default.Warning,
                    isSelected = selectedFilter == StockLevelAlert.CRITICAL,
                    onClick = {
                        selectedFilter = if (selectedFilter == StockLevelAlert.CRITICAL) null else StockLevelAlert.CRITICAL
                    },
                    modifier = Modifier.weight(1f).testTag("filter_critical")
                )

                // Low Stock Badge
                AlertBadgeCard(
                    title = "Low Warning",
                    count = lowCount,
                    color = Color(0xFF0284C7),
                    icon = Icons.Default.Info,
                    isSelected = selectedFilter == StockLevelAlert.LOW,
                    onClick = {
                        selectedFilter = if (selectedFilter == StockLevelAlert.LOW) null else StockLevelAlert.LOW
                    },
                    modifier = Modifier.weight(1f).testTag("filter_low")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFilter == null) "All stock levels are currently healthy!" else "No products found in this alert category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductAlertRow(
                            product = product,
                            onUpdateStock = onUpdateStock
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertBadgeCard(
    title: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        contentColor = if (isSelected) Color.White else color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$count", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(text = title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ProductAlertRow(
    product: Product,
    onUpdateStock: (productId: String, newStock: Int) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    val (badgeColor, alertLabel) = when (product.alertLevel) {
        StockLevelAlert.OUT_OF_STOCK -> Color(0xFFDC2626) to "OUT OF STOCK (0)"
        StockLevelAlert.CRITICAL -> Color(0xFFD97706) to "CRITICAL (${product.currentStock})"
        StockLevelAlert.LOW -> Color(0xFF0284C7) to "LOW (${product.currentStock})"
        StockLevelAlert.HEALTHY -> Color(0xFF16A34A) to "HEALTHY (${product.currentStock})"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Category: ${product.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = alertLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { showEditDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("restock_button_${product.id}")
            ) {
                Text("Restock", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showEditDialog) {
        var newStockText by remember { mutableStateOf("${product.currentStock + 10}") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Restock ${product.name}") },
            text = {
                OutlinedTextField(
                    value = newStockText,
                    onValueChange = { newStockText = it },
                    label = { Text("New Stock Quantity") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val amount = newStockText.toIntOrNull() ?: product.currentStock
                    onUpdateStock(product.id, amount)
                    showEditDialog = false
                }) {
                    Text("Save Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
