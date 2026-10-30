package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderWithItems
import com.example.ui.OrderBookingViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySalesTrend(
    val dayLabel: String,
    val dateStr: String,
    val totalRevenue: Double,
    val orderCount: Int
)

data class HourlySalesPoint(
    val hourLabel: String,
    val hourOfDay: Int,
    val revenue: Double,
    val orderCount: Int
)

/**
 * Composable screen using Vico charts that displays:
 * 1. 7-Day Sales Revenue Trend (Bar / Column chart & Line trend)
 * 2. Hourly Sales Timeline (Time-series dispatch and booking volume)
 * Designed for the Zayed SalesForce Executive Admin Dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSalesAnalyticsScreen(
    viewModel: OrderBookingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val borderNavy = Color(0xFF334155)

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    var chartType7Day by remember { mutableStateOf("COLUMN") } // "COLUMN" or "LINE"
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "DELIVERED", "BOOKED"

    // Filter orders based on status
    val filteredOrders = remember(orders, statusFilter) {
        when (statusFilter) {
            "DELIVERED" -> orders.filter { it.order.status == "DELIVERED" }
            "BOOKED" -> orders.filter { it.order.status == "BOOKED" || it.order.status == "SCHEDULED" }
            else -> orders
        }
    }

    // 1. Compute 7-Day Sales Revenue Trend
    val sevenDayTrends = remember(filteredOrders) {
        computeSevenDayTrends(filteredOrders)
    }

    // 2. Compute Hourly Sales Timeline
    val hourlyTrends = remember(filteredOrders) {
        computeHourlySalesTimeline(filteredOrders)
    }

    // Vico Model Producers
    val sevenDayModelProducer = remember { CartesianChartModelProducer.build() }
    val hourlyModelProducer = remember { CartesianChartModelProducer.build() }

    LaunchedEffect(sevenDayTrends, chartType7Day) {
        val revenues = sevenDayTrends.map { it.totalRevenue }
        sevenDayModelProducer.tryRunTransaction {
            if (chartType7Day == "COLUMN") {
                columnSeries {
                    series(revenues)
                }
            } else {
                lineSeries {
                    series(revenues)
                }
            }
        }
    }

    LaunchedEffect(hourlyTrends) {
        val hourlyRevenues = hourlyTrends.map { it.revenue }
        hourlyModelProducer.tryRunTransaction {
            lineSeries {
                series(hourlyRevenues)
            }
        }
    }

    val total7DayRevenue = remember(sevenDayTrends) { sevenDayTrends.sumOf { it.totalRevenue } }
    val total7DayOrders = remember(sevenDayTrends) { sevenDayTrends.sumOf { it.orderCount } }
    val avgDailyRevenue = if (sevenDayTrends.isNotEmpty()) total7DayRevenue / sevenDayTrends.size else 0.0
    val peakDay = remember(sevenDayTrends) { sevenDayTrends.maxByOrNull { it.totalRevenue } }
    val peakHour = remember(hourlyTrends) { hourlyTrends.maxByOrNull { it.revenue } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = deepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Executive Sales Analytics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "7-Day Revenue Trend & Hourly Dispatch Timeline",
                            fontSize = 11.sp,
                            color = imperialGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sales_analytics_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = imperialGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardNavy)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == "ALL",
                        onClick = { statusFilter = "ALL" },
                        label = { Text("All Orders (${orders.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = imperialGold,
                            selectedLabelColor = deepNavy
                        )
                    )
                    FilterChip(
                        selected = statusFilter == "DELIVERED",
                        onClick = { statusFilter = "DELIVERED" },
                        label = { Text("Delivered & Paid") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = imperialGold,
                            selectedLabelColor = deepNavy
                        )
                    )
                    FilterChip(
                        selected = statusFilter == "BOOKED",
                        onClick = { statusFilter = "BOOKED" },
                        label = { Text("Pending Dispatch") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = imperialGold,
                            selectedLabelColor = deepNavy
                        )
                    )
                }
            }

            // Summary KPI Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: 7-Day Revenue
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardNavy),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = imperialGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("7-DAY TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(total7DayRevenue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "$total7DayOrders orders total",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // KPI 2: Daily Average
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardNavy),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, borderNavy)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DAILY AVERAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(avgDailyRevenue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Peak: ${peakDay?.dayLabel ?: "N/A"}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // 1. 7-Day Sales Revenue Trend Chart Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_card_7day_sales"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardNavy),
                    border = BorderStroke(1.dp, borderNavy)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "7-Day Sales Revenue Trend",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Rolling 7-day revenue performance (KES)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Chart Type Toggle (Bar vs Line)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(deepNavy)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (chartType7Day == "COLUMN") imperialGold else Color.Transparent)
                                        .clickable { chartType7Day = "COLUMN" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "Bar Chart",
                                        tint = if (chartType7Day == "COLUMN") deepNavy else Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (chartType7Day == "LINE") imperialGold else Color.Transparent)
                                        .clickable { chartType7Day = "LINE" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = "Line Chart",
                                        tint = if (chartType7Day == "LINE") deepNavy else Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vico Cartesian Chart Host
                        if (sevenDayTrends.all { it.totalRevenue == 0.0 }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No order transactions in the last 7 days", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        } else {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    if (chartType7Day == "COLUMN") rememberColumnCartesianLayer() else rememberLineCartesianLayer(),
                                    startAxis = rememberStartAxis(),
                                    bottomAxis = rememberBottomAxis(),
                                ),
                                modelProducer = sevenDayModelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Day-by-Day Revenue Breakdown Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (trend in sevenDayTrends) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = trend.dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (trend.dateStr == sevenDayTrends.last().dateStr) imperialGold else Color.White
                                    )
                                    Text(
                                        text = if (trend.totalRevenue > 0) "${(trend.totalRevenue / 1000).toInt()}k" else "0",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Hourly Sales Timeline Chart Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_card_hourly_timeline"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardNavy),
                    border = BorderStroke(1.dp, borderNavy)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hourly Sales & Dispatch Timeline",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Distribution across operating hours (06:00 - 22:00)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            peakHour?.let {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = imperialGold.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, imperialGold)
                                ) {
                                    Text(
                                        text = "Peak: ${it.hourLabel}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = imperialGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hourly Line Chart
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                            ),
                            modelProducer = hourlyModelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hourly Highlights summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HourlyBucketSummary("Morning (06-12)", hourlyTrends.filter { it.hourOfDay in 6..11 }.sumOf { it.revenue }, currencyFormatter)
                            HourlyBucketSummary("Afternoon (12-17)", hourlyTrends.filter { it.hourOfDay in 12..16 }.sumOf { it.revenue }, currencyFormatter)
                            HourlyBucketSummary("Evening (17-22)", hourlyTrends.filter { it.hourOfDay in 17..21 }.sumOf { it.revenue }, currencyFormatter)
                        }
                    }
                }
            }

            // Historical Orders Activity List
            item {
                Text(
                    text = "Recent Dispatch Transactions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val recentOrders = filteredOrders.take(6)
            if (recentOrders.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions found.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(recentOrders.size) { index ->
                    val item = recentOrders[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardNavy.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.order.orderNumber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${item.order.businessName} • ${item.order.accountHolderName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(item.order.totalAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = imperialGold
                                )
                                Text(
                                    text = item.order.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.order.status) {
                                        "DELIVERED" -> Color(0xFF34D399)
                                        "BOOKED" -> Color(0xFF38BDF8)
                                        else -> Color(0xFFFBBF24)
                                    }
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
private fun HourlyBucketSummary(
    label: String,
    total: Double,
    formatter: NumberFormat
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        Text(
            text = formatter.format(total),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Calculates 7 daily buckets from 6 days ago through today.
 */
