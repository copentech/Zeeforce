package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StaffMember
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

@Composable
fun StaffManagementDialog(
    staffList: List<StaffMember>,
    onSaveStaff: (StaffMember) -> Unit,
    onDeleteStaff: (String) -> Unit,
    onIssue24hTempPassword: (staffId: String, onIssued: (String) -> Unit) -> Unit,
    onVerifySecondaryPin: ((String) -> Boolean)? = null,
    requirePinForSensitiveActions: Boolean = true,
    onOpenPermissionManager: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editingStaff by remember { mutableStateOf<StaffMember?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var issuedTempPassCode by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(StaffName, TempCode)
    var pendingSensitiveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var sensitiveActionTitle by remember { mutableStateOf("Authorize Staff Action") }
    var sensitiveActionDesc by remember { mutableStateOf("Enter 4-digit secondary security PIN to confirm.") }

    val pendingResetRequests = remember(staffList) {
        staffList.filter { it.passwordResetRequested }
    }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)
    val deepNavy = Color(0xFF0F172A)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .testTag("staff_management_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = cardNavy,
            border = BorderStroke(1.5.dp, imperialGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(deepNavy)
                                .border(1.dp, imperialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = imperialGold, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Staff Administration & Security", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            Text(text = "Credentials, PINs, 24h temp passwords & commission", fontSize = 10.sp, color = imperialGold)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF334155))

                // Alert when a temporary password was just generated
                issuedTempPassCode?.let { (name, code) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.dp, Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("24-HOUR TEMPORARY PASSWORD ISSUED FOR $name:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                                Text(code, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, color = Color.White)
                                Text("Valid for 24 hours. User will be forced to set new password on first login.", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Temporary Password", code))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                            }
                        }
                    }
                }

                // Password Reset Requests Banner
                if (pendingResetRequests.isNotEmpty() && !isCreatingNew && editingStaff == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF78350F).copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, imperialGold)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = imperialGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PASSWORD RESET REQUESTS (${pendingResetRequests.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = imperialGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            pendingResetRequests.forEach { reqStaff ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${reqStaff.name} (${reqStaff.email})", fontSize = 11.sp, color = Color.White)
                                    Button(
                                        onClick = {
                                            if (requirePinForSensitiveActions && onVerifySecondaryPin != null) {
                                                sensitiveActionTitle = "Issue Temporary Credentials"
                                                sensitiveActionDesc = "Authorizing a 24-hour temporary login password for ${reqStaff.name}."
                                                pendingSensitiveAction = {
                                                    onIssue24hTempPassword(reqStaff.id) { code ->
                                                        issuedTempPassCode = Pair(reqStaff.name, code)
                                                    }
                                                }
                                            } else {
                                                onIssue24hTempPassword(reqStaff.id) { code ->
                                                    issuedTempPassCode = Pair(reqStaff.name, code)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = deepNavy),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Send 24h Pass", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isCreatingNew || editingStaff != null) {
                    val staff = editingStaff ?: StaffMember(
                        id = UUID.randomUUID().toString(),
                        name = "",
                        email = "",
                        phone = "",
                        customRoleLabel = "Field Sales Representative"
                    )

                    EditStaffForm(
                        initialStaff = staff,
                        isNew = isCreatingNew,
                        onCancel = {
                            isCreatingNew = false
                            editingStaff = null
                        },
                        onSave = { updated ->
                            val isSensitiveChange = updated.isAdmin != staff.isAdmin ||
                                updated.accountStatus != staff.accountStatus ||
                                updated.canManageInventory != staff.canManageInventory
                            if (requirePinForSensitiveActions && onVerifySecondaryPin != null && isSensitiveChange) {
                                sensitiveActionTitle = "Authorize Privilege Change"
                                sensitiveActionDesc = "Modifying administrative privileges or account status for ${updated.name}."
                                pendingSensitiveAction = {
                                    onSaveStaff(updated)
                                    isCreatingNew = false
                                    editingStaff = null
                                }
                            } else {
                                onSaveStaff(updated)
                                isCreatingNew = false
                                editingStaff = null
                            }
                        },
                        onIssueTempPassword = {
                            if (requirePinForSensitiveActions && onVerifySecondaryPin != null) {
                                sensitiveActionTitle = "Issue Temporary Credentials"
                                sensitiveActionDesc = "Authorizing temporary 24-hour login password for ${staff.name}."
                                pendingSensitiveAction = {
                                    onIssue24hTempPassword(staff.id) { code ->
                                        issuedTempPassCode = Pair(staff.name, code)
                                    }
                                }
                            } else {
                                onIssue24hTempPassword(staff.id) { code ->
                                    issuedTempPassCode = Pair(staff.name, code)
                                }
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Staff Accounts (${staffList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            onOpenPermissionManager?.let { openPerms ->
                                OutlinedButton(
                                    onClick = openPerms,
                                    modifier = Modifier.testTag("btn_open_perms_from_roster"),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = imperialGold),
                                    border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.6f))
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Permissions", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = { isCreatingNew = true },
                                modifier = Modifier.testTag("btn_create_staff_member"),
                                colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = deepNavy),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(staffList, key = { it.id }) { staff ->
                            val statusColor = when (staff.accountStatus.uppercase()) {
                                "SUSPENDED" -> Color(0xFFF59E0B)
                                "TERMINATED" -> Color(0xFFEF4444)
                                else -> Color(0xFF10B981)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                                if (staff.isAdmin) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "ADMIN",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = imperialGold,
                                                        modifier = Modifier
                                                            .border(1.dp, imperialGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = statusColor.copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, statusColor)
                                                ) {
                                                    Text(
                                                        text = staff.accountStatus.uppercase(),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Role: ${staff.customRoleLabel}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = imperialGold
                                            )
                                            Text(
                                                text = "${staff.email} • ${staff.phone}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "Commission: ${if (staff.commissionType == "FIXED") "KES ${staff.fixedCommissionAmount.toInt()} Flat" else "${String.format("%.1f", staff.commissionRate * 100)}%"}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }

                                        Row {
                                            IconButton(onClick = { editingStaff = staff }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Role", modifier = Modifier.size(18.dp), tint = imperialGold)
                                            }
                                            if (!staff.name.contains("Al Yasir Bin Zayed")) {
                                                IconButton(
                                                    onClick = {
                                                        if (requirePinForSensitiveActions && onVerifySecondaryPin != null) {
                                                            sensitiveActionTitle = "Delete Staff Member"
                                                            sensitiveActionDesc = "Confirm deletion of ${staff.name}. This sensitive action requires 4-digit security PIN authorization."
                                                            pendingSensitiveAction = { onDeleteStaff(staff.id) }
                                                        } else {
                                                            onDeleteStaff(staff.id)
                                                        }
                                                    },
                                                    modifier = Modifier.testTag("btn_delete_staff_${staff.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = Color(0xFFEF4444))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }

    // Secondary PIN Verification Dialog for sensitive staff actions
    pendingSensitiveAction?.let { action ->
        SecondaryStaffPinDialog(
            title = sensitiveActionTitle,
            description = sensitiveActionDesc,
            onVerifyPin = onVerifySecondaryPin ?: { true },
            onAuthorized = {
                action()
                pendingSensitiveAction = null
            },
            onDismiss = {
                pendingSensitiveAction = null
            }
        )
    }
}

@Composable
fun EditStaffForm(
    initialStaff: StaffMember,
    isNew: Boolean,
    onCancel: () -> Unit,
    onSave: (StaffMember) -> Unit,
    onIssueTempPassword: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialStaff.name) }
    var email by remember { mutableStateOf(initialStaff.email) }
    var phone by remember { mutableStateOf(initialStaff.phone) }
    var customRoleLabel by remember { mutableStateOf(initialStaff.customRoleLabel) }
    var password by remember { mutableStateOf(initialStaff.password) }
    var adminPin by remember { mutableStateOf(initialStaff.adminPin) }
    var accountStatus by remember { mutableStateOf(initialStaff.accountStatus) }
    var commissionType by remember { mutableStateOf(initialStaff.commissionType) }
    var commissionRateText by remember { mutableStateOf(String.format(Locale.US, "%.1f", initialStaff.commissionRate * 100)) }
    var fixedCommissionText by remember { mutableStateOf(initialStaff.fixedCommissionAmount.toInt().toString()) }

    var canAccessOrders by remember { mutableStateOf(initialStaff.canAccessOrders) }
    var canAccessInventory by remember { mutableStateOf(initialStaff.canAccessInventory) }
    var canAccessCustomers by remember { mutableStateOf(initialStaff.canAccessCustomers) }
    var canBookOrders by remember { mutableStateOf(initialStaff.canBookOrders) }
    var canManageInventory by remember { mutableStateOf(initialStaff.canManageInventory) }
    var canScheduleDelivery by remember { mutableStateOf(initialStaff.canScheduleDelivery) }
    var isAdmin by remember { mutableStateOf(initialStaff.isAdmin) }
    var error by remember { mutableStateOf<String?>(null) }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val borderNavy = Color(0xFF334155)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isNew) "Add New Staff Member" else "Edit Staff & Security Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = imperialGold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Staff Full Name *", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth().testTag("input_staff_name"),
            singleLine = true
        )

        OutlinedTextField(
            value = customRoleLabel,
            onValueChange = { customRoleLabel = it },
            label = { Text("Custom Role Title *", color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth().testTag("input_staff_role"),
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Contact", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Corporate Email", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        // Account Status Selector (Active, Suspended, Terminated)
        Text(text = "ACCOUNT STATUS (LIFECYCLE)", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = imperialGold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ACTIVE", "SUSPENDED", "TERMINATED").forEach { status ->
                val isSel = accountStatus.equals(status, ignoreCase = true)
                val statusColor = when (status) {
                    "SUSPENDED" -> Color(0xFFF59E0B)
                    "TERMINATED" -> Color(0xFFEF4444)
                    else -> Color(0xFF10B981)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) statusColor.copy(alpha = 0.25f) else deepNavy,
                    border = BorderStroke(1.dp, if (isSel) statusColor else borderNavy),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { accountStatus = status }
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) statusColor else Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Security Credentials Section
        Text(text = "LOGIN CREDENTIALS & SECURITY", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = imperialGold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Login Password", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = adminPin,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) adminPin = it },
                label = { Text("2nd Layer PIN (4-8)", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // 24h Temporary Password Action Button
        if (!isNew && onIssueTempPassword != null) {
            OutlinedButton(
                onClick = onIssueTempPassword,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = imperialGold),
                border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate & Issue 24-Hour Temporary Password", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Commission Configuration Section (Fixed vs %)
        Text(text = "COMMISSION CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = imperialGold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (commissionType == "PERCENTAGE") imperialGold.copy(alpha = 0.2f) else deepNavy,
                border = BorderStroke(1.dp, if (commissionType == "PERCENTAGE") imperialGold else borderNavy),
                modifier = Modifier.weight(1f).clickable { commissionType = "PERCENTAGE" }
            ) {
                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = commissionType == "PERCENTAGE", onClick = { commissionType = "PERCENTAGE" })
                    Text("Percentage (%)", fontSize = 11.sp, color = Color.White)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (commissionType == "FIXED") imperialGold.copy(alpha = 0.2f) else deepNavy,
                border = BorderStroke(1.dp, if (commissionType == "FIXED") imperialGold else borderNavy),
                modifier = Modifier.weight(1f).clickable { commissionType = "FIXED" }
            ) {
                Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = commissionType == "FIXED", onClick = { commissionType = "FIXED" })
                    Text("Fixed Flat Rate", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        if (commissionType == "PERCENTAGE") {
            OutlinedTextField(
                value = commissionRateText,
                onValueChange = { commissionRateText = it },
                label = { Text("Commission Rate (%)", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = fixedCommissionText,
                onValueChange = { fixedCommissionText = it.filter { c -> c.isDigit() } },
                label = { Text("Fixed Amount per Delivered Order (KES)", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "MODULE ACCESS & PRIVILEGES", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = imperialGold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = deepNavy)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Executive Admin Privileges", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Switch(checked = isAdmin, onCheckedChange = { isAdmin = it })
                }

                HorizontalDivider(color = borderNavy)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Inventory Module Access", fontSize = 12.sp, color = Color.White)
                    Switch(checked = canAccessInventory, onCheckedChange = { canAccessInventory = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Orders Module Access", fontSize = 12.sp, color = Color.White)
                    Switch(checked = canAccessOrders, onCheckedChange = { canAccessOrders = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Customers Module Access", fontSize = 12.sp, color = Color.White)
                    Switch(checked = canAccessCustomers, onCheckedChange = { canAccessCustomers = it })
                }
            }
        }

        error?.let {
            Text(text = it, color = Color(0xFFEF4444), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel", color = Color.White)
            }

            Button(
                onClick = {
                    if (name.isBlank() || customRoleLabel.isBlank()) {
                        error = "Please fill in staff name and custom role title"
                        return@Button
                    }
                    val rate = (commissionRateText.toDoubleOrNull() ?: 3.5) / 100.0
                    val fixed = fixedCommissionText.toDoubleOrNull() ?: 1500.0
                    onSave(
                        initialStaff.copy(
                            name = name.trim(),
                            customRoleLabel = customRoleLabel.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            password = password.trim(),
                            adminPin = adminPin.trim().ifEmpty { "2026" },
                            accountStatus = accountStatus,
                            commissionType = commissionType,
                            commissionRate = rate,
                            fixedCommissionAmount = fixed,
                            isAdmin = isAdmin,
                            canAccessInventory = canAccessInventory,
                            canAccessOrders = canAccessOrders,
                            canAccessCustomers = canAccessCustomers,
                            canBookOrders = canBookOrders,
                            canManageInventory = canManageInventory,
                            canScheduleDelivery = canScheduleDelivery
                        )
                    )
                },
                modifier = Modifier.weight(1.5f).testTag("btn_save_staff_form"),
                colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = deepNavy)
            ) {
                Text("Save Credentials & Role", fontWeight = FontWeight.Bold)
            }
        }
    }
}
