package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.model.CommissionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object CommissionPdfPrinter {

    fun generateCommissionSlip(
        context: Context,
        salesRepName: String,
        salesRepId: String,
        records: List<CommissionRecord>,
        currencyCode: String = "USD"
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currencyFormat = NumberFormat.getCurrencyInstance().apply {
                currency = java.util.Currency.getInstance(currencyCode)
            }

            // Header - Brand Blue
            paint.color = Color.parseColor("#0F52BA")
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ZAYED SALESFORCE", 30f, 45f, paint)

            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("OFFICIAL SALES COMMISSION SLIP", 30f, 75f, paint)

            try {
                val logoBmp = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.zayed_logo)
                if (logoBmp != null) {
                    val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBmp, 65, 65, true)
                    canvas.drawBitmap(scaledLogo, 500f, 18f, paint)
                }
            } catch (_: Exception) {}

            // Sales Rep Details Box
            paint.color = Color.parseColor("#F0F4F8")
            canvas.drawRect(30f, 120f, 565f, 190f, paint)

            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Representative Name: $salesRepName", 45f, 145f, paint)
            canvas.drawText("Staff ID: $salesRepId", 45f, 165f, paint)

            canvas.drawText("Date: ${dateFormat.format(Date())}", 350f, 145f, paint)
            canvas.drawText("Status: OFFICIAL STATEMENT", 350f, 165f, paint)

            // Table Header
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 11f
            canvas.drawText("Order ID", 30f, 220f, paint)
            canvas.drawText("Order Amt", 150f, 220f, paint)
            canvas.drawText("Structure Mode", 260f, 220f, paint)
            canvas.drawText("Rate", 420f, 220f, paint)
            canvas.drawText("Earned ($)", 500f, 220f, paint)

            paint.color = Color.LTGRAY
            canvas.drawLine(30f, 230f, 565f, 230f, paint)

            // Table Body
            var yPos = 250f
            var totalCommission = 0.0

            paint.color = Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            records.forEach { record ->
                if (yPos < 750f) {
                    canvas.drawText(record.orderId.takeLast(10), 30f, yPos, paint)
                    canvas.drawText(currencyFormat.format(record.orderAmount), 150f, yPos, paint)
                    canvas.drawText(record.commissionType.name, 260f, yPos, paint)
                    val rateStr = if (record.commissionType == com.example.data.model.CommissionType.FLAT_FIXED) "$${record.commissionRate}" else "${record.commissionRate}%"
                    canvas.drawText(rateStr, 420f, yPos, paint)
                    canvas.drawText(currencyFormat.format(record.calculatedCommission), 500f, yPos, paint)

                    totalCommission += record.calculatedCommission
                    yPos += 24f
                }
            }

            // Summary Divider
            paint.color = Color.parseColor("#0F52BA")
            canvas.drawLine(30f, yPos + 10f, 565f, yPos + 10f, paint)

            // Total Summary Box
            yPos += 40f
            paint.color = Color.parseColor("#E0F2FE")
            canvas.drawRect(300f, yPos - 20f, 565f, yPos + 30f, paint)

            paint.color = Color.parseColor("#0369A1")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NET EARNED COMMISSION:", 310f, yPos, paint)
            canvas.drawText(currencyFormat.format(totalCommission), 310f, yPos + 20f, paint)

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Generated automatically by Zayed SalesForce System. Valid without signature.", 30f, 810f, paint)

            pdfDocument.finishPage(page)

            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            val pdfFile = File(outputDir, "Commission_Slip_${salesRepName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Toast.makeText(context, "Commission Slip saved to ${pdfFile.name}", Toast.LENGTH_LONG).show()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    fun printCommissionSlip(context: Context, data: Any?): File? {
        return generateCommissionSlip(
            context = context,
            salesRepName = "Sales Representative",
            salesRepId = "AYBZ-001-SR",
            records = emptyList()
        )
    }
}

