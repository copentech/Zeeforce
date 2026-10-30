package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricAuthManager

@Composable
fun SecondaryStaffPinDialog(
    title: String = "Secondary Authorization Required",
    description: String = "Please enter your 4-digit secondary PIN or scan biometric credentials to continue.",
    onVerifyPin: (String) -> Boolean = { it == "1234" },
    onDismiss: () -> Unit,
    onAuthorized: () -> Unit
) {
    val context = LocalContext.current
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Automatically attempt biometric prompt if available
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val status = BiometricAuthManager.checkBiometricStatus(context)
            if (status == BiometricAuthManager.BiometricStatus.AVAILABLE) {
                BiometricAuthManager.promptBiometric(
                    activity = activity,
                    title = "Administrative Access",
                    subtitle = "Scan fingerprint or face unlock to proceed",
                    onSuccess = {
                        onAuthorized()
                    },
                    onError = { err ->
                        errorMessage = err
                    },
                    onFallbackToPin = {
                        // User chose 4-digit PIN fallback
                    }
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            inputPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secondary_pin_input")
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val activity = context as? FragmentActivity
                if (activity != null && BiometricAuthManager.checkBiometricStatus(context) == BiometricAuthManager.BiometricStatus.AVAILABLE) {
                    OutlinedButton(
                        onClick = {
                            BiometricAuthManager.promptBiometric(
                                activity = activity,
                                onSuccess = { onAuthorized() },
                                onError = { errorMessage = it },
                                onFallbackToPin = {}
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_biometric_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Scan",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Scan Fingerprint / Face Unlock")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerifyPin(inputPin)) {
                        onAuthorized()
                    } else {
                        errorMessage = "Invalid 4-digit PIN."
                    }
                },
                modifier = Modifier.testTag("pin_confirm_button")
            ) {
                Text("Verify PIN")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("pin_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
