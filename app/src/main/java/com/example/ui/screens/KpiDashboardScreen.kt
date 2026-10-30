package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommissionRecord
import com.example.data.model.OrderWithItems
import com.example.data.model.StaffMember
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/**
 * Executive KPI Dashboard Screen for Zayed SalesForce (Al Yasir Bin Zayed).
 * Displays high-level business metrics including total monthly revenue,
 * active sales representatives, total volume of orders for the current period,
 * and regional tier performance breakdown.
 */
@Composable
fun KpiDashboardScreen(
    orders: List<OrderWithItems>,
    staffList: List<StaffMember>,
    commissions: List<CommissionRecord>,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val monthlyOrders = remember(orders) {
        orders.filter { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = item.order.createdAt }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }

    val totalMonthlyRevenue = monthlyOrders.sumOf { it.order.totalAmount }
    val totalMonthlyUnits = monthlyOrders.sumOf { it.order.totalQuantity }
    val activeRepsCount = staffList.count { it.isActive && !it.isAdmin }
    val totalDeliveredCount = orders.count { it.order.status == "DELIVERED" }

    val totalCommissionPaid = commissions.filter { it.isPaidOut }.sumOf { it.calculatedAmount }
    val totalCommissionPending = commissions.filter { !it.isPaidOut }.sumOf { it.calculatedAmount }

    val tier1Revenue = orders.filter { it.order.regionalTier.contains("Tier 1") }.sumOf { it.order.totalAmount }
    val tier2Revenue = orders.filter { it.order.regionalTier.contains("Tier 2") }.sumOf { it.order.totalAmount }
    val tier3Revenue = orders.filter { it.order.regionalTier.contains("Tier 3") }.sumOf { it.order.totalAmount }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val borderNavy = Color(0xFF334155)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(deepNavy)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Executive KPI Dashboard",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "Al Yasir Bin Zayed • Route-to-Market Performance Analytics",
                    fontSize = 11.sp,
                    color = imperialGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardNavy,
                border = BorderStroke(1.dp, imperialGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = imperialGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Live Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 1: Total Monthly Revenue & Total Monthly Volume
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_monthly_revenue"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, imperialGold)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = imperialGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "MONTHLY REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currencyFormatter.format(totalMonthlyRevenue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${monthlyOrders.size} orders booked this month",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_monthly_volume"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PERIOD VOLUME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalMonthlyUnits Units",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalDeliveredCount orders delivered",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Active Sales Reps & Total Commissions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_active_reps"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ACTIVE FIELD REPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$activeRepsCount Reps",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${staffList.size} total system users",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_commissions_payout"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderNavy)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Color(0xFFF472B6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "COMMISSION OBLIGATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF472B6))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currencyFormatter.format(totalCommissionPaid + totalCommissionPending),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paid: ${currencyFormatter.format(totalCommissionPaid)}",
                        fontSize = 11.sp,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Regional Tier Performance Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardNavy),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, borderNavy)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Regional Tier Revenue Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Comparative revenue distribution across logistics zones",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TierRevenueRow(tierName = "Tier 1 - Nairobi Urban Metro", revenue = tier1Revenue, total = totalMonthlyRevenue, currencyFormatter = currencyFormatter)
                Spacer(modifier = Modifier.height(8.dp))
                TierRevenueRow(tierName = "Tier 2 - Peri-Urban / Industrial", revenue = tier2Revenue, total = totalMonthlyRevenue, currencyFormatter = currencyFormatter)
                Spacer(modifier = Modifier.height(8.dp))
                TierRevenueRow(tierName = "Tier 3 - Remote / Upcountry", revenue = tier3Revenue, total = totalMonthlyRevenue, currencyFormatter = currencyFormatter)
            }
        }
    }
}

@Composable
private fun TierRevenueRow(
    tierName: String,
    revenue: Double,
    total: Double,
    currencyFormatter: NumberFormat
) {
    val percentage = if (total > 0.0) (revenue / total * 100.0).coerceIn(0.0, 100.0) else 0.0
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = tierName, fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Medium)
            Text(text = currencyFormatter.format(revenue), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percentage / 100.0).toFloat())
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFD4AF37))
            )
        }
    }
}
