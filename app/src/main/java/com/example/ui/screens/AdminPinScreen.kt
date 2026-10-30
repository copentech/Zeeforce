package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StaffMember

/**
 * Dedicated Full-Screen Second Security Layer (2FA PIN Entry Screen)
 * for Administrator accounts in Zayed SalesForce.
 *
 * Triggers after successful password verification for any account with isAdmin = true.
 */
@Composable
fun AdminPinScreen(
    adminStaff: StaffMember,
    onVerifyPin: (pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToLogin() }

    var pinInput by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableStateOf(0) }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val borderNavy = Color(0xFF334155)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deepNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.testTag("pin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = imperialGold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardNavy,
                    border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = imperialGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LAYER 2: 4-8 DIGIT PIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = imperialGold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Luxury Emblem Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(cardNavy)
                    .border(2.dp, imperialGold, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zayed_logo),
                    contentDescription = "Al Yasir Bin Zayed Crest",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Zayed SalesForce",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "OWNED BY AL YASIR BIN ZAYED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = imperialGold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Identity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_staff_identity_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(deepNavy)
                            .border(1.dp, imperialGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = imperialGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = adminStaff.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = imperialGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = imperialGold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = adminStaff.customRoleLabel,
                            fontSize = 11.sp,
                            color = imperialGold,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${adminStaff.email} • Password Verified ✓",
                            fontSize = 10.sp,
                            color = Color(0xFF34D399) // Emerald success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PIN Entry Display & Indicators
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_display_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                border = BorderStroke(1.5.dp, if (errorMessage != null) Color(0xFFEF4444) else imperialGold.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter 4 to 8 Digit Security PIN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic PIN Dots / Digits Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        val maxSlots = 8
                        for (i in 0 until maxSlots) {
                            val isFilled = i < pinInput.length
                            val isCurrentFocus = i == pinInput.length
                            val digitChar = if (isFilled) pinInput[i] else null

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isFilled -> imperialGold
                                            isCurrentFocus -> deepNavy
                                            else -> deepNavy.copy(alpha = 0.6f)
                                        }
                                    )
                                    .border(
                                        width = if (isCurrentFocus) 1.5.dp else 1.dp,
                                        color = when {
                                            isFilled -> imperialGold
                                            isCurrentFocus -> imperialGold
                                            else -> borderNavy
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("pin_indicator_$i"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFilled) {
                                    if (isPinVisible) {
                                        Text(
                                            text = digitChar.toString(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = deepNavy
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(deepNavy)
                                        )
                                    }
                                } else if (i < 4) {
                                    // Required indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(borderNavy)
                                    )
                                }
                            }
                        }
                    }

                    // Visibility Toggle and Counter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pinInput.length} of 4-8 digits entered",
                            fontSize = 11.sp,
                            color = if (pinInput.length >= 4) imperialGold else Color(0xFF94A3B8)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isPinVisible = !isPinVisible }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PIN preview",
                                tint = imperialGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPinVisible) "Hide" else "Show",
                                fontSize = 11.sp,
                                color = imperialGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = err,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Numeric Tactile Keypad (3x4 Grid)
            NumericKeypad(
                onDigitClick = { digit ->
                    if (pinInput.length < 8) {
                        pinInput += digit
                        errorMessage = null
                    }
                },
                onBackspace = {
                    if (pinInput.isNotEmpty()) {
                        pinInput = pinInput.dropLast(1)
                        errorMessage = null
                    }
                },
                onClear = {
                    pinInput = ""
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (pinInput.length < 4) {
                        errorMessage = "Admin PIN must be between 4 and 8 digits"
                        return@Button
                    }
                    isVerifying = true
                    errorMessage = null
                    onVerifyPin(
                        pinInput,
                        {
                            isVerifying = false
                        },
                        { err ->
                            isVerifying = false
                            attemptCount++
                            errorMessage = "$err (Attempt #$attemptCount)"
                        }
                    )
                },
                enabled = pinInput.length >= 4 && !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_verify_admin_pin"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = imperialGold,
                    contentColor = deepNavy,
                    disabledContainerColor = cardNavy,
                    disabledContentColor = Color(0xFF64748B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = deepNavy, strokeWidth = 2.5.dp)
                } else {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Access Admin Console",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Demo Autofill & Return Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.testTag("btn_cancel_pin"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = BorderStroke(1.dp, borderNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Return to Login", fontSize = 12.sp)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cardNavy,
                    border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable {
                            pinInput = "2026"
                            errorMessage = null
                        }
                        .testTag("chip_autofill_pin")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Autofill PIN: 2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = imperialGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Security Explanation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardNavy.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = imperialGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURITY GOVERNANCE POLICY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = imperialGold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Administrator privileges allow permanent customer deletion, price tier modifications, delivery fleet dispatching, and staff credential management. Secondary PIN authentication guarantees authorized executive access.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Tactile, responsive numeric keypad with 48dp+ minimum touch targets
 * compliant with Android Material 3 accessibility guidelines.
 */
@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("CLEAR", "0", "BACK")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (key) {
                            "CLEAR" -> {
                                KeypadSpecialButton(
                                    label = "C",
                                    icon = Icons.Default.Clear,
                                    testTag = "pin_key_clear",
                                    onClick = onClear
                                )
                            }
                            "BACK" -> {
                                KeypadSpecialButton(
                                    label = "⌫",
                                    icon = Icons.AutoMirrored.Filled.Backspace,
                                    testTag = "pin_key_backspace",
                                    onClick = onBackspace
                                )
                            }
                            else -> {
                                KeypadDigitButton(
                                    digit = key,
                                    onClick = { onDigitClick(key) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0xFFD4AF37)),
                onClick = onClick
            )
            .testTag("pin_key_$digit"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun KeypadSpecialButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(0xFFD4AF37)),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.7f),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
