package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.StaffMember
import com.example.ui.OrderBookingViewModel

@Composable
fun ManageStaffScreen(viewModel: OrderBookingViewModel, onNavigateBack: () -> Unit) {
    val staffMembers by viewModel.allStaff.collectAsState(initial = emptyList())
    var selectedStaff by remember { mutableStateOf<StaffMember?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Staff Role Management", style = MaterialTheme.typography.headlineSmall)
        LazyColumn {
            items(staffMembers) { staff ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { selectedStaff = staff }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(staff.name, style = MaterialTheme.typography.titleMedium)
                        Text(staff.customRoleLabel, style = MaterialTheme.typography.bodyMedium)
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
