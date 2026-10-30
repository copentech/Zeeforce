package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.OrderWithItems
import com.example.data.model.StaffMember
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Admin Commission Approval & Payment Code Dialog
 * Triggered when Admin marks an order as DELIVERED and PAID.
 * Supports BOTH Fixed Amount and Percentage (%) commission logic.
 */
@Composable
fun AdminCommissionApprovalDialog(
    orderWithItems: OrderWithItems,
    staffRep: StaffMember?,
    onDismiss: () -> Unit,
    onApproveDeliveryAndCommission: (paymentCode: String, commissionAmount: Double) -> Unit
) {
    val order = orderWithItems.order
    val orderSubtotal = order.subtotal

    // Determine default commission type and rate from staff or order
    val defaultType = staffRep?.commissionType ?: order.commissionType
    var commissionType by remember { mutableStateOf(defaultType) }
    var percentageRateText by remember {
        val ratePercent = (staffRep?.commissionRate ?: order.commissionRate) * 100
        mutableStateOf(String.format(Locale.US, "%.1f", ratePercent))
    }
    var fixedAmountText by remember {
        val fixedVal = staffRep?.fixedCommissionAmount ?: 1500.0
        mutableStateOf(fixedVal.toInt().toString())
    }
    var paymentCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val calculatedCommission = remember(commissionType, percentageRateText, fixedAmountText) {
        if (commissionType.equals("FIXED", ignoreCase = true)) {
            fixedAmountText.toDoubleOrNull() ?: 0.0
        } else {
            val pct = (percentageRateText.toDoubleOrNull() ?: 0.0) / 100.0
            orderSubtotal * pct
        }
    }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)
    val deepNavy = Color(0xFF0F172A)
    val borderNavy = Color(0xFF334155)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardNavy,
            border = BorderStroke(1.5.dp, imperialGold),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .testTag("dialog_admin_commission_approval")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(deepNavy)
                                .border(1.dp, imperialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = imperialGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Approve Delivery & Commission",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Order: ${order.orderNumber}",
                                fontSize = 11.sp,
                                color = imperialGold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderNavy)

                // Order & Rep Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = deepNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(order.businessName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sales Representative:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(order.accountHolderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order Subtotal:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(currencyFormatter.format(orderSubtotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total (Inc. Tax & Freight):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(currencyFormatter.format(order.totalAmount), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Commission Model Selector (Fixed Amount vs Percentage %)
                Text(
                    text = "SELECT COMMISSION MODEL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = imperialGold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Percentage
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (commissionType == "PERCENTAGE") imperialGold.copy(alpha = 0.15f) else deepNavy,
                        border = BorderStroke(1.dp, if (commissionType == "PERCENTAGE") imperialGold else borderNavy),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { commissionType = "PERCENTAGE" }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = commissionType == "PERCENTAGE",
                                onClick = { commissionType = "PERCENTAGE" }
                            )
                            Column {
                                Text("Percentage %", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Based on order value", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }

                    // Option 2: Fixed Amount
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (commissionType == "FIXED") imperialGold.copy(alpha = 0.15f) else deepNavy,
                        border = BorderStroke(1.dp, if (commissionType == "FIXED") imperialGold else borderNavy),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { commissionType = "FIXED" }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = commissionType == "FIXED",
                                onClick = { commissionType = "FIXED" }
                            )
                            Column {
                                Text("Fixed Amount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Flat rate per order", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input based on selected model
                if (commissionType == "PERCENTAGE") {
                    OutlinedTextField(
                        value = percentageRateText,
                        onValueChange = { percentageRateText = it },
                        label = { Text("Commission Percentage (%)", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. 3.5", color = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_commission_rate")
                    )
                } else {
                    OutlinedTextField(
                        value = fixedAmountText,
                        onValueChange = { fixedAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Fixed Commission (KES)", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. 1500", color = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fixed_commission")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Commission Preview Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF064E3B),
                    border = BorderStroke(1.dp, Color(0xFF34D399)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculated Commission:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = currencyFormatter.format(calculatedCommission),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Commission Payment Code Input (M-Pesa / Bank Ref)
                OutlinedTextField(
                    value = paymentCode,
                    onValueChange = {
                        paymentCode = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Commission Payment / Transaction Code *", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("e.g. MPESA-QRT8921B or EFT-5509", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = imperialGold)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_commission_payment_code")
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (paymentCode.isBlank()) {
                                errorMessage = "Please enter or paste the payment transaction code"
                                return@Button
                            }
                            if (calculatedCommission <= 0.0) {
                                errorMessage = "Commission amount must be greater than zero"
                                return@Button
                            }
                            onApproveDeliveryAndCommission(paymentCode.trim(), calculatedCommission)
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("btn_confirm_delivery_and_commission"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = imperialGold,
                            contentColor = deepNavy
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approve & Pay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Branded Commission Slip Dialog & Share Sheet Exporter
 * Shows the official Al Yasir Bin Zayed branded commission payout slip,
 * including verification token and one-tap Android Sharesheet/Print export.
 */
@Composable
fun CommissionSlipDialog(
    orderWithItems: OrderWithItems,
    staffRep: StaffMember?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val order = orderWithItems.order
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val paidDateStr = remember(order.commissionPaidAt) {
        if (order.commissionPaidAt > 0L) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(order.commissionPaidAt))
        } else {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(order.createdAt))
        }
    }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)
    val deepNavy = Color(0xFF0F172A)

    val slipText = remember(order, staffRep) {
        """
        =======================================================
               ZAYED SALESFORCE - OFFICIAL COMMISSION SLIP
               System Owned by Al Yasir Bin Zayed
        =======================================================
        Verification Token: ${order.stampVerificationCode}
        Status: COMMISSION RECEIVED & APPROVED (DELIVERY COMPLETE)
        Payment Transaction Ref: ${order.commissionPaymentCode.ifEmpty { "TXN-APPROVED" }}
        Date Paid: $paidDateStr
        
        REPRESENTATIVE DETAILS:
        Name: ${order.accountHolderName}
        Contact: ${order.accountHolderContact}
        Email: ${order.accountHolderEmail}
        
        ORDER PARTICULARS:
        Order Number: ${order.orderNumber}
        Customer: ${order.businessName}
        Location: ${order.customerLocation}
        Order Type: ${order.orderType}
        Order Subtotal: ${currencyFormatter.format(order.subtotal)}
        Grand Total: ${currencyFormatter.format(order.totalAmount)}
        
        COMMISSION SUMMARY:
        Model: ${order.commissionType} (${if (order.commissionType == "FIXED") "Flat Rate" else "${String.format("%.1f", order.commissionRate * 100)}%"})
        COMMISSION AMOUNT: ${currencyFormatter.format(order.commissionAmount)}
        
        Approved by Executive Administration
        Al Yasir Bin Zayed Enterprise Route-to-Market
        =======================================================
        """.trimIndent()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(2.dp, imperialGold),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .testTag("dialog_commission_slip")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Golden Emblem
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(deepNavy)
                                .border(1.dp, imperialGold, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.zayed_logo),
                                contentDescription = "Zayed Logo"
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zayed SalesForce",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = deepNavy
                            )
                            Text(
                                text = "OWNED BY AL YASIR BIN ZAYED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFCBD5E1))

                // Title Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF064E3B),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "OFFICIAL COMMISSION PAYOUT SLIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payout Amount Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF34D399))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COMMISSION AMOUNT PAID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = currencyFormatter.format(order.commissionAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ref: ${order.commissionPaymentCode.ifEmpty { "TXN-APPROVED" }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Particulars Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sales Rep:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(order.accountHolderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Linked Order:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(order.orderNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Customer:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(order.businessName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order Value:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(currencyFormatter.format(order.totalAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Commission Model:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            if (order.commissionType == "FIXED") "Fixed Flat Rate" else "${String.format("%.1f", order.commissionRate * 100)}% of Subtotal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Approval Date:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(paidDateStr, fontSize = 11.sp, color = Color(0xFF475569))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Stamp Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, imperialGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OFFICIAL VERIFICATION STAMP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = order.stampVerificationCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepNavy
                        )
                        Text(
                            text = "Authenticated by Zayed SalesForce Enterprise System",
                            fontSize = 9.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Share & Print PDF
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                com.example.util.CommissionPdfPrinter.printCommissionSlip(context, orderWithItems)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_print_commission_pdf"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF047857),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print / Save PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "Zayed SalesForce Commission Slip - ${order.orderNumber}")
                                    putExtra(Intent.EXTRA_TEXT, slipText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Official Commission Slip"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share_commission_slip"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = deepNavy,
                                contentColor = imperialGold
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Slip", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}
