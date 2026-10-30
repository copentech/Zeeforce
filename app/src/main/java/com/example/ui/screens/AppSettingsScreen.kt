package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.data.repository.SalesForceRepository
import com.example.util.BiometricAuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    repository: SalesForceRepository,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val biometricStatus = remember(context) {
        BiometricAuthManager.checkBiometricStatus(context)
    }

    var isBiometricEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Settings & Preferences", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Toggle Card
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

            // Biometric Authentication Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometrics",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(text = "Biometric Admin Authorization", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Fingerprint / Face Unlock for admin settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                isBiometricEnabled = enabled
                                coroutineScope.launch {
                                    repository.updateStaffBiometric("admin_1", enabled)
                                }
                            },
                            enabled = biometricStatus == BiometricAuthManager.BiometricStatus.AVAILABLE,
                            modifier = Modifier.testTag("biometric_auth_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusLabel = when (biometricStatus) {
                        BiometricAuthManager.BiometricStatus.AVAILABLE -> "Hardware Status: Ready (Fingerprint & Face Unlock Supported)"
                        BiometricAuthManager.BiometricStatus.NO_HARDWARE -> "Hardware Status: No Biometric Sensor Found"
                        BiometricAuthManager.BiometricStatus.NOT_ENROLLED -> "Hardware Status: No Fingerprint/Face Enrolled in System Settings"
                        BiometricAuthManager.BiometricStatus.UNAVAILABLE -> "Hardware Status: Unavailable"
                    }

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (biometricStatus == BiometricAuthManager.BiometricStatus.AVAILABLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    if (biometricStatus == BiometricAuthManager.BiometricStatus.AVAILABLE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val activity = context as? FragmentActivity
                                activity?.let {
                                    BiometricAuthManager.promptBiometric(
                                        activity = it,
                                        title = "Test Biometric Sensor",
                                        subtitle = "Verify fingerprint or face unlock hardware response",
                                        onSuccess = {},
                                        onError = {},
                                        onFallbackToPin = {}
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_biometric_button")
                        ) {
                            Text("Test Biometric Sensor Prompt")
                        }
                    }
                }
            }

            // Company Profile Box
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Company Profile Information", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Organization: Zayed SalesForce Corp", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "System Version: v1.0.0 Enterprise", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
