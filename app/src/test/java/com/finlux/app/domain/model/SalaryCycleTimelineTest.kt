package com.finlux.app.domain.model

import com.finlux.app.data.demo.DemoSalaryCycleRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SalaryCycleTimelineTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    @Test
    fun `isEffectiveAt evaluates date ranges correctly`() {
        val record = SalaryCycleConfigRecord(
            id = "rec1",
            effectiveFromDate = "2026-08-01",
            effectiveToDate = "2026-09-01",
            config = SalaryCycleConfig(paydayDay = 25),
        )

        // Before start -> false
        assertFalse(record.isEffectiveAt(LocalDate.of(2026, 7, 31)))
        // Exactly start -> true
        assertTrue(record.isEffectiveAt(LocalDate.of(2026, 8, 1)))
        // In the middle -> true
        assertTrue(record.isEffectiveAt(LocalDate.of(2026, 8, 15)))
        // Exactly end (exclusive) -> false
        assertFalse(record.isEffectiveAt(LocalDate.of(2026, 9, 1)))
        // After end -> false
        assertFalse(record.isEffectiveAt(LocalDate.of(2026, 9, 10)))
    }

    @Test
    fun `isEffectiveAt with null effectiveToDate remains effective indefinitely`() {
        val activeRecord = SalaryCycleConfigRecord(
            id = "rec_active",
            effectiveFromDate = "2026-09-01",
            effectiveToDate = null,
            config = SalaryCycleConfig(paydayDay = 10),
        )

        assertFalse(activeRecord.isEffectiveAt(LocalDate.of(2026, 8, 31)))
        assertTrue(activeRecord.isEffectiveAt(LocalDate.of(2026, 9, 1)))
        assertTrue(activeRecord.isEffectiveAt(LocalDate.of(2026, 12, 31)))
        assertTrue(activeRecord.isEffectiveAt(LocalDate.of(2030, 1, 1)))
    }

    @Test
    fun `isEffectiveAt works with Instant accurately across timezones`() {
        val record = SalaryCycleConfigRecord(
            id = "rec1",
            effectiveFromDate = "2026-09-10",
            effectiveToDate = null,
            config = SalaryCycleConfig(financeTimeZone = "Asia/Ho_Chi_Minh"),
        )

        val instantBefore = LocalDate.of(2026, 9, 9).atStartOfDay(zone).toInstant()
        val instantStart = LocalDate.of(2026, 9, 10).atStartOfDay(zone).toInstant()

        assertFalse(record.isEffectiveAt(instantBefore, zone))
        assertTrue(record.isEffectiveAt(instantStart, zone))
    }

    @Test
    fun `DemoSalaryCycleRepository manages timeline records and resolves getConfigAt accurately`() = runTest {
        val repo = DemoSalaryCycleRepository()

        // 1. Fallback khi chưa có timeline
        val initialConfig = repo.getConfigAt(LocalDate.of(2026, 5, 1).atStartOfDay(zone).toInstant())
        assertEquals(1, initialConfig.paydayDay) // Default fallback

        // 2. Lưu bản ghi lịch sử tháng 6 - tháng 8 (nhận lương ngày 25)
        val historyRecord = SalaryCycleConfigRecord(
            id = "rec_2026-06",
            effectiveFromDate = "2026-06-01",
            effectiveToDate = "2026-08-25",
            config = SalaryCycleConfig(
                enabled = true,
                paydayDay = 25,
                salaryWalletId = "wallet-old",
            ),
        )
        repo.saveConfigRecord(historyRecord)

        // 3. Lưu bản ghi hiện hành từ 2026-08-25 trở đi (nhận lương ngày 10)
        val currentRecord = SalaryCycleConfigRecord(
            id = "rec_2026-08",
            effectiveFromDate = "2026-08-25",
            effectiveToDate = null,
            config = SalaryCycleConfig(
                enabled = true,
                paydayDay = 10,
                salaryWalletId = "wallet-new",
            ),
        )
        repo.saveConfigRecord(currentRecord)

        // 4. Kiểm tra truy vấn theo mốc thời gian quá khứ
        val pastInstant = LocalDate.of(2026, 7, 10).atStartOfDay(zone).toInstant()
        val pastConfig = repo.getConfigAt(pastInstant)
        assertEquals(25, pastConfig.paydayDay)
        assertEquals("wallet-old", pastConfig.salaryWalletId)

        // 5. Kiểm tra truy vấn theo mốc thời gian hiện tại
        val currentInstant = LocalDate.of(2026, 9, 12).atStartOfDay(zone).toInstant()
        val resolvedCurrentConfig = repo.getConfigAt(currentInstant)
        assertEquals(10, resolvedCurrentConfig.paydayDay)
        assertEquals("wallet-new", resolvedCurrentConfig.salaryWalletId)

        // 6. Kiểm tra tính tương thích ngược của observeConfig()
        val activeFromFlow = repo.observeConfig().first()
        assertEquals(10, activeFromFlow.paydayDay)
        assertEquals("wallet-new", activeFromFlow.salaryWalletId)

        // 7. Kiểm tra observeTimeline() sắp xếp mới nhất lên đầu
        val timeline = repo.observeTimeline().first()
        assertEquals(2, timeline.size)
        assertEquals("2026-08-25", timeline[0].effectiveFromDate)
        assertEquals("2026-06-01", timeline[1].effectiveFromDate)
    }
}
