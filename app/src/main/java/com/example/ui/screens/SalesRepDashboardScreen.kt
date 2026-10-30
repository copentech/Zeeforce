package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderWithItems
import com.example.data.model.StaffMember
import com.example.ui.components.CommissionSlipDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Individual Sales Rep Performance & Commission Dashboard
 * Displays rep's daily/monthly booking volume, order pipeline, commission earnings,
 * and enables viewing/downloading official Al Yasir Bin Zayed commission slips.
 */
@Composable
fun SalesRepDashboardScreen(
    activeStaff: StaffMember,
    orders: List<OrderWithItems>,
    commissions: List<com.example.data.model.CommissionRecord> = emptyList(),
    onNewBookingClick: () -> Unit,
    onViewOrderSummary: (OrderWithItems) -> Unit,
    onSyncCommissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedOrderForSlip by remember { mutableStateOf<OrderWithItems?>(null) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    // Filter orders belonging to this representative
    val repOrders = remember(orders, activeStaff) {
        orders.filter {
            it.order.accountHolderEmail.equals(activeStaff.email, ignoreCase = true) ||
                    it.order.accountHolderName.contains(activeStaff.name, ignoreCase = true)
        }
    }

    val repCommissionsList = remember(commissions, activeStaff) {
        commissions.filter {
            it.repId == activeStaff.id || it.repEmail.equals(activeStaff.email, ignoreCase = true)
        }
    }

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }

    val todayOrders = repOrders.filter { it.order.createdAt >= todayStart }
    val todaySales = todayOrders.sumOf { it.order.totalAmount }
    val totalVolume = repOrders.sumOf { it.order.totalAmount }

    val deliveredOrders = repOrders.filter { it.order.status == "DELIVERED" }

    val pendingCommissionTotal = if (repCommissionsList.isNotEmpty()) {
        repCommissionsList.filter { !it.isPaidOut }.sumOf { it.calculatedAmount }
    } else {
        repOrders.filter { it.order.commissionStatus != "PAID" }.sumOf { it.order.commissionAmount }
    }

    val paidCommissionTotal = if (repCommissionsList.isNotEmpty()) {
        repCommissionsList.filter { it.isPaidOut }.sumOf { it.calculatedAmount }
    } else {
        repOrders.filter { it.order.commissionStatus == "PAID" }.sumOf { it.order.commissionAmount }
    }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val borderNavy = Color(0xFF334155)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Rep Profile & Executive Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${activeStaff.name}'s Dashboard",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Text(
                    text = "${activeStaff.customRoleLabel} • Zayed SalesForce",
                    fontSize = 11.sp,
                    color = imperialGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                val isFixed = activeStaff.commissionType.equals("FIXED", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cardNavy,
                    border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isFixed) "Model: Flat Fee (${currencyFormatter.format(activeStaff.fixedCommissionAmount)}/order)" else "Model: Rate (${String.format(java.util.Locale.US, "%.1f", if (activeStaff.commissionRate > 1.0) activeStaff.commissionRate else activeStaff.commissionRate * 100.0)}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Button(
                onClick = onNewBookingClick,
                colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = deepNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("rep_btn_new_booking")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Commission Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Commission Paid / Received Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_rep_paid_commission"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF34D399))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COMMISSION RECEIVED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                    Text(
                        text = currencyFormatter.format(paidCommissionTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${deliveredOrders.count { it.order.commissionStatus == "PAID" }} payouts cleared",
                        fontSize = 10.sp,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            // Commission Pending Payout Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_rep_pending_commission"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = imperialGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PENDING DELIVERY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                    }
                    Text(
                        text = currencyFormatter.format(pendingCommissionTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Awaiting delivery confirmation",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Performance KPI Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardNavy),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, borderNavy)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Volume", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(currencyFormatter.format(todaySales), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${todayOrders.size} orders booked", fontSize = 10.sp, color = Color(0xFF38BDF8))
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(borderNavy))

                Column {
                    Text("Total Pipeline", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(currencyFormatter.format(totalVolume), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                    Text("${repOrders.size} total bookings", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(borderNavy))

                Column {
                    Text("Commission Rule", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(
                        if (activeStaff.commissionType == "FIXED") "Fixed Flat" else "${String.format("%.1f", activeStaff.commissionRate * 100)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                    Text(
                        if (activeStaff.commissionType == "FIXED") "${currencyFormatter.format(activeStaff.fixedCommissionAmount)}/order" else "On order subtotal",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: My Booked Orders & Commission Status
        Text(
            text = "MY ORDERS & COMMISSION PAYOUT STATUS (${repOrders.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = imperialGold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (repOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No orders booked yet", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Book your first order using the New Order button above", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(repOrders, key = { it.order.id }) { orderWithItems ->
                    val order = orderWithItems.order
                    val isPaid = order.commissionStatus == "PAID"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rep_order_card_${order.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = cardNavy),
                        border = BorderStroke(1.dp, if (isPaid) Color(0xFF059669) else borderNavy)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text(order.businessName, fontSize = 11.sp, color = imperialGold)
                                }

                                // Status Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isPaid -> Color(0xFF064E3B)
                                        order.status == "DELIVERED" -> Color(0xFF1E3A8A)
                                        order.status == "SCHEDULED" -> Color(0xFF78350F)
                                        else -> Color(0xFF334155)
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            isPaid -> "COMMISSION PAID"
                                            order.status == "DELIVERED" -> "DELIVERED • PENDING APPROVAL"
                                            order.status == "SCHEDULED" -> "SCHEDULED"
                                            else -> "BOOKED"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderNavy)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order Value: ${currencyFormatter.format(order.totalAmount)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = "Commission: ${currencyFormatter.format(order.commissionAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaid) Color(0xFF34D399) else imperialGold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { onViewOrderSummary(orderWithItems) },
                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Invoice", fontSize = 10.sp)
                                    }

                                    if (isPaid) {
                                        Button(
                                            onClick = { selectedOrderForSlip = orderWithItems },
                                            colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = deepNavy),
                                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_view_commission_slip_${order.id}")
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Slip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Commission Slip Modal
        selectedOrderForSlip?.let { slipOrder ->
            CommissionSlipDialog(
                orderWithItems = slipOrder,
                staffRep = activeStaff,
                onDismiss = { selectedOrderForSlip = null }
            )
        }
    }
}
