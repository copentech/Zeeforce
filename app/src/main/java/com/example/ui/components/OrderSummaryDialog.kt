package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.CompanyProfile
import com.example.data.model.OrderWithItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderSummaryDialog(
    orderWithItems: OrderWithItems,
    companyProfile: CompanyProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val order = orderWithItems.order
    val items = orderWithItems.items
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
        currency = java.util.Currency.getInstance("KES")
    }

    val orderDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(order.createdAt))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("order_summary_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Bar with Close button
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
                                text = "Official Order Confirmation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Zayed SalesForce • Client Copy (${order.orderNumber})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Printable Receipt Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Company Header Card with Logo Emblem
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Gold Emblem
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.zayed_logo),
                                    contentDescription = "Emblem"
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "ZAYED SALESFORCE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "OWNED BY AL YASIR BIN ZAYED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37)
                            )
                            Text(
                                text = "Logistics Hub: ${companyProfile.factoryLocation}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tel: ${companyProfile.companyPhone} • Email: ${companyProfile.companyEmail}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tax PIN: ${companyProfile.taxPin} • Status: ${order.status}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client & Sales Rep Section
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Customer info
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "BILLED TO / CUSTOMER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = order.businessName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = order.customerName,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = order.customerLocation,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Mobile: ${order.customerMobile}",
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sales Rep
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "CAPTURED BY (STAFF)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = order.accountHolderName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = order.accountHolderContact,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = order.accountHolderEmail,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Booking Date: $orderDate",
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Regional Logistics Settings Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Regional Setting: ${order.regionalTier}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Distance: ${order.distanceKm} km (${order.distanceBand})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Expected Delivery: ${order.expectedDeliveryDate} (${order.deliveryUrgency})",
                                    fontSize = 10.sp
                                )
                                if (order.scheduledDeliveryDate.isNotBlank()) {
                                    Text(
                                        text = "Admin Scheduled: ${order.scheduledDeliveryDate}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Itemized Order Table Header
                    Text(
                        text = "ORDER ITEMS & QUANTITIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(6.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Item", modifier = Modifier.weight(2.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Qty", modifier = Modifier.weight(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(text = "Unit Price", modifier = Modifier.weight(1.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text(text = "Total (KES)", modifier = Modifier.weight(1.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }

                        items.forEach { item ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2.2f)) {
                                    Text(text = item.productName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(text = item.unit, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(text = "${item.quantity}", modifier = Modifier.weight(0.7f), fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                Text(text = "%,.0f".format(item.unitPrice), modifier = Modifier.weight(1.1f), fontSize = 10.sp, textAlign = TextAlign.End)
                                Text(text = "%,.0f".format(item.lineTotal), modifier = Modifier.weight(1.2f), fontSize = 11.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Financial Calculations Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Items Subtotal (${order.totalQuantity} units):", fontSize = 11.sp)
                                Text(text = currencyFormatter.format(order.subtotal), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Regional Logistics Freight:", fontSize = 11.sp)
                                Text(text = currencyFormatter.format(order.regionalFreightSurcharge), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tax (${order.taxCategory}):", fontSize = 11.sp)
                                Text(text = currencyFormatter.format(order.taxAmount), fontSize = 11.sp)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "GRAND TOTAL:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    text = currencyFormatter.format(order.totalAmount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Payment Terms: ${order.paymentModel} • Order Type: ${order.orderType}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Centered Branded Official Verification Stamp
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BrandedStampBadge(
                            companyName = "ZAYED SALESFORCE",
                            verificationCode = order.stampVerificationCode,
                            dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(order.createdAt))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Bottom Action Buttons: Share Client Copy & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            shareOrderSummary(context, orderWithItems, companyProfile)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("share_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Client Copy")
                    }
                }
            }
        }
    }
}

private fun shareOrderSummary(
    context: Context,
    orderWithItems: OrderWithItems,
    companyProfile: CompanyProfile
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    val sb = StringBuilder()
    sb.appendLine("========================================")
    sb.appendLine("ZAYED SALESFORCE")
    sb.appendLine("OWNED BY AL YASIR BIN ZAYED")
    sb.appendLine("Logistics Hub: ${companyProfile.factoryLocation}")
    sb.appendLine("Contacts: ${companyProfile.companyPhone}")
    sb.appendLine("Tax PIN: ${companyProfile.taxPin}")
    sb.appendLine("========================================")
    sb.appendLine("OFFICIAL ORDER CONFIRMATION - CLIENT COPY")
    sb.appendLine("Order Number: ${order.orderNumber}")
    sb.appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(order.createdAt))}")
    sb.appendLine("Verification Ref: ${order.stampVerificationCode}")
    sb.appendLine("----------------------------------------")
    sb.appendLine("CUSTOMER:")
    sb.appendLine("Business: ${order.businessName}")
    sb.appendLine("Contact Person: ${order.customerName}")
    sb.appendLine("Location: ${order.customerLocation}")
    sb.appendLine("Phone: ${order.customerMobile}")
    sb.appendLine("Captured By: ${order.accountHolderName}")
    sb.appendLine("----------------------------------------")
    sb.appendLine("LOGISTICS & REGIONAL SETTINGS:")
    sb.appendLine("Regional Tier: ${order.regionalTier}")
    sb.appendLine("Distance: ${order.distanceKm} km (${order.distanceBand})")
    sb.appendLine("Expected Delivery Date: ${order.expectedDeliveryDate} (${order.deliveryUrgency})")
    if (order.scheduledDeliveryDate.isNotBlank()) {
        sb.appendLine("Scheduled Delivery: ${order.scheduledDeliveryDate}")
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine("ITEMS:")
    items.forEachIndexed { index, item ->
        sb.appendLine("${index + 1}. ${item.productName} (${item.unit})")
        sb.appendLine("   Qty: ${item.quantity} x KES ${"%,.0f".format(item.unitPrice)} = KES ${"%,.0f".format(item.lineTotal)}")
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine("Subtotal: KES ${"%,.2f".format(order.subtotal)}")
    sb.appendLine("Freight Surcharge: KES ${"%,.2f".format(order.regionalFreightSurcharge)}")
    sb.appendLine("Tax (${order.taxCategory}): KES ${"%,.2f".format(order.taxAmount)}")
    sb.appendLine("GRAND TOTAL: KES ${"%,.2f".format(order.totalAmount)}")
    sb.appendLine("Payment Terms: ${order.paymentModel}")
    sb.appendLine("Order Type: ${order.orderType}")
    sb.appendLine("========================================")
    sb.appendLine("[OFFICIAL STAMP: VERIFIED & BOOKED - ZAYED SALESFORCE]")
    sb.appendLine("Status: ${order.status}")
    sb.appendLine("System owned and operated by Al Yasir Bin Zayed")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_SUBJECT, "Zayed SalesForce Order Confirmation - ${order.orderNumber} (${order.businessName})")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Order Confirmation Client Copy")
    context.startActivity(shareIntent)
}
