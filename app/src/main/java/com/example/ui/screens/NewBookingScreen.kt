package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import com.example.ui.BookingDraftState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingScreen(
    draftState: BookingDraftState,
    customers: List<Customer>,
    products: List<Product>,
    repName: String,
    onSelectCustomer: (Customer) -> Unit,
    onQuickAddCustomer: () -> Unit,
    onUpdateProduct: (Product, Int, Double?) -> Unit,
    onUpdateRegionalConfig: (Int?, String?, String?, String?) -> Unit,
    onUpdatePaymentAndDelivery: (String?, String?, String?) -> Unit,
    onSetStep: (Int) -> Unit,
    onSubmitOrder: (onComplete: (OrderWithItems) -> Unit) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Wizard Header with Step Indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Order Booking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Captured by: $repName",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Steps indicators (0: Customer, 1: Products, 2: Regional Pricing, 3: Review)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val stepNames = listOf("Customer", "Products", "Regional Pricing", "Confirm")
                    stepNames.forEachIndexed { index, name ->
                        val isDone = draftState.activeStep > index
                        val isCurrent = draftState.activeStep == index

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(enabled = isDone) { onSetStep(index) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> Color(0xFF10B981)
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Step Content Switcher
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (draftState.activeStep) {
                0 -> StepCustomerSelection(
                    customers = customers,
                    selectedCustomer = draftState.selectedCustomer,
                    onSelectCustomer = onSelectCustomer,
                    onQuickAddCustomer = onQuickAddCustomer
                )
                1 -> StepProductPicker(
                    products = products,
                    draftState = draftState,
                    currencyFormatter = currencyFormatter,
                    onUpdateProduct = onUpdateProduct,
                    onNext = { onSetStep(2) },
                    onBack = { onSetStep(0) }
                )
                2 -> StepRegionalPricingAndTerms(
                    draftState = draftState,
                    currencyFormatter = currencyFormatter,
                    onUpdateRegionalConfig = onUpdateRegionalConfig,
                    onUpdatePaymentAndDelivery = onUpdatePaymentAndDelivery,
                    onNext = { onSetStep(3) },
                    onBack = { onSetStep(1) }
                )
                3 -> StepOrderReviewAndStamp(
                    draftState = draftState,
                    repName = repName,
                    currencyFormatter = currencyFormatter,
                    onSubmitOrder = onSubmitOrder,
                    onBack = { onSetStep(2) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 0: CUSTOMER SELECTION
// -------------------------------------------------------------
@Composable
fun StepCustomerSelection(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onSelectCustomer: (Customer) -> Unit,
    onQuickAddCustomer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        searchQuery.isBlank() ||
                it.businessName.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.fullLocation.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Step 1: Select Customer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "Select an existing client or register a new one", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }

            Button(
                onClick = onQuickAddCustomer,
                modifier = Modifier.testTag("btn_quick_add_customer"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Client", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_search_input"),
            placeholder = { Text("Search by name, business, Mombasa Road location...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCustomers, key = { it.id }) { customer ->
                val isSelected = selectedCustomer?.id == customer.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCustomer(customer) }
                        .testTag("customer_item_${customer.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer.businessName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Contact: ${customer.contactPersonName} (${customer.contactPosition})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = customer.fullLocation,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "Phone: ${customer.contactMobile} • Rep: ${customer.capturedByAccountHolder}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            OutlinedButton(
                                onClick = { onSelectCustomer(customer) },
                                modifier = Modifier.testTag("select_customer_btn_${customer.id}")
                            ) {
                                Text("Select", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: PRODUCT PICKER
// -------------------------------------------------------------
@Composable
fun StepProductPicker(
    products: List<Product>,
    draftState: BookingDraftState,
    currencyFormatter: NumberFormat,
    onUpdateProduct: (Product, Int, Double?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Customer indicator
        draftState.selectedCustomer?.let { customer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Client: ${customer.businessName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = customer.fullLocation, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    TextButton(onClick = onBack) {
                        Text("Change Client", fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(text = "Step 2: Select Products & Quantities", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "Review catalog price ranges and add order quantities", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                val cartItem = draftState.cartItems[product.id]
                val currentQty = cartItem?.quantity ?: 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentQty > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (currentQty > 0) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${product.category} • ${product.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Base: ${currencyFormatter.format(product.basePrice)} (Range: ${currencyFormatter.format(product.minPrice)} - ${currencyFormatter.format(product.maxPrice)})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (product.isInfiniteStock) "In Stock: ∞ (Unlimited) • SKU: ${product.sku}" else "In Stock: ${product.stockQty} • SKU: ${product.sku}",
                                fontSize = 10.sp,
                                fontWeight = if (product.isInfiniteStock) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (product.isInfiniteStock) Color(0xFF047857) else MaterialTheme.colorScheme.outline
                            )
                        }

                        // Stepper control
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentQty > 0) onUpdateProduct(product, currentQty - 1, null)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("qty_minus_${product.sku}")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "$currentQty",
                                modifier = Modifier
                                    .width(36.dp)
                                    .testTag("qty_value_${product.sku}"),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            IconButton(
                                onClick = {
                                    onUpdateProduct(product, currentQty + 1, null)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("qty_plus_${product.sku}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total: ${draftState.totalUnits} items", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = currencyFormatter.format(draftState.subtotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack) {
                        Text("Back")
                    }

                    Button(
                        onClick = onNext,
                        enabled = draftState.totalUnits > 0,
                        modifier = Modifier.testTag("btn_proceed_to_regional_pricing")
                    ) {
                        Text("Regional Pricing")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: DYNAMIC REGIONAL PRICING & TERMS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepRegionalPricingAndTerms(
    draftState: BookingDraftState,
    currencyFormatter: NumberFormat,
    onUpdateRegionalConfig: (Int?, String?, String?, String?) -> Unit,
    onUpdatePaymentAndDelivery: (String?, String?, String?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val config = draftState.regionalConfig
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Text(text = "Step 3: Regional Pricing & Terms", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = "Freight and regional tiers dynamically adjust pricing per delivery profile",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Distance from Factory Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "1. Distance from Factory/Depot", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${config.distanceKm} km (${config.distanceBand})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = config.distanceKm.toFloat(),
                        onValueChange = { onUpdateRegionalConfig(it.toInt(), null, null, null) },
                        valueRange = 5f..150f,
                        steps = 28,
                        modifier = Modifier.testTag("distance_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 km (Local)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("50 km (Metro)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("150 km (Upcountry)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Regional Tier Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "2. Regional Tier", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val tiers = listOf(
                        "Tier 1 - Nairobi Urban",
                        "Tier 2 - Peri-Urban Hubs",
                        "Tier 3 - Remote / Upcountry"
                    )

                    tiers.forEach { tier ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateRegionalConfig(null, tier, null, null) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.regionalTier == tier,
                                onClick = { onUpdateRegionalConfig(null, tier, null, null) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = tier, fontSize = 12.sp, fontWeight = if (config.regionalTier == tier) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Date of Delivery & Urgency
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "3. Date of Delivery & Urgency", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = draftState.expectedDeliveryDate,
                        onValueChange = { onUpdatePaymentAndDelivery(null, null, it) },
                        label = { Text("Expected Delivery Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delivery_date_input"),
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Delivery Schedule Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    val urgencies = listOf(
                        "Standard Dispatch" to "Standard freight rate (2-3 business days)",
                        "Next-Day Priority" to "+30% priority dispatch premium",
                        "Weekend Express" to "+50% off-peak logistics charge"
                    )

                    urgencies.forEach { (urgency, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateRegionalConfig(null, null, urgency, null) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.deliveryUrgency == urgency,
                                onClick = { onUpdateRegionalConfig(null, null, urgency, null) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = urgency, fontSize = 12.sp, fontWeight = if (config.deliveryUrgency == urgency) FontWeight.Bold else FontWeight.Normal)
                                Text(text = desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tax Categorization & Payment Model
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Tax & Payment Model", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tax Categorization
                    Text(text = "Tax Categorization:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("VAT (16%)", "Zero-Rated (0%)", "Exempt (0%)").forEach { tax ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onUpdateRegionalConfig(null, null, null, tax) }
                            ) {
                                RadioButton(
                                    selected = config.taxCategory == tax,
                                    onClick = { onUpdateRegionalConfig(null, null, null, tax) }
                                )
                                Text(text = tax, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Payment Model Dropdown
                    Text(text = "Payment Model:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val paymentOptions = listOf(
                        "M-Pesa / Mobile Money",
                        "Cash on Delivery",
                        "Bank RTGS / EFT",
                        "Cheque on Dispatch",
                        "30-Day Credit Account"
                    )

                    var paymentExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = paymentExpanded,
                        onExpandedChange = { paymentExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = draftState.paymentModel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("payment_model_dropdown"),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false }
                        ) {
                            paymentOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onUpdatePaymentAndDelivery(option, null, null)
                                        paymentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Order Type
                    Text(text = "Order Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val orderTypeOptions = listOf(
                        "Standard Wholesale",
                        "Bulk Depot Distribution",
                        "Consignment Stock",
                        "Direct Retail Van Sale"
                    )
                    var orderTypeExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = orderTypeExpanded,
                        onExpandedChange = { orderTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = draftState.orderType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("order_type_dropdown"),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orderTypeExpanded) },
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = orderTypeExpanded,
                            onDismissRequest = { orderTypeExpanded = false }
                        ) {
                            orderTypeOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        onUpdatePaymentAndDelivery(null, opt, null)
                                        orderTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Calculation Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Dynamic Pricing Calculation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Products Subtotal:", fontSize = 11.sp)
                        Text(currencyFormatter.format(draftState.subtotal), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Regional Freight (${config.distanceKm}km, ${config.regionalTier.take(6)}):", fontSize = 11.sp)
                        Text(currencyFormatter.format(draftState.freightSurcharge), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax (${config.taxCategory}):", fontSize = 11.sp)
                        Text(currencyFormatter.format(draftState.taxAmount), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL BOOKING VALUE:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            currencyFormatter.format(draftState.grandTotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Navigation Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Back to Products")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.testTag("btn_proceed_to_review")
                ) {
                    Text("Review & Confirm")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: REVIEW & STAMP CONFIRMATION
// -------------------------------------------------------------
@Composable
fun StepOrderReviewAndStamp(
    draftState: BookingDraftState,
    repName: String,
    currencyFormatter: NumberFormat,
    onSubmitOrder: (onComplete: (OrderWithItems) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val customer = draftState.selectedCustomer ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Step 4: Final Review & Stamping", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "Verify order details prior to generating official client stamped copy", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Snapshot Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "CUSTOMER & LOCATION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = customer.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Contact: ${customer.contactPersonName} • Mobile: ${customer.contactMobile}", fontSize = 11.sp)
                    Text(text = "Location: ${customer.fullLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = "Sales Rep: $repName", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logistics & Terms Snapshot
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "REGIONAL LOGISTICS & TERMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Distance: ${draftState.regionalConfig.distanceKm} km (${draftState.regionalConfig.distanceBand})", fontSize = 11.sp)
                    Text(text = "Regional Setting: ${draftState.regionalConfig.regionalTier}", fontSize = 11.sp)
                    Text(text = "Expected Delivery: ${draftState.expectedDeliveryDate} (${draftState.regionalConfig.deliveryUrgency})", fontSize = 11.sp)
                    Text(text = "Payment Terms: ${draftState.paymentModel} • Order Type: ${draftState.orderType}", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products Table
            Text(text = "BOOKED ITEMS (${draftState.totalUnits} UNITS)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                draftState.cartItems.values.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.product.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = "${item.quantity} x ${currencyFormatter.format(item.unitPrice)} (${item.product.unit})", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = currencyFormatter.format(item.lineTotal),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", fontSize = 11.sp)
                    Text(currencyFormatter.format(draftState.subtotal), fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Regional Freight:", fontSize = 11.sp)
                    Text(currencyFormatter.format(draftState.freightSurcharge), fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax (${draftState.regionalConfig.taxCategory}):", fontSize = 11.sp)
                    Text(currencyFormatter.format(draftState.taxAmount), fontSize = 11.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GRAND TOTAL:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        currencyFormatter.format(draftState.grandTotal),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Submission Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        isSubmitting = true
                        onSubmitOrder {
                            isSubmitting = false
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.testTag("btn_confirm_stamp_order"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSubmitting) "Stamping..." else "Confirm & Generate Stamp")
                }
            }
        }
    }
}
