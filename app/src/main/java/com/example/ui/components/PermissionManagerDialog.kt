package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.StaffMember
import com.example.data.model.StaffModule

@Composable
fun PermissionManagerDialog(
    staffList: List<StaffMember>,
    onUpdatePermissions: (staffId: String, canInventory: Boolean, canOrders: Boolean, canCustomers: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, CUSTOM_ROLES, ADMINS
    var lastSavedStaffId by remember { mutableStateOf<String?>(null) }

    // Local mutable state map for live toggle adjustments before or as they save
    val permsState = remember(staffList) {
        mutableStateMapOf<String, Triple<Boolean, Boolean, Boolean>>().apply {
            staffList.forEach { staff ->
                put(
                    staff.id,
                    Triple(
                        staff.canAccessInventory,
                        staff.canAccessOrders,
                        staff.canAccessCustomers
                    )
                )
            }
        }
    }

    val filteredStaff = staffList.filter { staff ->
        val matchesQuery = searchQuery.isBlank() ||
                staff.name.contains(searchQuery, ignoreCase = true) ||
                staff.customRoleLabel.contains(searchQuery, ignoreCase = true) ||
                staff.email.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ADMINS" -> staff.isAdmin
            "CUSTOM_ROLES" -> !staff.isAdmin
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .testTag("permission_manager_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Imperial Gold and Security branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Module Permission Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Define module access for each staff role (Admin Console)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_permission_manager")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Info banner explaining the three core modules
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Controlled Modules: Inventory • Orders • Customers",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unchecked modules are hidden from that staff member's navigation bar. Admin accounts retain supervisory access.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar & Filter Chips
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter staff by name or role...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("perm_manager_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All Staff (${staffList.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_perm_all")
                    )
                    FilterChip(
                        selected = selectedFilter == "CUSTOM_ROLES",
                        onClick = { selectedFilter = "CUSTOM_ROLES" },
                        label = { Text("Field & Custom Roles", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_perm_custom")
                    )
                    FilterChip(
                        selected = selectedFilter == "ADMINS",
                        onClick = { selectedFilter = "ADMINS" },
                        label = { Text("Admins", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_perm_admins")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Staff Permissions List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("perm_manager_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStaff, key = { it.id }) { staff ->
                        val currentPerms = permsState[staff.id] ?: Triple(
                            staff.canAccessInventory,
                            staff.canAccessOrders,
                            staff.canAccessCustomers
                        )
                        val (canInventory, canOrders, canCustomers) = currentPerms

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("perm_card_${staff.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (staff.isAdmin) Color(0xFFFDE68A) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Staff Title & Role
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = staff.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (staff.isAdmin) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFD4AF37))
                                                ) {
                                                    Text(
                                                        text = "ADMINISTRATOR",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Role: ${staff.customRoleLabel}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${staff.email} • ${staff.phone}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (lastSavedStaffId == staff.id) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.testTag("saved_indicator_${staff.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Saved",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF059669)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                if (staff.isAdmin) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Administrator account has full unrestricted access to Inventory, Orders, and Customers modules.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                } else {
                                    // Granular Module Toggles
                                    Text(
                                        text = "ACCESSIBLE MODULES",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 1. Inventory Module Row
                                    ModuleToggleRow(
                                        module = StaffModule.INVENTORY,
                                        icon = Icons.Default.Inventory,
                                        isChecked = canInventory,
                                        onCheckedChange = { checked ->
                                            permsState[staff.id] = Triple(checked, canOrders, canCustomers)
                                            onUpdatePermissions(staff.id, checked, canOrders, canCustomers)
                                            lastSavedStaffId = staff.id
                                        },
                                        testTag = "switch_perm_inventory_${staff.id}"
                                    )

                                    // 2. Orders Module Row
                                    ModuleToggleRow(
                                        module = StaffModule.ORDERS,
                                        icon = Icons.Default.Receipt,
                                        isChecked = canOrders,
                                        onCheckedChange = { checked ->
                                            permsState[staff.id] = Triple(canInventory, checked, canCustomers)
                                            onUpdatePermissions(staff.id, canInventory, checked, canCustomers)
                                            lastSavedStaffId = staff.id
                                        },
                                        testTag = "switch_perm_orders_${staff.id}"
                                    )

                                    // 3. Customers Module Row
                                    ModuleToggleRow(
                                        module = StaffModule.CUSTOMERS,
                                        icon = Icons.Default.People,
                                        isChecked = canCustomers,
                                        onCheckedChange = { checked ->
                                            permsState[staff.id] = Triple(canInventory, canOrders, checked)
                                            onUpdatePermissions(staff.id, canInventory, canOrders, checked)
                                            lastSavedStaffId = staff.id
                                        },
                                        testTag = "switch_perm_customers_${staff.id}"
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Role Presets Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Presets:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                                        OutlinedButton(
                                            onClick = {
                                                permsState[staff.id] = Triple(true, true, true)
                                                onUpdatePermissions(staff.id, true, true, true)
                                                lastSavedStaffId = staff.id
                                            },
                                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                                            modifier = Modifier.testTag("btn_preset_all_${staff.id}")
                                        ) {
                                            Text("All Modules", fontSize = 9.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                permsState[staff.id] = Triple(false, true, true)
                                                onUpdatePermissions(staff.id, false, true, true)
                                                lastSavedStaffId = staff.id
                                            },
                                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                                            modifier = Modifier.testTag("btn_preset_sales_${staff.id}")
                                        ) {
                                            Text("Field Sales", fontSize = 9.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                permsState[staff.id] = Triple(true, false, false)
                                                onUpdatePermissions(staff.id, true, false, false)
                                                lastSavedStaffId = staff.id
                                            },
                                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                                            modifier = Modifier.testTag("btn_preset_warehouse_${staff.id}")
                                        ) {
                                            Text("Warehouse", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_done_permission_manager"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done • All Permissions Applied")
                }
            }
        }
    }
}

@Composable
private fun ModuleToggleRow(
    module: StaffModule,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = module.title,
                    fontSize = 11.sp,
                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = module.description,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
