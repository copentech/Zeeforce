package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.ui.components.AdminConfirmDeleteCustomerDialog
import com.example.ui.components.AdminDeletionRequestsDialog
import com.example.ui.components.RequestCustomerDeletionDialog

@Composable
fun CustomerDirectoryScreen(
    customers: List<Customer>,
    pendingDeletionRequests: List<Customer>,
    isAdmin: Boolean,
    activeStaffName: String,
    onAddCustomer: (
        customerName: String,
        businessName: String,
        fullLocation: String,
        businessEmail: String,
        contactPersonName: String,
        contactPosition: String,
        contactMobile: String,
        contactSecondaryPhone: String,
        companyEmail: String
    ) -> Unit,
    onBookOrderForCustomer: (Customer) -> Unit,
    onRequestDeleteCustomer: (Customer, reason: String) -> Unit,
    onArchiveCustomer: (Customer) -> Unit = {},
    onRestoreCustomer: (Customer) -> Unit = {},
    onDirectDeleteCustomer: (Customer) -> Unit,
    onApproveDeleteCustomer: (Customer) -> Unit,
    onDismissDeleteRequest: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Active Clients, 1: Archived Records
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showPendingRequestsDialog by remember { mutableStateOf(false) }

    var customerToRequestDeletion by remember { mutableStateOf<Customer?>(null) }
    var customerToDirectDelete by remember { mutableStateOf<Customer?>(null) }

    val activeCount = remember(customers) { customers.count { !it.isArchived } }
    val archivedCount = remember(customers) { customers.count { it.isArchived } }

    val filteredCustomers = customers.filter {
        val matchesTab = if (selectedTab == 1) it.isArchived else !it.isArchived
        val matchesQuery = searchQuery.isBlank() ||
                it.businessName.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.fullLocation.contains(searchQuery, ignoreCase = true) ||
                it.contactPersonName.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesQuery
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
                    Text(text = "Customer Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${customers.size} registered wholesale clients",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = { showAddCustomerDialog = true },
                    modifier = Modifier.testTag("btn_open_add_customer_dialog"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Client", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active vs Archived Tab Pills
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Active Clients ($activeCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedTab == 1) Color(0xFFD4AF37) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Archived Records ($archivedCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Admin Review Banner if pending deletion requests
            if (isAdmin && pendingDeletionRequests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPendingRequestsDialog = true }
                        .testTag("banner_pending_deletion_requests"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${pendingDeletionRequests.size} Deletion Request(s) Pending",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Requires Admin authorization to permanently remove",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }

                        Button(
                            onClick = { showPendingRequestsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Text("Review", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_directory_search"),
                placeholder = { Text("Search by customer, business, location...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (customers.isEmpty()) "No clients registered" else "No matching clients",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerDetailCardWithGovernance(
                            customer = customer,
                            isAdmin = isAdmin,
                            onBookOrder = { onBookOrderForCustomer(customer) },
                            onArchive = { onArchiveCustomer(customer) },
                            onRestore = { onRestoreCustomer(customer) },
                            onRequestDeletion = { customerToRequestDeletion = customer },
                            onDirectDelete = { customerToDirectDelete = customer }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddCustomerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_customer"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerModalDialog(
            repName = activeStaffName,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, bName, loc, bEmail, cName, pos, mob, secMob, cEmail ->
                onAddCustomer(name, bName, loc, bEmail, cName, pos, mob, secMob, cEmail)
                showAddCustomerDialog = false
            }
        )
    }

    // Non-admin Deletion Request Modal
    customerToRequestDeletion?.let { customer ->
        RequestCustomerDeletionDialog(
            customer = customer,
            staffName = activeStaffName,
            onDismiss = { customerToRequestDeletion = null },
            onSubmitRequest = { reason ->
                onRequestDeleteCustomer(customer, reason)
                customerToRequestDeletion = null
            }
        )
    }

    // Admin Direct Delete Modal
    customerToDirectDelete?.let { customer ->
        AdminConfirmDeleteCustomerDialog(
            customer = customer,
            onDismiss = { customerToDirectDelete = null },
            onConfirmDelete = {
                onDirectDeleteCustomer(customer)
                customerToDirectDelete = null
            }
        )
    }

    // Admin Deletion Requests Review Dialog
    if (showPendingRequestsDialog) {
        AdminDeletionRequestsDialog(
            pendingRequests = pendingDeletionRequests,
            onApproveDelete = { customer ->
                onApproveDeleteCustomer(customer)
            },
            onDismissRequest = { customer ->
                onDismissDeleteRequest(customer)
            },
            onClose = { showPendingRequestsDialog = false }
        )
    }
}

@Composable
fun CustomerDetailCardWithGovernance(
    customer: Customer,
    isAdmin: Boolean,
    onBookOrder: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onRequestDeletion: () -> Unit,
    onDirectDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Pending deletion warning banner on card
            if (customer.pendingDeletionRequested) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Archive/Deletion requested: ${customer.deletionRequestReason}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.businessName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Owner / Client Name: ${customer.customerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onBookOrder,
                    modifier = Modifier.testTag("btn_book_order_for_${customer.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Book Order", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = customer.fullLocation,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Contact Person Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "CONTACT PERSON: ${customer.contactPersonName.uppercase()} (${customer.contactPosition})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Primary Mobile: ${customer.contactMobile}", fontSize = 11.sp)
                    if (customer.contactSecondaryPhone.isNotBlank()) {
                        Text(text = "Secondary Phone: ${customer.contactSecondaryPhone}", fontSize = 11.sp)
                    }
                    Text(text = "Corporate Email: ${customer.companyEmail}", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with Rep info & Deletion Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Captured: ${customer.capturedByAccountHolder}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (customer.isSynced) "Synced with Cloud" else "Queued in Outbox",
                        fontSize = 10.sp,
                        color = if (customer.isSynced) Color(0xFF059669) else Color(0xFFD97706),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (customer.isArchived) {
                        // Customer is archived: allow restoring
                        Button(
                            onClick = onRestore,
                            modifier = Modifier.testTag("restore_customer_${customer.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Restore", fontSize = 10.sp)
                        }

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = onDirectDelete,
                                modifier = Modifier.testTag("admin_delete_customer_${customer.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text("Purge", fontSize = 10.sp)
                            }
                        }
                    } else {
                        // Customer is active: allow safe archival instead of destructive delete
                        OutlinedButton(
                            onClick = onArchive,
                            modifier = Modifier.testTag("archive_customer_${customer.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Archive", fontSize = 10.sp)
                        }

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = onDirectDelete,
                                modifier = Modifier.testTag("admin_delete_customer_${customer.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Delete", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerModalDialog(
    repName: String,
    onDismiss: () -> Unit,
    onSave: (
        customerName: String,
        businessName: String,
        fullLocation: String,
        businessEmail: String,
        contactPersonName: String,
        contactPosition: String,
        contactMobile: String,
        contactSecondaryPhone: String,
        companyEmail: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var fullLocation by remember { mutableStateOf("") }
    var businessEmail by remember { mutableStateOf("") }

    var contactPersonName by remember { mutableStateOf("") }
    var contactPosition by remember { mutableStateOf("") }
    var contactMobile by remember { mutableStateOf("") }
    var contactSecondaryPhone by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("add_customer_modal"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Register Commercial Client", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Captured by: $repName", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
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
                    Text(text = "BUSINESS DETAILS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name *") },
                        placeholder = { Text("e.g. Apex Supermarkets Ltd") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_business_name"),
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer / Principal Name *") },
                        placeholder = { Text("e.g. Peter Kamau") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_customer_name"),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fullLocation,
                        onValueChange = { fullLocation = it },
                        label = { Text("Full Location * (e.g. General Motors Mombasa Road)") },
                        placeholder = { Text("e.g. Plot 42, Opposite General Motors, Mombasa Road") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_full_location"),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = businessEmail,
                        onValueChange = { businessEmail = it },
                        label = { Text("Business Email") },
                        placeholder = { Text("e.g. info@apexsupermarkets.co.ke") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_business_email"),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(text = "PRIMARY CONTACT PERSON", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = contactPersonName,
                        onValueChange = { contactPersonName = it },
                        label = { Text("Contact Person Full Name *") },
                        placeholder = { Text("e.g. Beatrice Mwende") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_person_name"),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactPosition,
                        onValueChange = { contactPosition = it },
                        label = { Text("Position / Role *") },
                        placeholder = { Text("e.g. Procurement Manager / Store Lead") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_position"),
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactMobile,
                        onValueChange = { contactMobile = it },
                        label = { Text("Contact Mobile Number *") },
                        placeholder = { Text("e.g. +254 722 345 678") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_mobile"),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = contactSecondaryPhone,
                        onValueChange = { contactSecondaryPhone = it },
                        label = { Text("Second Contact Number (Optional)") },
                        placeholder = { Text("e.g. +254 733 901 234") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_secondary_phone"),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = companyEmail,
                        onValueChange = { companyEmail = it },
                        label = { Text("Corporate Email (Company Email) *") },
                        placeholder = { Text("e.g. bmwende@apexsupermarkets.co.ke") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_email"),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    errorMessage?.let { err ->
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (businessName.isBlank() || customerName.isBlank() || fullLocation.isBlank() ||
                                contactPersonName.isBlank() || contactMobile.isBlank()
                            ) {
                                errorMessage = "Please fill in all required fields marked with *"
                                return@Button
                            }
                            onSave(
                                customerName,
                                businessName,
                                fullLocation,
                                businessEmail,
                                contactPersonName,
                                contactPosition,
                                contactMobile,
                                contactSecondaryPhone,
                                if (companyEmail.isBlank()) businessEmail else companyEmail
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_customer"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save & Enqueue")
                    }
                }
            }
        }
    }
}
