package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.R
import com.example.data.model.StaffMember
import com.example.ui.OrderBookingViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStaffScreen(viewModel: OrderBookingViewModel, onNavigateBack: () -> Unit) {
    BackHandler { onNavigateBack() }
    val staffMembers by viewModel.allStaff.collectAsState(initial = emptyList())
    var selectedStaff by remember { mutableStateOf<StaffMember?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Account Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.zayed_logo),
                        contentDescription = "Zayed Corporate Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("System Users & Privilege Control", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(staffMembers) { staff ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedStaff = staff },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(staff.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${staff.customRoleLabel} • ${staff.email}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (staff.isAdmin) {
                                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                    Text("ADMIN", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
            
            selectedStaff?.let { staff ->
                StaffPermissionsDialog(
                    staff = staff,
                    onDismiss = { selectedStaff = null },
                    onSave = { updatedStaff ->
                        viewModel.updateStaff(updatedStaff)
                        selectedStaff = null
                    }
                )
            }
        }
    }
}

@Composable
fun StaffPermissionsDialog(
    staff: StaffMember,
    onDismiss: () -> Unit,
    onSave: (StaffMember) -> Unit
) {
    var canOnboard by remember { mutableStateOf(staff.canOnboardCustomers) }
    var canPlaceOrders by remember { mutableStateOf(staff.canPlaceOrders) }
    var canApproveSuppliers by remember { mutableStateOf(staff.canApproveSuppliers) }
    var canManageStaff by remember { mutableStateOf(staff.canManageStaff) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions: ${staff.name}") },
        text = {
            Column {
                CheckboxRow("Onboard Customers", canOnboard) { canOnboard = it }
                CheckboxRow("Place Orders", canPlaceOrders) { canPlaceOrders = it }
                CheckboxRow("Approve Suppliers", canApproveSuppliers) { canApproveSuppliers = it }
                CheckboxRow("Manage Staff", canManageStaff) { canManageStaff = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(staff.copy(
                    canOnboardCustomers = canOnboard,
                    canPlaceOrders = canPlaceOrders,
                    canApproveSuppliers = canApproveSuppliers,
                    canManageStaff = canManageStaff
                ))
            }) { Text("Save") }
        }
    )
}

@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
