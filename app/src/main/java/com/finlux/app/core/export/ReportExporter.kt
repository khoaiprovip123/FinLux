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

    /**
     * Exports a genuine 2-Sheet OpenXML (.xlsx) Excel workbook.
     * Sheet 1: Chi tiết giao dịch
     * Sheet 2: Tổng hợp danh mục & KPI
     */
    fun exportToXlsx(
        context: Context,
        range: ReportRange,
        summary: DashboardSummary,
        expensesByCategory: List<CategoryExpense>,
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        wallets: List<Wallet>,
    ): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "FinLux_BaoCao_${range.start.format(DateTimeFormatter.BASIC_ISO_DATE)}_${range.end.format(DateTimeFormatter.BASIC_ISO_DATE)}.xlsx"
        val file = File(exportDir, fileName)

        XlsxReportWriter.writeReport(
            outputFile = file,
            range = range,
            summary = summary,
            expensesByCategory = expensesByCategory,
            transactions = transactions,
            categories = categories,
            wallets = wallets,
        )

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * CSV fallback exporter.
     */
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
                TransactionType.EXPENSE -> if (tx.categoryId == "debt_payment") "Trả nợ" else "Chi tiêu"
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

    /**
     * Generates a beautifully formatted Multi-page PDF Report with Visual KPI and Category Distribution Bars.
     */
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
            color = Color.rgb(30, 64, 175) // Rich Blue
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintHeader = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 9.5f
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paintCard = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50
            style = Paint.Style.FILL
        }

        val paintIncome = Paint(paintHeader).apply { color = Color.rgb(16, 185, 129) } // Emerald 500
        val paintExpense = Paint(paintHeader).apply { color = Color.rgb(239, 68, 68) } // Red 500

        val categoryColors = listOf(
            Color.rgb(99, 102, 241),  // Indigo
            Color.rgb(16, 185, 129),  // Emerald
            Color.rgb(245, 158, 11),  // Amber
            Color.rgb(236, 72, 153),  // Pink
            Color.rgb(14, 165, 233),  // Sky
        )

        var y = 45f

        // Draw Header
        canvas.drawText("FINLUX - BÁO CÁO TÀI CHÍNH CÁ NHÂN", 40f, y, paintTitle)
        y += 20f
        paintBody.color = Color.rgb(100, 116, 139)
        canvas.drawText("Khoảng thời gian: ${range.start.format(dateFormatter)} - ${range.end.format(dateFormatter)} | Ngày xuất: ${java.time.LocalDate.now().format(dateFormatter)}", 40f, y, paintBody)
        y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 22f

        // KPI Summary Box
        canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 68f, 10f, 10f, paintCard)
        canvas.drawText("TỔNG THU NHẬP", 55f, y + 22f, paintBody)
        canvas.drawText("+${summary.income.value.toVnd()}", 55f, y + 46f, paintIncome)

        canvas.drawText("TỔNG CHI TIÊU", 230f, y + 22f, paintBody)
        canvas.drawText("-${summary.expense.value.toVnd()}", 230f, y + 46f, paintExpense)

        canvas.drawText("THU RÒNG (DƯ / THÂM HỤT)", 400f, y + 22f, paintBody)
        val paintNet = Paint(paintHeader).apply {
            color = if (summary.net >= 0) Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
        }
        val netPrefix = if (summary.net > 0) "+" else ""
        canvas.drawText("${netPrefix}${summary.net.toVnd()}", 400f, y + 46f, paintNet)
        y += 90f

        // Top Category Breakdown with Visual Progress Bars
        canvas.drawText("CƠ CẤU CHI TIÊU THEO DANH MỤC", 40f, y, paintHeader)
        y += 18f
        val totalExpense = summary.expense.value
        val topCategories = expensesByCategory.take(5)

        if (topCategories.isNotEmpty() && totalExpense > 0L) {
            topCategories.forEachIndexed { index, item ->
                val catName = item.category?.name ?: "Khác"
                val ratio = (item.amount * 100.0 / totalExpense)
                val color = categoryColors[index % categoryColors.size]

                val paintCatColor = Paint().apply {
                    this.color = color
                    isAntiAlias = true
                }

                // Category Dot
                canvas.drawCircle(46f, y - 4f, 4.5f, paintCatColor)

                // Category Label & Amount
                paintBody.color = Color.rgb(30, 41, 59)
                paintBody.isFakeBoldText = true
                canvas.drawText(catName, 58f, y, paintBody)
                paintBody.isFakeBoldText = false

                paintBody.color = Color.rgb(100, 116, 139)
                val amountAndRatioText = "${item.amount.toVnd()} (${String.format("%.1f%%", ratio)})"
                canvas.drawText(amountAndRatioText, pageWidth - 160f, y, paintBody)

                // Visual Progress Bar
                y += 6f
                val barBgPaint = Paint().apply {
                    this.color = Color.rgb(241, 245, 249)
                    style = Paint.Style.FILL
                }
                val barFillPaint = Paint().apply {
                    this.color = color
                    style = Paint.Style.FILL
                }
                val barWidth = (pageWidth - 80f)
                val fillWidth = (barWidth * (ratio.toFloat() / 100f)).coerceIn(4f, barWidth)

                canvas.drawRoundRect(40f, y, 40f + barWidth, y + 5f, 2.5f, 2.5f, barBgPaint)
                canvas.drawRoundRect(40f, y, 40f + fillWidth, y + 5f, 2.5f, 2.5f, barFillPaint)

                y += 18f
            }
        } else {
            paintBody.color = Color.rgb(100, 116, 139)
            canvas.drawText("Không có dữ liệu chi tiêu trong kỳ", 50f, y, paintBody)
            y += 18f
        }

        y += 10f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 22f

        // Transactions Table Header
        canvas.drawText("DANH SÁCH GIAO DỊCH (${transactions.size} giao dịch)", 40f, y, paintHeader)
        y += 18f

        paintHeader.color = Color.rgb(100, 116, 139)
        paintHeader.textSize = 10f
        canvas.drawText("Ngày", 40f, y, paintHeader)
        canvas.drawText("Danh mục / Ghi chú", 110f, y, paintHeader)
        canvas.drawText("Ví", 340f, y, paintHeader)
        canvas.drawText("Số tiền", 450f, y, paintHeader)
        y += 8f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
        y += 16f

        // Draw Transactions Rows
        transactions.forEach { tx ->
            if (y > pageHeight - 55f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 45f

                // Re-draw table header on next page
                paintHeader.color = Color.rgb(30, 41, 59)
                paintHeader.textSize = 12f
                canvas.drawText("DANH SÁCH GIAO DỊCH (tiếp theo)", 40f, y, paintHeader)
                y += 20f
                paintHeader.color = Color.rgb(100, 116, 139)
                paintHeader.textSize = 10f
                canvas.drawText("Ngày", 40f, y, paintHeader)
                canvas.drawText("Danh mục / Ghi chú", 110f, y, paintHeader)
                canvas.drawText("Ví", 340f, y, paintHeader)
                canvas.drawText("Số tiền", 450f, y, paintHeader)
                y += 8f
                canvas.drawLine(40f, y, pageWidth - 40f, y, paintLine)
                y += 16f
            }

            val dateStr = tx.date.atZone(ZoneId.systemDefault()).format(dateFormatter)
            val catName = if (tx.categoryId == "debt_payment") "Trả nợ & Tín dụng" else tx.categoryId?.let { categoryMap[it]?.name } ?: "Giao dịch"
            val noteSnippet = if (tx.note.isNotBlank()) " (${tx.note.take(15)})" else ""
            val walletName = walletMap[tx.walletId]?.name?.take(12) ?: "Ví"

            paintBody.color = Color.rgb(71, 85, 105)
            canvas.drawText(dateStr, 40f, y, paintBody)
            canvas.drawText("${catName}${noteSnippet}".take(32), 110f, y, paintBody)
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

            y += 16f
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
