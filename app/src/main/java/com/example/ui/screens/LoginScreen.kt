package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.data.repository.OrderBookingRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: OrderBookingRepository,
    onLoginSuccess: (role: UserRole, email: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var emailInput by remember { mutableStateOf("admin@zayed.com") }
    var passwordInput by remember { mutableStateOf("password") }
    var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var showForcePasswordResetDialog by remember { mutableStateOf(false) }
    var loggedInStaffId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Security",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Zayed SalesForce Portal",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Enterprise Account Authentication",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selection
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            UserRole.values().forEachIndexed { index, role ->
                SegmentedButton(
                    selected = selectedRole == role,
                    onClick = {
                        selectedRole = role
                        emailInput = when (role) {
                            UserRole.ADMIN -> "admin@zayed.com"
                            UserRole.SALES_REP -> "ahmad@zayed.com"
                            UserRole.CUSTOMER -> "fatima@client.com"
                            UserRole.SUPPLIER -> "supplier@zayed.com"
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = UserRole.values().size)
                ) {
                    Text(text = role.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password") },
            leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        successMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val staff = repository.getStaffByEmail(emailInput)
                    if (staff == null) {
                        errorMessage = "User email not found in database."
                        return@launch
                    }

                    if (staff.isTemporaryPassword && staff.isTempPasswordExpired) {
                        errorMessage = "24-Hour Temporary Recovery Password has expired! Generate a new one."
                        return@launch
                    }

                    if (staff.passwordHash == passwordInput) {
                        if (staff.mustChangePassword || staff.isTemporaryPassword) {
                            loggedInStaffId = staff.id
                            showForcePasswordResetDialog = true
                        } else {
                            onLoginSuccess(staff.role, staff.email)
                        }
                    } else {
                        errorMessage = "Invalid password. Try 'password' or generate temporary pass."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_submit_button")
        ) {
            Text("Sign In to Zayed Portal")
        }

        TextButton(
            onClick = { showForgotDialog = true },
            modifier = Modifier.testTag("forgot_password_button")
        ) {
            Text("Forgot Password? (24-Hr Temp Recovery)")
        }
    }

    // Forgot Password Temp Recovery Dialog
    if (showForgotDialog) {
        var recoveryEmail by remember { mutableStateOf(emailInput) }
        var generatedTempCode by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("24-Hour Temp Password Recovery") },
            text = {
                Column {
                    Text("Enter email address to generate a 24-hour expiring temporary recovery key:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = { Text("Account Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    generatedTempCode?.let { tempPass ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Temporary Password (Expires in 24 hrs):", style = MaterialTheme.typography.labelSmall)
                                Text(text = tempPass, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Use this key to sign in. You will be forced to set a new password.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val tempPass = repository.generateTemporaryPassword(recoveryEmail)
                        if (tempPass != null) {
                            generatedTempCode = tempPass
                            successMessage = "Temporary 24-hr password generated: $tempPass"
                        } else {
                            errorMessage = "Email not found"
                        }
                    }
                }) {
                    Text("Generate Temp Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Force First-Time / Recovery Password Reset Dialog
    if (showForcePasswordResetDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var resetError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { },
            title = { Text("First-Time Login: Set New Password") },
            text = {
                Column {
                    Text("You signed in using a temporary 24-hour key. For security, please establish a new permanent password:")
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Permanent Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm Permanent Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    resetError?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPass.length < 4) {
                        resetError = "Password must be at least 4 characters."
                        return@Button
                    }
                    if (newPass != confirmPass) {
                        resetError = "Passwords do not match."
                        return@Button
                    }

                    loggedInStaffId?.let { staffId ->
                        coroutineScope.launch {
                            repository.updatePassword(staffId, newPass)
                            showForcePasswordResetDialog = false
                            onLoginSuccess(selectedRole, emailInput)
                        }
                    }
                }) {
                    Text("Save & Continue")
                }
            }
        )
    }
}
