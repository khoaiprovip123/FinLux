package com.finlux.app.core.export

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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * High-performance, lightweight OpenXML (.xlsx) Workbook writer for FinLux reports.
 * Produces genuine 2-Sheet Excel files without bulky third-party dependencies.
 */
object XlsxReportWriter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun writeReport(
        outputFile: File,
        range: ReportRange,
        summary: DashboardSummary,
        expensesByCategory: List<CategoryExpense>,
        transactions: List<FinanceTransaction>,
        categories: List<Category>,
        wallets: List<Wallet>,
    ) {
        val categoryMap = categories.associateBy(Category::id)
        val walletMap = wallets.associateBy(Wallet::id)

        val sheet1Xml = buildTransactionDetailSheet(
            range = range,
            transactions = transactions,
            categoryMap = categoryMap,
            walletMap = walletMap,
        )

        val sheet2Xml = buildSummaryCategorySheet(
            range = range,
            summary = summary,
            expensesByCategory = expensesByCategory,
        )

        ZipOutputStream(FileOutputStream(outputFile)).use { zip ->
            // 1. [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(CONTENT_TYPES_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(ROOT_RELS_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 3. xl/_rels/workbook.xml.rels
            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(WORKBOOK_RELS_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 4. xl/workbook.xml
            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(WORKBOOK_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 5. xl/styles.xml
            zip.putNextEntry(ZipEntry("xl/styles.xml"))
            zip.write(STYLES_XML.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 6. xl/worksheets/sheet1.xml (Chi tiết giao dịch)
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(sheet1Xml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 7. xl/worksheets/sheet2.xml (Tổng hợp danh mục)
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
            zip.write(sheet2Xml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    private fun buildTransactionDetailSheet(
        range: ReportRange,
        transactions: List<FinanceTransaction>,
        categoryMap: Map<String, Category>,
        walletMap: Map<String, Wallet>,
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
        sb.append("  <sheetData>\n")

        var rowIndex = 1

        // Title row
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>BÁO CÁO CHI TIẾT GIAO DỊCH FINLUX</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        // Subtitle / Date range
        val rangeText = "Thời gian: Từ ${range.start.format(dateFormatter)} đến ${range.end.format(dateFormatter)} | Ngày xuất: ${LocalDate.now().format(dateFormatter)}"
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(rangeText)}</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex += 2

        // Table Header
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>STT</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Thời gian</t></is></c>\n")
        sb.append("      <c r=\"C${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Loại giao dịch</t></is></c>\n")
        sb.append("      <c r=\"D${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Danh mục</t></is></c>\n")
        sb.append("      <c r=\"E${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Ví thanh toán</t></is></c>\n")
        sb.append("      <c r=\"F${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Số tiền (VNĐ)</t></is></c>\n")
        sb.append("      <c r=\"G${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Ghi chú</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        // Data Rows
        transactions.forEachIndexed { index, tx ->
            val dateStr = tx.date.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
            val typeStr = when (tx.type) {
                TransactionType.INCOME -> "Thu nhập"
                TransactionType.EXPENSE -> if (tx.categoryId == "debt_payment") "Trả nợ" else "Chi tiêu"
                TransactionType.TRANSFER_OUT -> "Chuyển tiền đi"
                TransactionType.TRANSFER_IN -> "Nhận chuyển tiền"
            }
            val catName = tx.categoryId?.let { categoryMap[it]?.name } ?: "Không có danh mục"
            val walletName = walletMap[tx.walletId]?.name ?: "Ví không xác định"
            val note = tx.note

            sb.append("    <row r=\"${rowIndex}\">\n")
            sb.append("      <c r=\"A${rowIndex}\"><v>${index + 1}</v></c>\n")
            sb.append("      <c r=\"B${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(dateStr)}</t></is></c>\n")
            sb.append("      <c r=\"C${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(typeStr)}</t></is></c>\n")
            sb.append("      <c r=\"D${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(catName)}</t></is></c>\n")
            sb.append("      <c r=\"E${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(walletName)}</t></is></c>\n")
            sb.append("      <c r=\"F${rowIndex}\"><v>${tx.amount.value}</v></c>\n")
            sb.append("      <c r=\"G${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(note)}</t></is></c>\n")
            sb.append("    </row>\n")
            rowIndex++
        }

        sb.append("  </sheetData>\n")
        sb.append("</worksheet>")
        return sb.toString()
    }

    private fun buildSummaryCategorySheet(
        range: ReportRange,
        summary: DashboardSummary,
        expensesByCategory: List<CategoryExpense>,
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
        sb.append("  <sheetData>\n")

        var rowIndex = 1

        // Title
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>TỔNG HỢP DÒNG TIỀN &amp; PHÂN BỔ DANH MỤC</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        val rangeText = "Thời gian: Từ ${range.start.format(dateFormatter)} đến ${range.end.format(dateFormatter)}"
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(rangeText)}</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex += 2

        // Section 1: KPI Overview
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>1. TỔNG QUAN DÒNG TIỀN (KPI)</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Chỉ số</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Số tiền (VNĐ)</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\"><is><t>Tổng thu nhập</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\"><v>${summary.income.value}</v></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\"><is><t>Tổng chi tiêu</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\"><v>${summary.expense.value}</v></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\"><is><t>Thu ròng (Dư / Thâm hụt)</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\"><v>${summary.net}</v></c>\n")
        sb.append("    </row>\n")
        rowIndex += 2

        // Section 2: Category Breakdown
        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>2. PHÂN BỔ THEO DANH MỤC CHI TIÊU</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        sb.append("    <row r=\"${rowIndex}\">\n")
        sb.append("      <c r=\"A${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>STT</t></is></c>\n")
        sb.append("      <c r=\"B${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Danh mục</t></is></c>\n")
        sb.append("      <c r=\"C${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Số tiền (VNĐ)</t></is></c>\n")
        sb.append("      <c r=\"D${rowIndex}\" t=\"inlineStr\" s=\"1\"><is><t>Tỷ trọng (%)</t></is></c>\n")
        sb.append("    </row>\n")
        rowIndex++

        val totalExpense = summary.expense.value
        expensesByCategory.forEachIndexed { index, item ->
            val catName = item.category?.name ?: "Khác"
            val ratio = if (totalExpense > 0) (item.amount * 100.0 / totalExpense) else 0.0
            val ratioStr = String.format("%.2f%%", ratio)

            sb.append("    <row r=\"${rowIndex}\">\n")
            sb.append("      <c r=\"A${rowIndex}\"><v>${index + 1}</v></c>\n")
            sb.append("      <c r=\"B${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(catName)}</t></is></c>\n")
            sb.append("      <c r=\"C${rowIndex}\"><v>${item.amount}</v></c>\n")
            sb.append("      <c r=\"D${rowIndex}\" t=\"inlineStr\"><is><t>${escapeXml(ratioStr)}</t></is></c>\n")
            sb.append("    </row>\n")
            rowIndex++
        }

        sb.append("  </sheetData>\n")
        sb.append("</worksheet>")
        return sb.toString()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private const val CONTENT_TYPES_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
        "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
        "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
        "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n" +
        "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
        "  <Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
        "  <Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>\n" +
        "</Types>"

    private const val ROOT_RELS_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
        "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n" +
        "</Relationships>"

    private const val WORKBOOK_RELS_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
        "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n" +
        "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>\n" +
        "  <Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n" +
        "</Relationships>"

    private const val WORKBOOK_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n" +
        "  <sheets>\n" +
        "    <sheet name=\"Chi tiết giao dịch\" sheetId=\"1\" r:id=\"rId1\"/>\n" +
        "    <sheet name=\"Tổng hợp danh mục\" sheetId=\"2\" r:id=\"rId2\"/>\n" +
        "  </sheets>\n" +
        "</workbook>"

    private const val STYLES_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n" +
        "  <fonts count=\"2\">\n" +
        "    <font><sz val=\"11\"/><name val=\"Calibri\"/></font>\n" +
        "    <font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>\n" +
        "  </fonts>\n" +
        "  <fills count=\"2\">\n" +
        "    <fill><patternFill patternType=\"none\"/></fill>\n" +
        "    <fill><patternFill patternType=\"gray125\"/></fill>\n" +
        "  </fills>\n" +
        "  <borders count=\"1\">\n" +
        "    <border><left/><right/><top/><bottom/><diagonal/></border>\n" +
        "  </borders>\n" +
        "  <cellStyleXfs count=\"1\">\n" +
        "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>\n" +
        "  </cellStyleXfs>\n" +
        "  <cellXfs count=\"2\">\n" +
        "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n" +
        "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>\n" +
        "  </cellXfs>\n" +
        "</styleSheet>"
}
