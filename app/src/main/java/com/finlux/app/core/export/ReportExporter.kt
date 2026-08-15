package com.finlux.app.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.ReportRange
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun exportToCsv(
        context: Context,
        range: ReportRange,
        summary: DashboardSummary,
        expensesByCategory: List<CategoryExpense>,
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        wallets: List<Wallet>,
    ): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "FinLux_BaoCao_${range.start.format(DateTimeFormatter.BASIC_ISO_DATE)}_${range.end.format(DateTimeFormatter.BASIC_ISO_DATE)}.csv"
        val file = File(exportDir, fileName)

        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)

        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")

        // Title & Metadata
        sb.append("BÁO CÁO TÀI CHÍNH CÁ NHÂN FINLUX\n")
        sb.append("Khoảng thời gian:,Từ ${range.start.format(dateFormatter)} đến ${range.end.format(dateFormatter)}\n")
        sb.append("Ngày xuất báo cáo:,${java.time.LocalDate.now().format(dateFormatter)}\n\n")

        // Section 1: Summary KPI
        sb.append("1. TỔNG QUAN DÒNG TIỀN\n")
        sb.append("Chỉ số,Số tiền (VNĐ)\n")
        sb.append("Tổng Thu Nhập,${summary.income.value}\n")
        sb.append("Tổng Chi Tiêu,${summary.expense.value}\n")
        sb.append("Thu Ròng (Dư / Thâm hụt),${summary.net}\n")
        sb.append("Tổng số giao dịch,${transactions.size}\n\n")

        // Section 2: Expenses by Category
        sb.append("2. PHÂN BỔ CHI TIÊU THEO DANH MỤC\n")
        sb.append("Danh Mục,Số Tiền (VNĐ),Tỷ Trọng (%)\n")
        val totalExpense = summary.expense.value
        expensesByCategory.forEach { item ->
            val catName = item.category?.name ?: "Khác"
            val ratio = if (totalExpense > 0) (item.amount * 100.0 / totalExpense) else 0.0
            sb.append("\"${catName}\",${item.amount},${String.format("%.1f%%", ratio)}\n")
        }
        sb.append("\n")

        // Section 3: Detailed Transactions
        sb.append("3. DANH SÁCH GIAO DỊCH CHI TIẾT\n")
        sb.append("Thời gian,Loại giao dịch,Danh mục,Ví thanh toán,Số tiền (VNĐ),Ghi chú\n")
        transactions.forEach { tx ->
            val dateStr = tx.date.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
            val typeStr = when (tx.type) {
                TransactionType.INCOME -> "Thu nhập"
                TransactionType.EXPENSE -> "Chi tiêu"
                TransactionType.TRANSFER_OUT -> "Chuyển đi"
                TransactionType.TRANSFER_IN -> "Nhận chuyển"
            }
            val catStr = tx.categoryId?.let { categoryMap[it]?.name } ?: "Không có"
            val walletStr = walletMap[tx.walletId]?.name ?: "Ví không xác định"
            val safeNote = tx.note.replace("\"", "\"\"")
            sb.append("${dateStr},${typeStr},\"${catStr}\",\"${walletStr}\",${tx.amount.value},\"${safeNote}\"\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun exportToPdf(
        context: Context,
        range: ReportRange,
        summary: DashboardSummary,
        expensesByCategory: List<CategoryExpense>,
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        wallets: List<Wallet>,
    ): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "FinLux_BaoCao_${range.start.format(DateTimeFormatter.BASIC_ISO_DATE)}_${range.end.format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"
        val file = File(exportDir, fileName)

        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842 // A4 standard size in points
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.rgb(22, 138, 98)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintHeader = Paint().apply {
            color = Color.rgb(40, 50, 60)
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(60, 70, 80)
            textSize = 10f
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = Color.rgb(210, 215, 220)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintCard = Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }

        var y = 45f

        // Draw Header
        canvas.drawText("FINLUX - BÁO CÁO TÀI CHÍNH CÁ NHÂN", 40f, y, paintTitle)
        y += 20f
        paintBody.color = Color.rgb(100, 110, 120)
        canvas.drawText("Khoảng thời gian: ${range.start.format(dateFormatter)} - ${range.end.format(dateFormatter)} | Ngày xuất: ${java.time.LocalDate.now().format(dateFormatter)}", 40f, y, paintBody)
        y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 25f

        // KPI Summary Box
        canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 65f, 8f, 8f, paintCard)
        canvas.drawText("TỔNG THU NHẬP", 55f, y + 22f, paintBody)
        val paintIncome = Paint(paintHeader).apply { color = Color.rgb(22, 138, 98) }
        canvas.drawText("+${summary.income.value.toVnd()}", 55f, y + 45f, paintIncome)

        canvas.drawText("TỔNG CHI TIÊU", 230f, y + 22f, paintBody)
        val paintExpense = Paint(paintHeader).apply { color = Color.rgb(217, 75, 91) }
        canvas.drawText("-${summary.expense.value.toVnd()}", 230f, y + 45f, paintExpense)

        canvas.drawText("THU RÒNG (DƯ/THÂM HỤT)", 400f, y + 22f, paintBody)
        val paintNet = Paint(paintHeader).apply {
            color = if (summary.net >= 0) Color.rgb(22, 138, 98) else Color.rgb(217, 75, 91)
        }
        val netPrefix = if (summary.net > 0) "+" else ""
        canvas.drawText("${netPrefix}${summary.net.toVnd()}", 400f, y + 45f, paintNet)
        y += 90f

        // Top Category Breakdown
        canvas.drawText("CƠ CẤU CHI TIÊU THEO DANH MỤC", 40f, y, paintHeader)
        y += 18f
        val totalExpense = summary.expense.value
        expensesByCategory.take(5).forEach { item ->
            val catName = item.category?.name ?: "Khác"
            val ratio = if (totalExpense > 0) (item.amount * 100.0 / totalExpense) else 0.0
            paintBody.color = Color.rgb(60, 70, 80)
            canvas.drawText("• ${catName}: ${item.amount.toVnd()} (${String.format("%.1f%%", ratio)})", 50f, y, paintBody)
            y += 16f
        }
        y += 15f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 25f

        // Transactions Table Header
        canvas.drawText("DANH SÁCH GIAO DỊCH (${transactions.size} giao dịch)", 40f, y, paintHeader)
        y += 20f

        paintHeader.color = Color.rgb(80, 90, 100)
        canvas.drawText("Ngày", 40f, y, paintHeader)
        canvas.drawText("Danh mục / Ghi chú", 110f, y, paintHeader)
        canvas.drawText("Ví", 340f, y, paintHeader)
        canvas.drawText("Số tiền", 450f, y, paintHeader)
        y += 10f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 18f

        // Draw Transactions Rows
        transactions.forEach { tx ->
            if (y > pageHeight - 60f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 45f

                // Re-draw table header on next page
                canvas.drawText("DANH SÁCH GIAO DỊCH (tiếp theo)", 40f, y, paintHeader)
                y += 20f
                canvas.drawText("Ngày", 40f, y, paintHeader)
                canvas.drawText("Danh mục / Ghi chú", 110f, y, paintHeader)
                canvas.drawText("Ví", 340f, y, paintHeader)
                canvas.drawText("Số tiền", 450f, y, paintHeader)
                y += 10f
                canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
                y += 18f
            }

            val dateStr = tx.date.atZone(ZoneId.systemDefault()).format(dateFormatter)
            val catName = tx.categoryId?.let { categoryMap[it]?.name } ?: "Giao dịch"
            val noteSnippet = if (tx.note.isNotBlank()) " (${tx.note.take(15)})" else ""
            val walletName = walletMap[tx.walletId]?.name?.take(10) ?: "Ví"

            paintBody.color = Color.rgb(70, 80, 90)
            canvas.drawText(dateStr, 40f, y, paintBody)
            canvas.drawText("${catName}${noteSnippet}".take(30), 110f, y, paintBody)
            canvas.drawText(walletName, 340f, y, paintBody)

            val amountPaint = when (tx.type) {
                TransactionType.INCOME, TransactionType.TRANSFER_IN -> paintIncome
                TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> paintExpense
            }
            val prefix = when (tx.type) {
                TransactionType.INCOME, TransactionType.TRANSFER_IN -> "+"
                TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> "-"
            }
            canvas.drawText("${prefix}${tx.amount.value.toVnd()}", 450f, y, amountPaint)

            y += 18f
        }

        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareExportedFile(context: Context, fileUri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
