package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object AdminReportPdfPrinter {

    fun exportAdminReport(
        context: Context,
        weeklyRevenue: Double,
        orderCount: Int,
        lowStockProducts: List<Product>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

            // Header Banner
            paint.color = Color.parseColor("#1E1B4B") // Dark Indigo
            canvas.drawRect(0f, 0f, 595f, 110f, paint)

            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ZAYED EXECUTIVE DASHBOARD REPORT", 30f, 50f, paint)

            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("7-DAY REVENUE PERFORMANCE & INVENTORY ALERTS", 30f, 80f, paint)

            // Executive Summary Box
            paint.color = Color.parseColor("#F3F4F6")
            canvas.drawRect(30f, 130f, 565f, 220f, paint)

            paint.color = Color.BLACK
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Key Business Indicators (Last 7 Days)", 45f, 155f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Total 7-Day Revenue: ${currencyFormat.format(weeklyRevenue)}", 45f, 180f, paint)
            canvas.drawText("Total Completed Orders: $orderCount", 45f, 200f, paint)

            canvas.drawText("Generated: ${dateFormat.format(Date())}", 330f, 180f, paint)

            // Inventory Alerts Section Header
            paint.color = Color.parseColor("#991B1B") // Dark Red
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("3-Layer Stock Warning Summary", 30f, 255f, paint)

            // Table Column Headers
            paint.color = Color.parseColor("#374151")
            paint.textSize = 11f
            canvas.drawText("Product Name", 30f, 280f, paint)
            canvas.drawText("Category", 220f, 280f, paint)
            canvas.drawText("Current Stock", 360f, 280f, paint)
            canvas.drawText("Warning Category", 460f, 280f, paint)

            paint.color = Color.GRAY
            canvas.drawLine(30f, 290f, 565f, 290f, paint)

            var yPos = 310f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            lowStockProducts.forEach { product ->
                if (yPos < 770f) {
                    paint.color = Color.BLACK
                    canvas.drawText(product.name.take(25), 30f, yPos, paint)
                    canvas.drawText(product.category.take(15), 220f, yPos, paint)
                    canvas.drawText("${product.currentStock} units", 360f, yPos, paint)

                    // Color badge text based on alert
                    paint.color = when (product.alertLevel) {
                        com.example.data.model.StockLevelAlert.OUT_OF_STOCK -> Color.RED
                        com.example.data.model.StockLevelAlert.CRITICAL -> Color.parseColor("#D97706") // Amber
                        com.example.data.model.StockLevelAlert.LOW -> Color.parseColor("#2563EB") // Blue
                        else -> Color.GREEN
                    }
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(product.alertLevel.name, 460f, yPos, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    yPos += 24f
                }
            }

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 10f
            canvas.drawText("Zayed SalesForce Enterprise Intelligence Document", 30f, 810f, paint)

            pdfDocument.finishPage(page)

            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            val pdfFile = File(outputDir, "Admin_Revenue_Report_${System.currentTimeMillis()}.pdf")

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Toast.makeText(context, "Exported PDF Report: ${pdfFile.name}", Toast.LENGTH_LONG).show()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    fun generateAdminDashboardPdfReport(
        context: Context,
        orders: List<Any>,
        products: List<Product>,
        companyProfile: Any? = null
    ): File? {
        val totalRevenue = orders.sumOf { 
            when (it) {
                is com.example.data.model.OrderEntity -> it.totalAmount
                is com.example.data.model.CustomerOrder -> it.totalPricing
                else -> 0.0
            }
        }
        val lowStock = products.filter { it.currentStock <= it.lowStockThreshold }
        return exportAdminReport(context, totalRevenue, orders.size, lowStock)
    }
}

