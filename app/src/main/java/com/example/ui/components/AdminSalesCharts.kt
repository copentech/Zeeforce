package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.CompanyProfile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ChartViewMode(val title: String) {
    SEVEN_DAY_TREND("7-Day Trend"),
    HOURLY_TIMELINE("24h Timeline"),
    REVENUE_VS_ORDERS("Revenue & Volume")
}

data class DailySalesMetric(
    val dayLabel: String,
    val dateStr: String,
    val revenue: Double,
    val orderCount: Int,
    val isToday: Boolean
)

data class HourlySalesMetric(
    val hourLabel: String,
    val hour: Int,
    val revenue: Double,
    val cumulativeRevenue: Double
)

/**
 * 100% Native Jetpack Compose Canvas Interactive Dashboard Suite
 * Features high-performance 7-day revenue bar charts, 24h hourly timelines,
 * revenue vs order volume comparative breakdowns, and 3-layer low-stock alert monitoring.
 */
@Composable
fun AdminSalesDashboardWidget(
    orders: List<OrderWithItems>,
    products: List<Product>,
    onQuickRestock: (productId: String, addedUnits: Int) -> Unit,
    companyProfile: CompanyProfile = CompanyProfile(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(ChartViewMode.SEVEN_DAY_TREND) }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }
    var showRestockDialogForProduct by remember { mutableStateOf<Product?>(null) }
    var showAllLowStockDialog by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = java.util.Currency.getInstance("KES")
        }
    }

    val imperialGold = Color(0xFFD4AF37)
    val deepNavy = Color(0xFF0F172A)
    val cardNavy = Color(0xFF1E293B)
    val barCyan = Color(0xFF38BDF8)
    val alertRed = Color(0xFFEF4444)

    // Calculate Low-Stock Items (finite products <= 10 units, ignoring infinite inventory where stockQty = -1)
    val lowStockProducts = remember(products) {
        products.filter { !it.isInfiniteStock && it.stockQty <= 10 }
    }

    // Aggregate 7-Day Metrics
    val dailyMetrics = remember(orders) {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val list = mutableListOf<DailySalesMetric>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = dateFormat.format(c.time)
            val lbl = dayFormat.format(c.time)

            val dayOrders = orders.filter {
                val orderDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.order.createdAt))
                orderDateStr == dStr
            }
            val rev = dayOrders.sumOf { it.order.totalAmount }
            list.add(
                DailySalesMetric(
                    dayLabel = lbl,
                    dateStr = dStr,
                    revenue = rev,
                    orderCount = dayOrders.size,
                    isToday = dStr == todayStr
                )
            )
        }
        list
    }

    // Aggregate Hourly Metrics (for today)
    val hourlyMetrics = remember(orders) {
        val list = mutableListOf<HourlySalesMetric>()
        val cal = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        var cumulative = 0.0
        val hourBuckets = listOf(6, 9, 12, 15, 18, 21, 24)
        for (h in hourBuckets) {
            val label = when (h) {
                6 -> "6 AM"
                9 -> "9 AM"
                12 -> "12 PM"
                15 -> "3 PM"
                18 -> "6 PM"
                21 -> "9 PM"
                else -> "12 AM"
            }
            val revInBucket = orders.filter {
                it.order.createdAt >= todayStart &&
                        Calendar.getInstance().apply { timeInMillis = it.order.createdAt }.get(Calendar.HOUR_OF_DAY) <= h
            }.sumOf { it.order.totalAmount }

            cumulative = revInBucket
            list.add(
                HourlySalesMetric(
                    hourLabel = label,
                    hour = h,
                    revenue = revInBucket,
                    cumulativeRevenue = cumulative
                )
            )
        }
        list
    }

    val todaySales = dailyMetrics.find { it.isToday }?.revenue ?: 0.0
    val todayOrdersCount = dailyMetrics.find { it.isToday }?.orderCount ?: 0
    val total7DayRevenue = dailyMetrics.sumOf { it.revenue }
    val totalOrdersCount = orders.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Executive Summary KPI Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_today_sales"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = imperialGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TODAY'S SALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = imperialGold)
                    }
                    Text(
                        text = currencyFormatter.format(todaySales),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$todayOrdersCount bookings today",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_7day_revenue"),
                colors = CardDefaults.cardColors(containerColor = cardNavy),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = barCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("7-DAY RUN RATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = barCyan)
                    }
                    Text(
                        text = currencyFormatter.format(total7DayRevenue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$totalOrdersCount lifetime orders",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Low-Stock Alert Warning Banner (Tier 1 & 2 Alert)
        if (lowStockProducts.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAllLowStockDialog = true }
                    .testTag("banner_low_stock_alert"),
                shape = RoundedCornerShape(10.dp),
                color = alertRed.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, alertRed)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(alertRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = alertRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CRITICAL STOCK ALERT: ${lowStockProducts.size} Items Depleted",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = alertRed
                            )
                            Text(
                                text = "Tap to review depleted items and restock instantly",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = alertRed
                    ) {
                        Text(
                            text = "Restock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Interactive Canvas Chart Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_admin_sales_chart"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardNavy),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header & View Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Revenue & Sales Analytics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "7-Day Revenue & Inventory Alerts",
                            fontSize = 10.sp,
                            color = imperialGold,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                com.example.util.AdminReportPdfPrinter.generateAdminDashboardPdfReport(
                                    context,
                                    orders,
                                    products,
                                    companyProfile
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = imperialGold),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("admin_export_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = deepNavy, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Export PDF", fontSize = 10.sp, color = deepNavy, fontWeight = FontWeight.Bold)
                        }

                        // Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = deepNavy,
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                ChartViewMode.values().forEach { mode ->
                                    val isSelected = viewMode == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) imperialGold else Color.Transparent)
                                            .clickable {
                                                viewMode = mode
                                                selectedBarIndex = -1
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = mode.title,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) deepNavy else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    when (viewMode) {
                        ChartViewMode.SEVEN_DAY_TREND -> {
                            SevenDayBarChartCanvas(
                                metrics = dailyMetrics,
                                selectedIndex = selectedBarIndex,
                                onBarSelected = { selectedBarIndex = it }
                            )
                        }
                        ChartViewMode.HOURLY_TIMELINE -> {
                            HourlyTimelineCanvas(
                                metrics = hourlyMetrics
                            )
                        }
                        ChartViewMode.REVENUE_VS_ORDERS -> {
                            RevenueAndVolumeComparativeCanvas(
                                metrics = dailyMetrics,
                                selectedIndex = selectedBarIndex,
                                onBarSelected = { selectedBarIndex = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Scrubbing Tooltip / Detail
                if (selectedBarIndex in dailyMetrics.indices) {
                    val m = dailyMetrics[selectedBarIndex]
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = deepNavy,
                        border = BorderStroke(1.dp, imperialGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (m.isToday) imperialGold else barCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${m.dayLabel} (${m.dateStr}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "${currencyFormatter.format(m.revenue)} • ${m.orderCount} Orders",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = imperialGold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 Tap any bar or line node for exact values",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Avg: ${currencyFormatter.format(if (dailyMetrics.isNotEmpty()) total7DayRevenue / 7 else 0.0)}/day",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Restock Dialog (for a single product)
        showRestockDialogForProduct?.let { product ->
            QuickRestockDialog(
                product = product,
                onDismiss = { showRestockDialogForProduct = null },
                onConfirmRestock = { units ->
                    onQuickRestock(product.id, units)
                    showRestockDialogForProduct = null
                }
            )
        }

        // All Low-Stock Items Sheet Dialog
        if (showAllLowStockDialog) {
            LowStockReviewDialog(
                lowStockProducts = lowStockProducts,
                onDismiss = { showAllLowStockDialog = false },
                onSelectToRestock = { product ->
                    showAllLowStockDialog = false
                    showRestockDialogForProduct = product
                }
            )
        }
    }
}

/**
 * Native Canvas 7-Day Bar Chart with gradient bars, baseline reference, and touch scrubbing
 */
@Composable
private fun SevenDayBarChartCanvas(
    metrics: List<DailySalesMetric>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    val imperialGold = Color(0xFFD4AF37)
    val barCyan = Color(0xFF38BDF8)
    val gridColor = Color(0xFF334155).copy(alpha = 0.5f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(metrics) {
                detectTapGestures { tapOffset ->
                    val barWidthWithSpace = size.width / metrics.size.toFloat()
                    val index = (tapOffset.x / barWidthWithSpace).toInt().coerceIn(0, metrics.size - 1)
                    onBarSelected(index)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val bottomMargin = 40f
        val topMargin = 20f
        val chartHeight = h - bottomMargin - topMargin
        val maxRevenue = metrics.maxOfOrNull { it.revenue }?.coerceAtLeast(10000.0) ?: 10000.0

        // Draw 3 horizontal grid lines
        for (i in 0..2) {
            val y = topMargin + chartHeight * (i / 2f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        val barSlotWidth = w / metrics.size.toFloat()
        val barWidth = barSlotWidth * 0.55f

        metrics.forEachIndexed { i, metric ->
            val x = i * barSlotWidth + (barSlotWidth - barWidth) / 2f
            val barHeight = ((metric.revenue / maxRevenue) * chartHeight).toFloat().coerceAtLeast(6f)
            val y = topMargin + chartHeight - barHeight

            val isSelected = selectedIndex == i
            val isToday = metric.isToday

            val gradientBrush = if (isToday || isSelected) {
                Brush.verticalGradient(
                    colors = listOf(imperialGold, Color(0xFFB45309)),
                    startY = y,
                    endY = topMargin + chartHeight
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(barCyan, Color(0xFF0369A1)),
                    startY = y,
                    endY = topMargin + chartHeight
                )
            }

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Selection indicator border
            if (isSelected) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x - 2f, y - 2f),
                    size = Size(barWidth + 4f, barHeight + 4f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2f)
                )
            }

            // Draw label
            val textPaint = android.graphics.Paint().apply {
                color = if (isToday) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#94A3B8")
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                if (isToday) isFakeBoldText = true
            }

            drawContext.canvas.nativeCanvas.drawText(
                metric.dayLabel + if (isToday) "•" else "",
                x + barWidth / 2f,
                h - 10f,
                textPaint
            )
        }
    }
}

/**
 * 24h Hourly Sales Cumulative Flow Line and Area Chart
 */
@Composable
private fun HourlyTimelineCanvas(
    metrics: List<HourlySalesMetric>
) {
    val imperialGold = Color(0xFFD4AF37)
    val gridColor = Color(0xFF334155).copy(alpha = 0.5f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val bottomMargin = 40f
        val topMargin = 20f
        val chartHeight = h - bottomMargin - topMargin
        val maxRev = metrics.maxOfOrNull { it.cumulativeRevenue }?.coerceAtLeast(10000.0) ?: 10000.0

        // Grid lines
        for (i in 0..2) {
            val y = topMargin + chartHeight * (i / 2f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        if (metrics.size >= 2) {
            val stepX = w / (metrics.size - 1).toFloat()
            val path = Path()
            val fillPath = Path()

            metrics.forEachIndexed { i, metric ->
                val x = i * stepX
                val y = topMargin + chartHeight - ((metric.cumulativeRevenue / maxRev) * chartHeight).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, topMargin + chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (i == metrics.size - 1) {
                    fillPath.lineTo(x, topMargin + chartHeight)
                    fillPath.close()
                }

                // Node dot
                drawCircle(
                    color = imperialGold,
                    radius = 4f,
                    center = Offset(x, y)
                )

                // Label
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    metric.hourLabel,
                    x,
                    h - 10f,
                    textPaint
                )
            }

            // Fill gradient below line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(imperialGold.copy(alpha = 0.35f), Color.Transparent),
                    startY = topMargin,
                    endY = topMargin + chartHeight
                )
            )

            // Stroke line
            drawPath(
                path = path,
                color = imperialGold,
                style = Stroke(width = 3f)
            )
        }
    }
}

/**
 * Comparative Dual Bar Chart showing Gross Revenue vs Delivered Order Volume
 */
@Composable
private fun RevenueAndVolumeComparativeCanvas(
    metrics: List<DailySalesMetric>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    val imperialGold = Color(0xFFD4AF37)
    val orderColor = Color(0xFF10B981)
    val gridColor = Color(0xFF334155).copy(alpha = 0.5f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(metrics) {
                detectTapGestures { tapOffset ->
                    val barWidthWithSpace = size.width / metrics.size.toFloat()
                    val index = (tapOffset.x / barWidthWithSpace).toInt().coerceIn(0, metrics.size - 1)
                    onBarSelected(index)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val bottomMargin = 40f
        val topMargin = 20f
        val chartHeight = h - bottomMargin - topMargin
        val maxRevenue = metrics.maxOfOrNull { it.revenue }?.coerceAtLeast(10000.0) ?: 10000.0
        val maxOrders = metrics.maxOfOrNull { it.orderCount }?.coerceAtLeast(5) ?: 5

        val slotWidth = w / metrics.size.toFloat()
        val individualBarWidth = slotWidth * 0.32f

        metrics.forEachIndexed { i, metric ->
            val slotX = i * slotWidth
            val revHeight = ((metric.revenue / maxRevenue) * chartHeight).toFloat().coerceAtLeast(4f)
            val revY = topMargin + chartHeight - revHeight

            val orderHeight = ((metric.orderCount.toFloat() / maxOrders) * chartHeight).coerceAtLeast(4f)
            val orderY = topMargin + chartHeight - orderHeight

            // Bar 1: Revenue
            drawRoundRect(
                color = imperialGold,
                topLeft = Offset(slotX + slotWidth * 0.15f, revY),
                size = Size(individualBarWidth, revHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Bar 2: Order Count
            drawRoundRect(
                color = orderColor,
                topLeft = Offset(slotX + slotWidth * 0.52f, orderY),
                size = Size(individualBarWidth, orderHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                metric.dayLabel,
                slotX + slotWidth / 2f,
                h - 10f,
                textPaint
            )
        }
    }
}

/**
 * Quick One-Tap Restock Modal Dialog
 */
@Composable
fun QuickRestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirmRestock: (addedUnits: Int) -> Unit
) {
    var addedUnitsText by remember { mutableStateOf("50") }
    var selectedPreset by remember { mutableIntStateOf(50) }

    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardNavy,
            border = BorderStroke(1.dp, imperialGold),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_quick_restock")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = imperialGold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Instant Inventory Restock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = product.name,
                            fontSize = 12.sp,
                            color = imperialGold,
                            maxLines = 1
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Available Stock:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = if (product.isInfiniteStock) "Infinite (∞)" else "${product.stockQty} ${product.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stockQty <= 10 && !product.isInfiniteStock) Color(0xFFEF4444) else Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 50, 100, 250).forEach { preset ->
                        val isSel = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) imperialGold else Color(0xFF334155),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                    addedUnitsText = preset.toString()
                                }
                        ) {
                            Text(
                                text = "+$preset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = addedUnitsText,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }
                        addedUnitsText = filtered
                        selectedPreset = filtered.toIntOrNull() ?: 0
                    },
                    label = { Text("Units to Add", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_restock_units")
                )

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
                            val units = addedUnitsText.toIntOrNull() ?: 0
                            if (units > 0) {
                                onConfirmRestock(units)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_restock"),
                        colors = ButtonDefaults.buttonColors(containerColor = imperialGold, contentColor = Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stock Units", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Review all products currently in low-stock status
 */
@Composable
fun LowStockReviewDialog(
    lowStockProducts: List<Product>,
    onDismiss: () -> Unit,
    onSelectToRestock: (Product) -> Unit
) {
    val imperialGold = Color(0xFFD4AF37)
    val cardNavy = Color(0xFF1E293B)
    val alertRed = Color(0xFFEF4444)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardNavy,
            border = BorderStroke(1.dp, alertRed),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_low_stock_review")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Depleted Inventory (${lowStockProducts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = alertRed
                        )
                        Text(
                            text = "Items with stock <= 10 units (Excluding infinite inventory)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    lowStockProducts.forEach { prod ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectToRestock(prod) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = prod.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "${prod.category} • ${prod.unit}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = alertRed.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, alertRed)
                                    ) {
                                        Text(
                                            text = "${prod.stockQty} left",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = alertRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Add, contentDescription = "Restock", tint = imperialGold, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
