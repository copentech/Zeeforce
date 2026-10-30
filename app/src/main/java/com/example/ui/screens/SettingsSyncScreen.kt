package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CompanyProfile
import com.example.data.model.StaffMember
import com.example.data.remote.SyncResult

@Composable
fun SettingsSyncScreen(
    companyProfile: CompanyProfile,
    activeStaff: StaffMember,
    isOnline: Boolean,
    pendingSyncCount: Int,
    isSyncing: Boolean,
    lastSyncResult: SyncResult?,
    onLogout: () -> Unit,
    onOpenAboutDialog: () -> Unit,
    onToggleNetworkMode: () -> Unit,
    onUpdateProfile: (CompanyProfile) -> Unit,
    onTriggerSync: () -> Unit,
    supabaseHealth: com.example.data.remote.supabase.model.SupabaseHealthCheck? = null,
    isTestingSupabase: Boolean = false,
    onTestSupabase: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var supabaseUrl by remember { mutableStateOf(companyProfile.supabaseUrl) }
    var supabaseAnonKey by remember { mutableStateOf(companyProfile.supabaseAnonKey) }

    var companyName by remember { mutableStateOf(companyProfile.companyName) }
    var ownerName by remember { mutableStateOf(companyProfile.ownerName) }
    var tagline by remember { mutableStateOf(companyProfile.tagline) }
    var factoryLocation by remember { mutableStateOf(companyProfile.factoryLocation) }
    var companyPhone by remember { mutableStateOf(companyProfile.companyPhone) }
    var companyEmail by remember { mutableStateOf(companyProfile.companyEmail) }
    var taxPin by remember { mutableStateOf(companyProfile.taxPin) }
    var adminPin by remember { mutableStateOf(companyProfile.adminPin) }

    var savedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title with About Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Settings & Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Zayed SalesForce • Al Yasir Bin Zayed", fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenAboutDialog,
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                modifier = Modifier.testTag("btn_open_about_from_settings")
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("About", fontSize = 11.sp)
            }
        }

        // 1. Active Staff Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "AUTHENTICATED STAFF PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = activeStaff.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = activeStaff.customRoleLabel, fontSize = 12.sp, color = if (activeStaff.isAdmin) Color(0xFFB45309) else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(text = "${activeStaff.email} • ${activeStaff.phone}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (activeStaff.isAdmin) {
                                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                                    Text("Full Admin (All Modules)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            } else {
                                if (activeStaff.canAccessOrders) {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text("Orders", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                if (activeStaff.canAccessInventory) {
                                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text("Inventory", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                if (activeStaff.canAccessCustomers) {
                                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text("Customers", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.testTag("btn_logout_from_settings")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Out", fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. Connectivity & Low-Bandwidth Sync Station (< 1 Mbps)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Low-Bandwidth Sync Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (isOnline) "Online" else "Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { onToggleNetworkMode() },
                            modifier = Modifier.testTag("toggle_network_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Optimized for <1Mbps remote connections: delta changes are queued offline and compressed into minimal JSON packets (<2KB) upon reconnection.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (pendingSyncCount > 0) "$pendingSyncCount items queued in outbox" else "All local data synchronized",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (pendingSyncCount > 0) Color(0xFFD97706) else Color(0xFF059669)
                        )
                        lastSyncResult?.let { res ->
                            Text(text = res.message, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Button(
                        onClick = onTriggerSync,
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("btn_trigger_sync_now"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Syncing...", fontSize = 11.sp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Now", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. Supabase BaaS Client & Authentication Gateway
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Supabase Client & Auth Gateway", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    val isConfigured = supabaseUrl.isNotBlank() && !supabaseUrl.contains("example-project") && !supabaseUrl.contains("your-project")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isConfigured) Color(0xFFE8F5E9) else Color(0xFFEDE7F6)
                    ) {
                        Text(
                            text = if (isConfigured) "Live Gateway" else "Simulated Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfigured) Color(0xFF2E7D32) else Color(0xFF512DA8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Initial Supabase client configured for remote synchronization (/rest/v1) and user authentication (/auth/v1). Secrets can be managed via the AI Studio Secrets panel.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supabaseUrl,
                    onValueChange = { supabaseUrl = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth().testTag("input_supabase_url"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supabaseAnonKey,
                    onValueChange = { supabaseAnonKey = it },
                    label = { Text("Supabase Anon API Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsIn...") },
                    modifier = Modifier.fillMaxWidth().testTag("input_supabase_key"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Connection Test Action & Health Result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onTestSupabase,
                        enabled = !isTestingSupabase,
                        modifier = Modifier.testTag("btn_test_supabase_ping")
                    ) {
                        if (isTestingSupabase) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pinging...", fontSize = 11.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Supabase Ping", fontSize = 11.sp)
                        }
                    }

                    supabaseHealth?.let { health ->
                        Text(
                            text = if (health.isConnected) "${health.latencyMs}ms • Connected" else "Unreachable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (health.isConnected) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }

                supabaseHealth?.let { health ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (health.isConnected) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = health.message,
                            fontSize = 10.sp,
                            color = if (health.isConnected) Color(0xFF166534) else Color(0xFF991B1B),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // 4. Company Profile & Ownership (Al Yasir Bin Zayed)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Enterprise Ownership & Security", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("System / Enterprise Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_company_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("System Owner") },
                    modifier = Modifier.fillMaxWidth().testTag("input_owner_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = factoryLocation,
                    onValueChange = { factoryLocation = it },
                    label = { Text("Logistics Hub / Depot Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = companyPhone,
                        onValueChange = { companyPhone = it },
                        label = { Text("Headquarters Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = adminPin,
                        onValueChange = { if (it.length <= 6) adminPin = it },
                        label = { Text("Admin Security PIN") },
                        modifier = Modifier.weight(1f).testTag("input_admin_pin"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = companyEmail,
                    onValueChange = { companyEmail = it },
                    label = { Text("Official Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        savedMessage?.let { msg ->
            Text(text = msg, color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        if (activeStaff.isAdmin) {
            Button(
                onClick = onOpenAdminDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("btn_open_admin_dashboard_module"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Secure Admin Dashboard & Activity Logs (PIN 22026)", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            }
        }

        // Save All Changes Button
        Button(
            onClick = {
                val updated = companyProfile.copy(
                    supabaseUrl = supabaseUrl.trim(),
                    supabaseAnonKey = supabaseAnonKey.trim(),
                    companyName = companyName.trim(),
                    ownerName = ownerName.trim(),
                    tagline = tagline.trim(),
                    factoryLocation = factoryLocation.trim(),
                    companyPhone = companyPhone.trim(),
                    companyEmail = companyEmail.trim(),
                    adminPin = adminPin.trim()
                )
                onUpdateProfile(updated)
                savedMessage = "Zayed SalesForce settings updated successfully!"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .testTag("btn_save_settings"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Zayed SalesForce Configuration")
        }
    }
}
