package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Product
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InventoryCatalogScreen(
    products: List<Product>,
    onAddProduct: (
        name: String,
        category: String,
        unit: String,
        minPrice: Double,
        maxPrice: Double,
        basePrice: Double,
        stockQty: Int,
        sku: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val filteredProducts = products.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Inventory Catalog", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "${products.size} active warehouse SKUs", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Button(
                    onClick = { showAddProductDialog = true },
                    modifier = Modifier.testTag("btn_open_add_product_dialog"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_catalog_search"),
                placeholder = { Text("Search by product name, SKU, category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCatalogItemCard(
                        product = product,
                        currencyFormatter = currencyFormatter
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddProductDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_product"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    if (showAddProductDialog) {
        AddProductModalDialog(
            onDismiss = { showAddProductDialog = false },
            onSave = { name, cat, unit, minP, maxP, baseP, stock, sku ->
                onAddProduct(name, cat, unit, minP, maxP, baseP, stock, sku)
                showAddProductDialog = false
            }
        )
    }
}

@Composable
fun ProductCatalogItemCard(
    product: Product,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.sku}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon Emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (product.isInfiniteStock) Color(0xFFD1FAE5) else if (product.stockQty > 20) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (product.isInfiniteStock) "Stock: ∞ (Infinite)" else "Stock: ${product.stockQty}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (product.isInfiniteStock) Color(0xFF047857) else if (product.stockQty > 20) Color(0xFF059669) else Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${product.category} • ${product.unit} • SKU: ${product.sku}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Base Price: ${currencyFormatter.format(product.basePrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Range: ${currencyFormatter.format(product.minPrice)} - ${currencyFormatter.format(product.maxPrice)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddProductModalDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        unit: String,
        minPrice: Double,
        maxPrice: Double,
        basePrice: Double,
        stockQty: Int,
        sku: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Beers & Malts") }
    var unit by remember { mutableStateOf("Crate (24 btls)") }
    var minPriceText by remember { mutableStateOf("3000") }
    var maxPriceText by remember { mutableStateOf("3600") }
    var basePriceText by remember { mutableStateOf("3300") }
    var isInfiniteStock by remember { mutableStateOf(true) }
    var stockText by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .testTag("add_product_modal"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Add Inventory Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        placeholder = { Text("e.g. Guinness Smooth 500ml") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category *") },
                        placeholder = { Text("e.g. Beers & Malts, Sodas, Water") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Packaging Unit *") },
                        placeholder = { Text("e.g. Crate (24 btls), Shrink 12-pack") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Product Code *") },
                        placeholder = { Text("e.g. BEER-GUI-24") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_sku"),
                        singleLine = true
                    )

                    Text(text = "PRICE RANGE SETTINGS (KES)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minPriceText,
                            onValueChange = { minPriceText = it },
                            label = { Text("Min Price") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = basePriceText,
                            onValueChange = { basePriceText = it },
                            label = { Text("Base Price *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxPriceText,
                            onValueChange = { maxPriceText = it },
                            label = { Text("Max Price") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Text(text = "INVENTORY QUANTITY POLICY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isInfiniteStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Infinite Inventory (∞ Unlimited)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Default when quantity is omitted or not specified. Orders are never constrained by stock counts.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = isInfiniteStock,
                                    onCheckedChange = {
                                        isInfiniteStock = it
                                        if (it) stockText = ""
                                    },
                                    modifier = Modifier.testTag("switch_infinite_stock")
                                )
                            }

                            if (!isInfiniteStock) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = stockText,
                                    onValueChange = { stockText = it },
                                    label = { Text("Specific Physical Stock Count") },
                                    placeholder = { Text("Leave blank for infinite stock (∞)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_product_stock"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    errorMessage?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val minP = minPriceText.toDoubleOrNull() ?: 0.0
                            val maxP = maxPriceText.toDoubleOrNull() ?: 0.0
                            val baseP = basePriceText.toDoubleOrNull() ?: 0.0
                            // If infinite stock is enabled OR stock input is blank, default to infinite stock (-1)
                            val stock: Int = if (isInfiniteStock || stockText.isBlank()) {
                                Product.INFINITE_STOCK
                            } else {
                                stockText.toIntOrNull() ?: 0
                            }

                            if (name.isBlank() || baseP <= 0) {
                                errorMessage = "Please enter product name and valid base price"
                                return@Button
                            }
                            val finalSku = if (sku.isBlank()) "SKU-${(100..999).random()}" else sku
                            onSave(name, category, unit, minP, maxP, baseP, stock, finalSku)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_product")
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}
