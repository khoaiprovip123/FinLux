package com.finlux.app.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.ReportRange
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        sb.append("Ngày xuất báo cáo:,${LocalDate.now().format(dateFormatter)}\n\n")

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
            sb.append("\"${catName}\",${item.amount},${String.format(Locale.US, "%.1f%%", ratio)}\n")
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
     * Generates a beautifully formatted Multi-page PDF Report with Visual KPI,
     * Category Distribution Progress Bars, and Financial Statement Tabular Layout.
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

        // Palette & Paints
        val paintTitle = Paint().apply {
            color = Color.rgb(30, 58, 138) // Deep Blue #1E3A8A
            textSize = 17f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate 500 #64748B
            textSize = 8.5f
            isAntiAlias = true
        }

        val paintSectionHeader = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900 #0F172A
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintTableHeaderBg = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100 #F1F5F9
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintTableHeaderText = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600 #475569
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintZebraRowBg = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50 #F8FAFC
            style = Paint.Style.FILL
        }

        val paintRowLine = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200 #E2E8F0
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val paintCard = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintCardBorder = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val paintKpiLabel = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate 500
            textSize = 8f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintIncome = Paint().apply {
            color = Color.rgb(22, 163, 74) // Emerald 600 #16A34A
            textSize = 11.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintExpense = Paint().apply {
            color = Color.rgb(220, 38, 38) // Red 600 #DC2626
            textSize = 11.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintCatName = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800 #1E293B
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintCatNote = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate 500 #64748B
            textSize = 7.8f
            isAntiAlias = true
        }

        val paintDate = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 8.2f
            isAntiAlias = true
        }

        val paintWallet = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 8.5f
            isAntiAlias = true
        }

        val paintAmountIncome = Paint().apply {
            color = Color.rgb(22, 163, 74) // Emerald 600 #16A34A
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val paintAmountExpense = Paint().apply {
            color = Color.rgb(220, 38, 38) // Red 600 #DC2626
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val paintProgressBarBg = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200 #E2E8F0
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val defaultCategoryColors = listOf(
            Color.rgb(99, 102, 241),  // Indigo
            Color.rgb(16, 185, 129),  // Emerald
            Color.rgb(245, 158, 11),  // Amber
            Color.rgb(236, 72, 153),  // Pink
            Color.rgb(14, 165, 233),  // Sky
        )

        var y = 42f

        // 1. Header Banner
        canvas.drawText("FINLUX - BÁO CÁO TÀI CHÍNH CÁ NHÂN", 40f, y, paintTitle)
        y += 16f
        val subtitleText = "Khoảng thời gian: ${range.start.format(dateFormatter)} - ${range.end.format(dateFormatter)}   |   Ngày xuất báo cáo: ${LocalDate.now().format(dateFormatter)}"
        canvas.drawText(subtitleText, 40f, y, paintSubtitle)
        y += 14f
        canvas.drawLine(40f, y, 555f, y, paintRowLine)
        y += 16f

        // 2. Summary KPI Box
        val kpiBoxHeight = 62f
        canvas.drawRoundRect(40f, y, 555f, y + kpiBoxHeight, 8f, 8f, paintCard)
        canvas.drawRoundRect(40f, y, 555f, y + kpiBoxHeight, 8f, 8f, paintCardBorder)

        // Col 1: Tổng Thu Nhập
        canvas.drawText("TỔNG THU NHẬP", 55f, y + 20f, paintKpiLabel)
        canvas.drawText("+${formatVndAmount(summary.income.value)}", 55f, y + 43f, paintIncome)

        // Col 2: Tổng Chi Tiêu
        canvas.drawText("TỔNG CHI TIÊU", 225f, y + 20f, paintKpiLabel)
        canvas.drawText("-${formatVndAmount(summary.expense.value)}", 225f, y + 43f, paintExpense)

        // Col 3: Thu Ròng (Dư/Thâm hụt)
        canvas.drawText("THU RÒNG (DƯ / THÂM HỤT)", 395f, y + 20f, paintKpiLabel)
        val paintNet = Paint().apply {
            color = if (summary.net >= 0) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
            textSize = 11.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val netPrefix = if (summary.net > 0) "+" else ""
        canvas.drawText("${netPrefix}${formatVndAmount(summary.net)}", 395f, y + 43f, paintNet)
        y += kpiBoxHeight + 22f

        // 3. Top Category Breakdown with Polished Progress Bars
        canvas.drawText("CƠ CẤU CHI TIÊU THEO DANH MỤC", 40f, y, paintSectionHeader)
        y += 14f
        val totalExpense = summary.expense.value
        val topCategories = expensesByCategory.take(5)

        if (topCategories.isNotEmpty() && totalExpense > 0L) {
            topCategories.forEachIndexed { index, item ->
                val catName = item.category?.name ?: "Khác"
                val ratio = (item.amount * 100.0 / totalExpense)
                val fallbackColor = defaultCategoryColors[index % defaultCategoryColors.size]
                val catColorInt = parseColorHex(item.category?.colorHex, fallbackColor)

                val paintDot = Paint().apply {
                    color = catColorInt
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }

                // Line 1: Dot + Category Name (Left) & Amount + Ratio (Right Align at 555f)
                canvas.drawCircle(46f, y + 6.5f, 3.5f, paintDot)

                val truncatedCatName = smartEllipsize(paintCatName, catName, 280f)
                canvas.drawText(truncatedCatName, 56f, y + 10f, paintCatName)

                val amountAndRatioText = "${formatVndAmount(item.amount)} (${String.format(Locale.US, "%.1f%%", ratio)})"
                val paintStats = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 8.8f
                    isAntiAlias = true
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText(amountAndRatioText, 555f, y + 10f, paintStats)

                // Line 2: Progress Bar
                val barTop = y + 16f
                val barBottom = y + 21f
                val totalBarWidth = 515f // from x=40 to x=555
                val fillWidth = (totalBarWidth * (ratio.toFloat() / 100f)).coerceIn(4f, totalBarWidth)

                canvas.drawRoundRect(40f, barTop, 555f, barBottom, 2.5f, 2.5f, paintProgressBarBg)

                val paintBarFill = Paint().apply {
                    color = catColorInt
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(40f, barTop, 40f + fillWidth, barBottom, 2.5f, 2.5f, paintBarFill)

                y += 27f
            }
        } else {
            canvas.drawText("Không có dữ liệu chi tiêu trong kỳ", 40f, y + 10f, paintSubtitle)
            y += 18f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, paintRowLine)
        y += 20f

        // Helper to draw clean table header
        fun drawTableHeader(c: Canvas, startY: Float, isContinued: Boolean = false) {
            var curY = startY
            val titleText = if (isContinued) "DANH SÁCH GIAO DỊCH (TIẾP THEO)" else "DANH SÁCH GIAO DỊCH (${transactions.size} giao dịch)"
            c.drawText(titleText, 40f, curY, paintSectionHeader)
            curY += 13f

            val headerHeight = 22f
            c.drawRoundRect(40f, curY, 555f, curY + headerHeight, 4f, 4f, paintTableHeaderBg)

            val textY = curY + 14.5f
            // Col 1: Thời gian (Ngày & Giờ: dd/MM/yyyy HH:mm)
            c.drawText("THỜI GIAN", 46f, textY, paintTableHeaderText)
            // Col 2: Danh mục & Ghi chú
            c.drawText("DANH MỤC & GHI CHÚ", 142f, textY, paintTableHeaderText)
            // Col 3: Ví
            c.drawText("VÍ THANH TOÁN", 355f, textY, paintTableHeaderText)
            // Col 4: Số tiền (Align Right at 547f)
            val paintCol4Header = Paint(paintTableHeaderText).apply { textAlign = Paint.Align.RIGHT }
            c.drawText("SỐ TIỀN", 547f, textY, paintCol4Header)
        }

        // Draw initial table header
        drawTableHeader(canvas, y, isContinued = false)
        y += 40f

        // 4. Draw Transactions Rows with Zebra Striping & 2-Line Category/Note
        val rowHeight = 28f

        transactions.forEachIndexed { index, tx ->
            // Check page overflow
            if (y + rowHeight > pageHeight - 45f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 42f

                drawTableHeader(canvas, y, isContinued = true)
                y += 40f
            }

            // Zebra Striping Background
            if (index % 2 == 1) {
                canvas.drawRect(40f, y, 555f, y + rowHeight, paintZebraRowBg)
            }

            // Bottom Border Line
            canvas.drawLine(40f, y + rowHeight, 555f, y + rowHeight, paintRowLine)

            // Col 1: Date & Time (dd/MM/yyyy HH:mm)
            val dateTimeStr = tx.date.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
            canvas.drawText(dateTimeStr, 46f, y + 16f, paintDate)

            // Col 2: Category & Note (2 lines in 1 cell)
            val catName = if (tx.categoryId == "debt_payment") "Trả nợ & Tín dụng" else tx.categoryId?.let { categoryMap[it]?.name } ?: "Khác"
            val truncatedCatName = smartEllipsize(paintCatName, catName, 202f)

            if (tx.note.isNotBlank()) {
                // Line 1: Category Name
                canvas.drawText(truncatedCatName, 142f, y + 11.5f, paintCatName)
                // Line 2: Note Snippet
                val truncatedNote = smartEllipsize(paintCatNote, tx.note.trim(), 202f)
                canvas.drawText(truncatedNote, 142f, y + 21.5f, paintCatNote)
            } else {
                // Single vertically centered line
                canvas.drawText(truncatedCatName, 142f, y + 16f, paintCatName)
            }

            // Col 3: Wallet (Smart Ellipsize max 88pt)
            val rawWalletName = walletMap[tx.walletId]?.name ?: "Ví không xác định"
            val truncatedWalletName = smartEllipsize(paintWallet, rawWalletName, 88f)
            canvas.drawText(truncatedWalletName, 355f, y + 16f, paintWallet)

            // Col 4: Amount (Align Right at 547f)
            val (amountPaint, prefix) = when (tx.type) {
                TransactionType.INCOME, TransactionType.TRANSFER_IN -> Pair(paintAmountIncome, "+")
                TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> Pair(paintAmountExpense, "-")
            }
            val amountFormatted = "${prefix}${formatVndAmount(tx.amount.value)}"
            canvas.drawText(amountFormatted, 547f, y + 16f, amountPaint)

            y += rowHeight
        }

        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Smart text ellipsize based on Paint text width measurement.
     * Prevents text truncation issues by adding an ellipsis '…' when text exceeds maxWidth.
     */
    private fun smartEllipsize(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        val ellipsisWidth = paint.measureText(ellipsis)
        if (maxWidth <= ellipsisWidth) return ellipsis

        val availableWidth = maxWidth - ellipsisWidth
        val count = paint.breakText(text, true, availableWidth, null)
        return text.substring(0, count).trimEnd() + ellipsis
    }

    /**
     * Safe Color Hex parser with fallback.
     */
    private fun parseColorHex(hex: String?, fallbackColor: Int): Int {
        if (hex.isNullOrBlank()) return fallbackColor
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color.parseColor(cleanHex)
        } catch (_: Exception) {
            fallbackColor
        }
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
