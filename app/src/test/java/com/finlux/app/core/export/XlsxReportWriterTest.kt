package com.finlux.app.core.export

import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.DashboardSummary
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.ReportRange
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.util.zip.ZipFile

class XlsxReportWriterTest {

    @Test
    fun `writeReport creates valid 2-sheet xlsx zip archive`(@TempDir tempDir: Path) {
        val outputFile = File(tempDir.toFile(), "test_report.xlsx")

        val cat1 = Category("cat_food", "Ăn uống", CategoryType.EXPENSE, "restaurant", "#EF4444", true, Instant.now())
        val cat2 = Category("cat_salary", "Tiền lương", CategoryType.INCOME, "payments", "#10B981", false, Instant.now())
        val wallet1 = Wallet("w1", "Ví Tiền mặt", WalletType.CASH, Money(10_000_000), "#3B82F6", true, Instant.now())

        val tx1 = FinanceTransaction(
            id = "tx1",
            walletId = "w1",
            amount = Money(250_000),
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            note = "Ăn tối cùng bạn bè",
            date = Instant.now(),
        )
        val tx2 = FinanceTransaction(
            id = "tx2",
            walletId = "w1",
            amount = Money(15_000_000),
            type = TransactionType.INCOME,
            categoryId = "cat_salary",
            note = "Lương tháng này",
            date = Instant.now(),
        )

        val range = ReportRange(
            start = LocalDate.of(2026, 8, 1),
            end = LocalDate.of(2026, 8, 31),
        )
        val summary = DashboardSummary(
            income = Money(15_000_000),
            expense = Money(250_000),
            net = 14_750_000,
        )
        val categoryExpenses = listOf(
            CategoryExpense(category = cat1, amount = 250_000L),
        )

        XlsxReportWriter.writeReport(
            outputFile = outputFile,
            range = range,
            summary = summary,
            expensesByCategory = categoryExpenses,
            transactions = listOf(tx1, tx2),
            categories = listOf(cat1, cat2),
            wallets = listOf(wallet1),
        )

        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)

        // Verify ZIP entries structure for Excel XLSX specification
        val zipFile = ZipFile(outputFile)
        val entryNames = zipFile.entries().asSequence().map { it.name }.toSet()

        assertTrue(entryNames.contains("[Content_Types].xml"))
        assertTrue(entryNames.contains("_rels/.rels"))
        assertTrue(entryNames.contains("xl/workbook.xml"))
        assertTrue(entryNames.contains("xl/_rels/workbook.xml.rels"))
        assertTrue(entryNames.contains("xl/styles.xml"))
        assertTrue(entryNames.contains("xl/worksheets/sheet1.xml"))
        assertTrue(entryNames.contains("xl/worksheets/sheet2.xml"))

        // Read Sheet 1 content to verify transaction data is encoded
        val sheet1Content = zipFile.getInputStream(zipFile.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
        assertTrue(sheet1Content.contains("Ăn tối cùng bạn bè"))
        assertTrue(sheet1Content.contains("250000"))
        assertTrue(sheet1Content.contains("Lương tháng này"))

        // Read Sheet 2 content to verify category summary and KPI data
        val sheet2Content = zipFile.getInputStream(zipFile.getEntry("xl/worksheets/sheet2.xml")).bufferedReader().readText()
        assertTrue(sheet2Content.contains("Ăn uống"))
        assertTrue(sheet2Content.contains("15000000"))
        assertTrue(sheet2Content.contains("14750000"))

        zipFile.close()
    }
}