private fun computeSevenDayTrends(orders: List<OrderWithItems>): List<DailySalesTrend> {
    val calendar = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val result = mutableListOf<DailySalesTrend>()

    for (offset in -6..0) {
        val dayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }
        val targetDateStr = dateFormat.format(dayCal.time)
        val dayLabel = dayFormat.format(dayCal.time)

        // Sum orders matching this day
        val dayOrders = orders.filter { orderWithItems ->
            val orderDateStr = dateFormat.format(Date(orderWithItems.order.createdAt))
            orderDateStr == targetDateStr || orderWithItems.order.expectedDeliveryDate == targetDateStr
        }

        val totalRev = dayOrders.sumOf { it.order.totalAmount }
        result.add(
            DailySalesTrend(
                dayLabel = dayLabel,
                dateStr = targetDateStr,
                totalRevenue = totalRev,
                orderCount = dayOrders.size
            )
        )
    }

    return result
}

/**
 * Calculates 9 hourly intervals from 06:00 to 22:00 (every 2 hours) to map hourly dispatch timeline.
 */
private fun computeHourlySalesTimeline(orders: List<OrderWithItems>): List<HourlySalesPoint> {
    val hours = listOf(6, 8, 10, 12, 14, 16, 18, 20, 22)
    val calendar = Calendar.getInstance()

    return hours.map { hour ->
        val hourLabel = String.format("%02d:00", hour)
        val matchingOrders = orders.filter { item ->
            calendar.timeInMillis = item.order.createdAt
            val orderHour = calendar.get(Calendar.HOUR_OF_DAY)
            orderHour in hour until (hour + 2)
        }
        val rev = matchingOrders.sumOf { it.order.totalAmount }
        HourlySalesPoint(
            hourLabel = hourLabel,
            hourOfDay = hour,
            revenue = rev,
            orderCount = matchingOrders.size
        )
    }
}
