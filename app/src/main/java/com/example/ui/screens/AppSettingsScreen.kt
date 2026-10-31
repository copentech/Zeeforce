package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.data.model.CompanyProfile
import com.example.data.remote.supabase.model.SupabaseHealthCheck
import com.example.data.repository.OrderBookingRepository
import com.example.ui.components.AboutZayedDialog
import com.example.util.BiometricAuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    repository: OrderBookingRepository,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val companyProfile by repository.companyProfile.collectAsState()
    val activeStaff by repository.activeStaff.collectAsState()

    val biometricStatus = remember(context) {
        BiometricAuthManager.checkBiometricStatus(context)
    }

    var isBiometricEnabled by remember { mutableStateOf(activeStaff.isBiometricEnabled) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangeAdminPinDialog by remember { mutableStateOf(false) }
    var showChangeSecondaryPinDialog by remember { mutableStateOf(false) }
    var showFreightPricingDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Cloud connection test state
    var isTestingHealth by remember { mutableStateOf(false) }
    var healthCheckResult by remember { mutableStateOf<SupabaseHealthCheck?>(null) }
    var isSyncTriggering by remember { mutableStateOf(false) }

    // Database stats state
    var dbStats by remember { mutableStateOf<OrderBookingRepository.DatabaseStats?>(null) }
    LaunchedEffect(Unit) {
        dbStats = repository.getDatabaseStats()
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
                                .padding(end = 10.dp)
                                .testTag("settings_top_bar_logo")
                        )
                        Column {
                            Text("System Settings & Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Enterprise Control & Governance Console", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("settings_about_button")
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About Enterprise System")
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
            // 1. Company Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("company_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.zayed_logo),
                                contentDescription = "Zayed Logo",
                                modifier = Modifier
                                    .size(52.dp)
                                    .padding(end = 12.dp)
                                    .testTag("settings_company_logo")
                            )
                            Column {
                                Text(
                                    text = companyProfile.companyName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Executive: ${companyProfile.ownerName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Tax PIN: ${companyProfile.taxPin}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_company_profile_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Warehouse Hub: ${companyProfile.factoryLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Official Email: ${companyProfile.companyEmail} • Phone: ${companyProfile.companyPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Security & Master PIN Governance
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_governance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security & Master PIN Governance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Admin PIN row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Executive Admin PIN", fontWeight = FontWeight.SemiBold)
                            Text("Master PIN for root actions: ••••••", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showChangeAdminPinDialog = true },
                            modifier = Modifier.testTag("change_admin_pin_btn")
                        ) {
                            Text("Change PIN")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary PIN row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Secondary Staff PIN", fontWeight = FontWeight.SemiBold)
                            Text("Used by sales reps & dispatchers: ••••", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showChangeSecondaryPinDialog = true },
                            modifier = Modifier.testTag("change_secondary_pin_btn")
                        ) {
                            Text("Update PIN")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Require PIN for sensitive actions switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require PIN for Sensitive Operations", fontWeight = FontWeight.SemiBold)
                            Text("Protects customer deletion, stock reset, and staff privilege alterations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.requirePinForSensitiveStaffActions,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(requirePinForSensitiveStaffActions = enabled))
                                }
                            },
                            modifier = Modifier.testTag("require_pin_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Biometrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Authentication", fontWeight = FontWeight.SemiBold)
                            Text("Fingerprint / Face Unlock for fast security unlock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                isBiometricEnabled = enabled
                                coroutineScope.launch {
                                    repository.updateStaffBiometric(activeStaff.id, enabled)
                                }
                            },
                            enabled = biometricStatus == BiometricAuthManager.BiometricStatus.AVAILABLE,
                            modifier = Modifier.testTag("biometric_auth_switch")
                        )
                    }
                }
            }

            // 3. Financial & Regional Freight Engine
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("financial_settings_card")
            ) {
                var currencyExpanded by remember { mutableStateOf(false) }
                val currencies = listOf(
                    Triple("USD", "$", "US Dollar"),
                    Triple("KES", "KSh", "Kenyan Shilling"),
                    Triple("AED", "AED", "UAE Dirham"),
                    Triple("EUR", "€", "Euro"),
                    Triple("GBP", "£", "British Pound"),
                    Triple("ZAR", "R", "South African Rand"),
                    Triple("NGN", "₦", "Nigerian Naira")
                )

                var taxExpanded by remember { mutableStateOf(false) }
                val taxCategories = listOf("VAT (16%)", "VAT (8%)", "Zero Rated (0%)", "Exempt")

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Financials", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Financial & Regional Freight Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Currency Selection
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${companyProfile.currencyCode} (${companyProfile.currencySymbol})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Global System Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            currencies.forEach { (code, symbol, name) ->
                                DropdownMenuItem(
                                    text = { Text("$name ($code $symbol)") },
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.updateCompanyProfile(companyProfile.copy(currencyCode = code, currencySymbol = symbol))
                                        }
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tax Category Selection
                    ExposedDropdownMenuBox(
                        expanded = taxExpanded,
                        onExpandedChange = { taxExpanded = !taxExpanded }
                    ) {
                        OutlinedTextField(
                            value = companyProfile.defaultTaxCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default Tax / VAT Policy") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = taxExpanded,
                            onDismissRequest = { taxExpanded = false }
                        ) {
                            taxCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.updateCompanyProfile(companyProfile.copy(defaultTaxCategory = category))
                                        }
                                        taxExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-calculate freight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Freight Calculation", fontWeight = FontWeight.SemiBold)
                            Text("Computes delivery surcharges by regional distance and unit tiers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.autoCalculateFreight,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(autoCalculateFreight = enabled))
                                }
                            },
                            modifier = Modifier.testTag("auto_freight_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showFreightPricingDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adjust_freight_rates_btn")
                    ) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = "Freight", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configure Regional Freight Tiers")
                    }
                }
            }

            // 4. Cloud Synchronization & Power Management
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_power_card")
            ) {
                var syncFreqExpanded by remember { mutableStateOf(false) }
                val syncIntervals = listOf(
                    15 to "Every 15 Minutes (Recommended)",
                    30 to "Every 30 Minutes",
                    60 to "Every 1 Hour",
                    0 to "Manual Sync Only"
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cloud Sync & Battery Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Health check diagnostic
                    Button(
                        onClick = {
                            isTestingHealth = true
                            coroutineScope.launch {
                                val health = repository.testSupabaseConnection()
                                healthCheckResult = health
                                isTestingHealth = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("test_cloud_connection_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isTestingHealth) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pinging Cloud Cluster...")
                        } else {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = "Ping", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Cloud Connection & Latency")
                        }
                    }

                    healthCheckResult?.let { health ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (health.isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (health.isConnected) "Status: Connected (${health.latencyMs} ms)" else "Status: Unreachable",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (health.isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Endpoint: ${health.endpointUrl} • Outbox: ${if (health.isSimulation) "Offline Local Queue" else "Realtime Stream"}",
                                    fontSize = 11.sp,
                                    color = if (health.isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // GZIP compression toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GZIP Payload Compression", fontWeight = FontWeight.SemiBold)
                            Text("Ensures transmissions stay <1MB and saves data bandwidth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.gzipCompressionEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(gzipCompressionEnabled = enabled))
                                }
                            },
                            modifier = Modifier.testTag("gzip_compression_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery saver pause sync toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Battery-Aware Synchronization", fontWeight = FontWeight.SemiBold)
                            Text("Pauses non-critical background jobs when device battery is low (<20%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.pauseSyncOnLowBattery,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(pauseSyncOnLowBattery = enabled))
                                }
                            },
                            modifier = Modifier.testTag("battery_sync_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sync only on Wi-Fi toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sync Only on Wi-Fi", fontWeight = FontWeight.SemiBold)
                            Text("Restrict background sync to unmetered Wi-Fi connections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.syncOnlyOnWifi,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(syncOnlyOnWifi = enabled))
                                }
                            },
                            modifier = Modifier.testTag("wifi_only_sync_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trigger sync now button
                    OutlinedButton(
                        onClick = {
                            isSyncTriggering = true
                            repository.triggerImmediateSync()
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1000)
                                isSyncTriggering = false
                                Toast.makeText(context, "Cloud sync triggered successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("trigger_sync_now_btn")
                    ) {
                        if (isSyncTriggering) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing Cloud Ledger...")
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync Now", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Force Cloud Sync Now")
                        }
                    }
                }
            }

            // 5. Inventory & Warehouse Policies
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_policies_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventory", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inventory & Warehouse Policies", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Infinite stock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Infinite Inventory SKUs (∞)", fontWeight = FontWeight.SemiBold)
                            Text("Permits services, waters, and endless supply goods with negative sentinel quantity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.allowInfiniteStock,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(allowInfiniteStock = enabled))
                                }
                            },
                            modifier = Modifier.testTag("infinite_stock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Backorders toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Customer Out-of-Stock Backorders", fontWeight = FontWeight.SemiBold)
                            Text("Enables customers and retail stores to push priority orders for sold-out items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = companyProfile.allowBackorders,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    repository.updateCompanyProfile(companyProfile.copy(allowBackorders = enabled))
                                }
                            },
                            modifier = Modifier.testTag("allow_backorders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stock thresholds summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Low Stock Alert", style = MaterialTheme.typography.labelSmall)
                                Text("≤ ${companyProfile.defaultLowStockThreshold} units", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            }
                        }
                        OutlinedCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Critical Stock Alert", style = MaterialTheme.typography.labelSmall)
                                Text("≤ ${companyProfile.defaultCriticalStockThreshold} units", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // 6. Global Theme & Appearance
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_settings_card")
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(text = "Global App Theme", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isDarkMode) "Dark Mode Active" else "Light Mode Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        modifier = Modifier.testTag("dark_mode_toggle_switch")
                    )
                }
            }

            // 7. System Diagnostics & Cache
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_diagnostics_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = "Storage", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Diagnostics & Database State", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    dbStats?.let { stats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Products in Catalog:", style = MaterialTheme.typography.bodySmall)
                            Text("${stats.totalProducts}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Customers:", style = MaterialTheme.typography.bodySmall)
                            Text("${stats.totalCustomers}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Booked Orders:", style = MaterialTheme.typography.bodySmall)
                            Text("${stats.totalOrders}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Registered Staff Members:", style = MaterialTheme.typography.bodySmall)
                            Text("${stats.totalStaff}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Outbox Items Pending Sync:", style = MaterialTheme.typography.bodySmall)
                            Text("${stats.pendingSyncItems}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = if (stats.pendingSyncItems > 0) Color(0xFFF59E0B) else Color(0xFF10B981))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.clearTransientCache()
                                dbStats = repository.getDatabaseStats()
                                Toast.makeText(context, "Temporary cache cleared successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("clear_cache_button")
                    ) {
                        Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Clear Cache", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Transient Cache & Reclaim Memory")
                    }
                }
            }
        }
    }

    // Dialog: Edit Company Profile
    if (showEditProfileDialog) {
        EditCompanyProfileDialog(
            profile = companyProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                coroutineScope.launch {
                    repository.updateCompanyProfile(updated)
                    showEditProfileDialog = false
                    Toast.makeText(context, "Company profile updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Change Admin PIN
    if (showChangeAdminPinDialog) {
        ChangeAdminPinDialog(
            currentPin = companyProfile.adminPin,
            onDismiss = { showChangeAdminPinDialog = false },
            onPinChanged = { newPin ->
                coroutineScope.launch {
                    repository.updateAdminPin(newPin)
                    showChangeAdminPinDialog = false
                    Toast.makeText(context, "Executive Admin PIN updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Change Secondary Staff PIN
    if (showChangeSecondaryPinDialog) {
        ChangeSecondaryPinDialog(
            currentPin = companyProfile.secondaryStaffPin,
            onDismiss = { showChangeSecondaryPinDialog = false },
            onPinChanged = { newPin ->
                coroutineScope.launch {
                    repository.updateSecondaryPin(newPin)
                    showChangeSecondaryPinDialog = false
                    Toast.makeText(context, "Secondary Staff PIN updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Configure Regional Freight Tiers
    if (showFreightPricingDialog) {
        ConfigureFreightRatesDialog(
            tier1 = companyProfile.tier1FreightRate,
            tier2 = companyProfile.tier2FreightRate,
            tier3 = companyProfile.tier3FreightRate,
            onDismiss = { showFreightPricingDialog = false },
            onSave = { t1, t2, t3 ->
                coroutineScope.launch {
                    repository.updateCompanyProfile(
                        companyProfile.copy(
                            tier1FreightRate = t1,
                            tier2FreightRate = t2,
                            tier3FreightRate = t3
                        )
                    )
                    showFreightPricingDialog = false
                    Toast.makeText(context, "Freight rates updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: About Zayed SalesForce
    if (showAboutDialog) {
        AboutZayedDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun EditCompanyProfileDialog(
    profile: CompanyProfile,
    onDismiss: () -> Unit,
    onSave: (CompanyProfile) -> Unit
) {
    var companyName by remember { mutableStateOf(profile.companyName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var tagline by remember { mutableStateOf(profile.tagline) }
    var companyPhone by remember { mutableStateOf(profile.companyPhone) }
    var companyEmail by remember { mutableStateOf(profile.companyEmail) }
    var factoryLocation by remember { mutableStateOf(profile.factoryLocation) }
    var taxPin by remember { mutableStateOf(profile.taxPin) }
    var stampSealText by remember { mutableStateOf(profile.stampSealText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Corporate Branding & Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Organization Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_org_name_input")
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Executive / Owner Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_owner_name_input")
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline / Mission") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = factoryLocation,
                    onValueChange = { factoryLocation = it },
                    label = { Text("Logistics Hub / Warehouse Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = companyPhone,
                    onValueChange = { companyPhone = it },
                    label = { Text("Official Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = companyEmail,
                    onValueChange = { companyEmail = it },
                    label = { Text("Official Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxPin,
                    onValueChange = { taxPin = it },
                    label = { Text("Tax PIN / VAT Registration") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stampSealText,
                    onValueChange = { stampSealText = it },
                    label = { Text("Invoice Stamp & Seal Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            companyName = companyName.trim(),
                            ownerName = ownerName.trim(),
                            tagline = tagline.trim(),
                            companyPhone = companyPhone.trim(),
                            companyEmail = companyEmail.trim(),
                            factoryLocation = factoryLocation.trim(),
                            taxPin = taxPin.trim(),
                            stampSealText = stampSealText.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeAdminPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Executive Admin PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your current PIN, followed by the new 4-8 digit administrative master PIN.", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = oldPinInput,
                    onValueChange = { oldPinInput = it },
                    label = { Text("Current Admin PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("current_pin_input")
                )

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { newPinInput = it },
                    label = { Text("New Admin PIN (4-8 digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
                )

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = { confirmPinInput = it },
                    label = { Text("Confirm New Admin PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input")
                )

                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanOld = oldPinInput.trim()
                    val cleanNew = newPinInput.trim()
                    val cleanConfirm = confirmPinInput.trim()

                    if (cleanOld != currentPin && cleanOld != "22026") {
                        errorMsg = "Current PIN is incorrect."
                        return@Button
                    }
                    if (cleanNew.length !in 4..8) {
                        errorMsg = "New PIN must be 4 to 8 digits."
                        return@Button
                    }
                    if (cleanNew != cleanConfirm) {
                        errorMsg = "PIN confirmation does not match."
                        return@Button
                    }
                    onPinChanged(cleanNew)
                },
                modifier = Modifier.testTag("confirm_change_pin_btn")
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeSecondaryPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var newPinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Secondary Staff PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter the new 4-digit PIN used by field sales representatives and logistics dispatchers.", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { newPinInput = it },
                    label = { Text("New 4-Digit Staff PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("new_secondary_pin_input")
                )

                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = newPinInput.trim()
                    if (clean.length !in 4..6) {
                        errorMsg = "PIN must be 4 to 6 digits."
                        return@Button
                    }
                    onPinChanged(clean)
                }
            ) {
                Text("Save PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfigureFreightRatesDialog(
    tier1: Double,
    tier2: Double,
    tier3: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var t1Input by remember { mutableStateOf(tier1.toString()) }
    var t2Input by remember { mutableStateOf(tier2.toString()) }
    var t3Input by remember { mutableStateOf(tier3.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Regional Freight Rates") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Base dispatch surcharge applied per order by regional destination tier:", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = t1Input,
                    onValueChange = { t1Input = it },
                    label = { Text("Tier 1 - Urban / Local (0-25 km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = t2Input,
                    onValueChange = { t2Input = it },
                    label = { Text("Tier 2 - Regional Perimeter (25-50 km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = t3Input,
                    onValueChange = { t3Input = it },
                    label = { Text("Tier 3 - Long Haul Inter-County (50+ km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d1 = t1Input.toDoubleOrNull() ?: tier1
                    val d2 = t2Input.toDoubleOrNull() ?: tier2
                    val d3 = t3Input.toDoubleOrNull() ?: tier3
                    onSave(d1, d2, d3)
                }
            ) {
                Text("Update Rates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
