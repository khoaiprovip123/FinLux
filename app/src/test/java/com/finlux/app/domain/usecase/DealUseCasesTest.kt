package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.repository.DealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class DealUseCasesTest {

    private lateinit var fakeRepository: FakeDealRepository
    private lateinit var saveDealUseCase: SaveDealUseCase
    private lateinit var deleteDealUseCase: DeleteDealUseCase
    private lateinit var recordDealOutlayUseCase: RecordDealOutlayUseCase
    private lateinit var recordDealInflowUseCase: RecordDealInflowUseCase
    private lateinit var closeDealWithLossUseCase: CloseDealWithLossUseCase
    private lateinit var closeDealUseCase: CloseDealUseCase
    private lateinit var revertDealLossUseCase: RevertDealLossUseCase
    private lateinit var reopenDealUseCase: ReopenDealUseCase

    @BeforeEach
    fun setUp() {
        fakeRepository = FakeDealRepository()
        saveDealUseCase = SaveDealUseCase(fakeRepository)
        deleteDealUseCase = DeleteDealUseCase(fakeRepository)
        recordDealOutlayUseCase = RecordDealOutlayUseCase(fakeRepository)
        recordDealInflowUseCase = RecordDealInflowUseCase(fakeRepository)
        closeDealWithLossUseCase = CloseDealWithLossUseCase(fakeRepository)
        closeDealUseCase = CloseDealUseCase(fakeRepository)
        revertDealLossUseCase = RevertDealLossUseCase(fakeRepository)
        reopenDealUseCase = ReopenDealUseCase(fakeRepository)
    }

    @Test
    fun `record outlay increases totalCapitalOutlay and remainingCapital`() = runTest {
        val deal = FinancialDeal(
            id = "deal-1",
            title = "Deal A",
            totalCapitalOutlay = Money(0),
            totalRecovered = Money(0),
        )
        fakeRepository.deals.value = listOf(deal)

        val result = recordDealOutlayUseCase(
            deal = deal,
            walletId = "wallet-1",
            amount = 100_000_000L,
            note = "Xuất vốn đợt 1",
        )

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(100_000_000L, updated.totalCapitalOutlay.value)
        assertEquals(0L, updated.totalRecovered.value)
        assertEquals(100_000_000L, updated.remainingCapital.value)
        assertEquals(DealStatus.ACTIVE, updated.status)
        assertEquals(1, fakeRepository.recordedTransactions.size)
        assertEquals(DealFlowType.OUTLAY_CAPITAL, fakeRepository.recordedTransactions.first().dealFlowType)
        assertEquals(100_000_000L, fakeRepository.recordedTransactions.first().amount.value)
    }

    @Test
    fun `record inflow less than remaining capital performs 100 percent principal recovery`() = runTest {
        val deal = FinancialDeal(
            id = "deal-1",
            title = "Deal A",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(0L),
        )
        fakeRepository.deals.value = listOf(deal)

        // Thu về 60 triệu (nhỏ hơn 100 triệu vốn còn lại)
        val result = recordDealInflowUseCase(
            deal = deal,
            walletId = "wallet-1",
            amount = 60_000_000L,
            note = "Thu hồi đợt 1",
        )

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(60_000_000L, updated.totalRecovered.value)
        assertEquals(40_000_000L, updated.remainingCapital.value)
        assertEquals(0L, updated.netProfitLoss.value) // Chưa có lãi
        assertEquals(1, fakeRepository.recordedTransactions.size)
        assertEquals(DealFlowType.PRINCIPAL_RECOVERY, fakeRepository.recordedTransactions.first().dealFlowType)
        assertEquals(60_000_000L, fakeRepository.recordedTransactions.first().amount.value)
    }

    @Test
    fun `record inflow greater than remaining capital splits principal recovery and capital gain`() = runTest {
        val deal = FinancialDeal(
            id = "deal-1",
            title = "Deal A",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(60_000_000L), // Đã thu 60tr, vốn còn lại là 40tr
        )
        fakeRepository.deals.value = listOf(deal)

        // Thu về 55 triệu (vượt 40 triệu vốn còn lại -> 40tr hoàn gốc + 15tr lãi ròng)
        val result = recordDealInflowUseCase(
            deal = deal,
            walletId = "wallet-1",
            amount = 55_000_000L,
            note = "Thu hồi tất toán",
        )

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(100_000_000L, updated.totalRecovered.value)
        assertEquals(0L, updated.remainingCapital.value)
        assertEquals(15_000_000L, updated.netProfitLoss.value) // Lãi ròng 15tr
        assertEquals(DealStatus.COMPLETED, updated.status)

        // Kiểm tra phân rã 2 giao dịch
        assertEquals(2, fakeRepository.recordedTransactions.size)
        val recoveryTx = fakeRepository.recordedTransactions.find { it.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY }
        val gainTx = fakeRepository.recordedTransactions.find { it.dealFlowType == DealFlowType.CAPITAL_GAIN }

        assertEquals(40_000_000L, recoveryTx?.amount?.value)
        assertEquals(15_000_000L, gainTx?.amount?.value)
    }

    @Test
    fun `close deal with loss records capital loss and completes deal`() = runTest {
        val deal = FinancialDeal(
            id = "deal-1",
            title = "Deal A",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(80_000_000L), // Chỉ thu về 80tr, thiếu 20tr vốn
            netProfitLoss = Money(0L),
            status = DealStatus.ACTIVE,
        )
        fakeRepository.deals.value = listOf(deal)

        val result = closeDealWithLossUseCase(deal = deal, note = "Chốt lỗ đóng deal")

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(-20_000_000L, updated.netProfitLoss.value)
        assertEquals(20_000_000L, updated.writtenOffCapital.value)
        assertEquals(0L, updated.remainingCapital.value)
        assertEquals(DealStatus.COMPLETED, updated.status)

        assertEquals(1, fakeRepository.recordedTransactions.size)
        val lossTx = fakeRepository.recordedTransactions.first()
        assertEquals(DealFlowType.CAPITAL_LOSS, lossTx.dealFlowType)
        assertEquals(20_000_000L, lossTx.amount.value)
        assertEquals(TransactionType.EXPENSE, lossTx.type)
    }

    @Test
    fun `revert deal loss restores netProfitLoss, removes capital loss tx and reopens deal`() = runTest {
        val deal = FinancialDeal(
            id = "deal-1",
            title = "Deal A",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(80_000_000L),
            writtenOffCapital = Money(20_000_000L),
            netProfitLoss = Money(-20_000_000L),
            status = DealStatus.COMPLETED,
            endDate = Instant.now(),
        )
        fakeRepository.deals.value = listOf(deal)
        fakeRepository.recordedTransactions.add(
            FinanceTransaction(
                id = "tx-loss",
                type = TransactionType.EXPENSE,
                amount = Money(20_000_000L),
                categoryId = null,
                walletId = "wal_cash",
                dealId = "deal-1",
                dealFlowType = DealFlowType.CAPITAL_LOSS,
                note = "Chốt lỗ đóng deal",
                date = Instant.now(),
            )
        )

        val result = revertDealLossUseCase(dealId = "deal-1")

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(0L, updated.netProfitLoss.value) // Hoàn lại 20tr lỗ
        assertEquals(0L, updated.writtenOffCapital.value) // Hoàn lại vốn xóa sổ
        assertEquals(20_000_000L, updated.remainingCapital.value) // Dư nợ quay lại 20tr
        assertEquals(DealStatus.ACTIVE, updated.status) // Mở lại deal
        assertEquals(null, updated.endDate)
        assertTrue(fakeRepository.recordedTransactions.none { it.dealFlowType == DealFlowType.CAPITAL_LOSS })
    }

    @Test
    fun `close deal with loss twice across multiple loan outlays prevents duplicate loss and resets remaining capital to zero`() = runTest {
        // Kịch bản thực tế của người dùng:
        // 1. Cho vay 150k
        var deal = FinancialDeal(
            id = "deal-lending-1",
            title = "Khoản vay Tờ Húi",
            category = DealCategory.LENDING,
            totalCapitalOutlay = Money(150_000L),
            totalRecovered = Money(0L),
            writtenOffCapital = Money(0L),
            netProfitLoss = Money(0L),
            status = DealStatus.ACTIVE,
        )
        fakeRepository.deals.value = listOf(deal)

        // 2. Chốt lỗ / Xóa nợ lần 1 (150k)
        val res1 = closeDealWithLossUseCase(deal = deal, note = "Xóa nợ đợt 1")
        assertTrue(res1 is AppResult.Success)

        deal = fakeRepository.deals.value.first { it.id == "deal-lending-1" }
        assertEquals(150_000L, deal.writtenOffCapital.value)
        assertEquals(-150_000L, deal.netProfitLoss.value)
        assertEquals(0L, deal.remainingCapital.value) // Dư nợ về 0 đ sau xóa nợ đợt 1
        assertEquals(DealStatus.COMPLETED, deal.status)

        // 3. Strict State Machine: Khi deal đã COMPLETED, muốn cho vay thêm người dùng mở lại deal
        val reopenRes = reopenDealUseCase(deal.id)
        assertTrue(reopenRes is AppResult.Success)
        deal = fakeRepository.deals.value.first { it.id == "deal-lending-1" }
        assertEquals(DealStatus.ACTIVE, deal.status)

        // Cho vay thêm đợt 2 (200k)
        val resOutlay = recordDealOutlayUseCase(
            deal = deal,
            walletId = "wallet-1",
            amount = 200_000L,
            note = "Cho vay thêm đợt 2",
        )
        assertTrue(resOutlay is AppResult.Success)

        deal = fakeRepository.deals.value.first { it.id == "deal-lending-1" }
        assertEquals(350_000L, deal.totalCapitalOutlay.value)
        assertEquals(150_000L, deal.writtenOffCapital.value)
        assertEquals(-150_000L, deal.netProfitLoss.value)
        // Dư nợ thực tế đợt 2 phải đúng 200k (350k tổng outlay - 150k đã xóa nợ)
        assertEquals(200_000L, deal.remainingCapital.value)
        assertEquals(DealStatus.ACTIVE, deal.status)

        // 4. Chốt lỗ / Xóa nợ lần 2 (chỉ xóa đúng 200k dư nợ mới)
        val res2 = closeDealWithLossUseCase(deal = deal, note = "Xóa nợ đợt 2")
        assertTrue(res2 is AppResult.Success)

        deal = fakeRepository.deals.value.first { it.id == "deal-lending-1" }
        // Kiểm tra triệt để:
        // A. writtenOffCapital == 350k (150k đợt 1 + 200k đợt 2)
        assertEquals(350_000L, deal.writtenOffCapital.value)
        // B. netProfitLoss == -350k (KHÔNG BỊ LỖ KÉP -500k)
        assertEquals(-350_000L, deal.netProfitLoss.value)
        // C. remainingCapital == 0k (DƯ NỢ PHẢI VỀ 0đ)
        assertEquals(0L, deal.remainingCapital.value)
        assertEquals(DealStatus.COMPLETED, deal.status)

        // D. Lịch sử giao dịch chỉ có 2 tx CAPITAL_LOSS tổng cộng là 350k
        val lossTxs = fakeRepository.recordedTransactions.filter { it.dealFlowType == DealFlowType.CAPITAL_LOSS }
        assertEquals(2, lossTxs.size)
        assertEquals(350_000L, lossTxs.sumOf { it.amount.value })
    }

    @Test
    fun `roi percentage calculated correctly for profit, loss, breakeven, and active waiting deal`() {
        val profitDeal = FinancialDeal(
            title = "Profit Deal",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(100_000_000L),
            netProfitLoss = Money(15_000_000L),
            status = DealStatus.COMPLETED,
        )
        assertEquals(15.0, profitDeal.roiPercentage, 0.001)

        val lossDeal = FinancialDeal(
            title = "Loss Deal",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(80_000_000L),
            netProfitLoss = Money(-20_000_000L),
            status = DealStatus.COMPLETED,
        )
        assertEquals(-20.0, lossDeal.roiPercentage, 0.001)

        // Active deal với vốn đang lưu động ngoài thị trường và net profit = 0 không được coi là âm -90%
        val activeWaitingDeal = FinancialDeal(
            title = "Active Waiting Deal",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(10_000_000L),
            netProfitLoss = Money(0L),
            status = DealStatus.ACTIVE,
        )
        assertEquals(0.0, activeWaitingDeal.roiPercentage, 0.001)

        val zeroOutlayDeal = FinancialDeal(
            title = "Zero Deal",
            totalCapitalOutlay = Money(0L),
        )
        assertEquals(0.0, zeroOutlayDeal.roiPercentage, 0.001)
    }

    @Test
    fun `remaining capital correctly deducts writtenOffCapital`() {
        // Mô phỏng deal "Lướt sóng nhỏ/lẻ": Outlay 3.855.900, Recovered 1.355.900, Lỗ chốt deal 2.000.000
        val deal = FinancialDeal(
            title = "Lướt sóng nhỏ/lẻ",
            totalCapitalOutlay = Money(3_855_900L),
            totalRecovered = Money(1_355_900L),
            writtenOffCapital = Money(2_000_000L),
            netProfitLoss = Money(-2_000_000L),
            status = DealStatus.COMPLETED,
        )
        // 3.855.900 - 1.355.900 - 2.000.000 = 500.000
        assertEquals(500_000L, deal.remainingCapital.value)
    }

    @Test
    fun `close deal sets status to COMPLETED and sets endDate`() = runTest {
        val deal = FinancialDeal(
            id = "deal-to-close",
            title = "Deal To Close",
            totalCapitalOutlay = Money(10_000_000L),
            totalRecovered = Money(10_000_000L),
            status = DealStatus.ACTIVE,
        )
        fakeRepository.deals.value = listOf(deal)

        val result = closeDealUseCase("deal-to-close")
        assertTrue(result is AppResult.Success)

        val updated = fakeRepository.deals.value.find { it.id == "deal-to-close" }!!
        assertEquals(DealStatus.COMPLETED, updated.status)
        assertTrue(updated.endDate != null)
    }

    @Test
    fun `strict state machine prevents outlay and inflow when deal is COMPLETED`() = runTest {
        val completedDeal = FinancialDeal(
            id = "deal-completed",
            title = "Deal Completed",
            totalCapitalOutlay = Money(10_000_000L),
            totalRecovered = Money(10_000_000L),
            status = DealStatus.COMPLETED,
        )
        fakeRepository.deals.value = listOf(completedDeal)

        val outlayResult = recordDealOutlayUseCase(completedDeal, "wallet-1", 1_000_000L)
        assertTrue(outlayResult is AppResult.Error)

        val inflowResult = recordDealInflowUseCase(completedDeal, "wallet-1", 1_000_000L)
        assertTrue(inflowResult is AppResult.Error)

        val stopLossResult = closeDealWithLossUseCase(completedDeal)
        assertTrue(stopLossResult is AppResult.Error)
    }

    @Test
    fun `delete deal cascades and removes all associated transactions`() = runTest {
        val deal = FinancialDeal(
            id = "deal-to-delete",
            title = "Deal To Delete",
            totalCapitalOutlay = Money(100_000_000L),
            totalRecovered = Money(50_000_000L),
        )
        fakeRepository.deals.value = listOf(deal)
        fakeRepository.recordedTransactions.add(
            FinanceTransaction(
                id = "tx-deal-1",
                type = TransactionType.EXPENSE,
                amount = Money(100_000_000L),
                categoryId = null,
                walletId = "wallet-1",
                dealId = "deal-to-delete",
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
                date = Instant.now(),
            )
        )
        fakeRepository.recordedTransactions.add(
            FinanceTransaction(
                id = "tx-deal-2",
                type = TransactionType.INCOME,
                amount = Money(50_000_000L),
                categoryId = null,
                walletId = "wallet-1",
                dealId = "deal-to-delete",
                dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
                date = Instant.now(),
            )
        )

        assertEquals(1, fakeRepository.deals.value.size)
        assertEquals(2, fakeRepository.recordedTransactions.size)

        val result = deleteDealUseCase("deal-to-delete")
        assertTrue(result is AppResult.Success)

        assertTrue(fakeRepository.deals.value.isEmpty())
        assertTrue(fakeRepository.recordedTransactions.isEmpty())
    }

    @Test
    fun `lending deal correctly tracks lending principal, recovery and interest gain`() = runTest {
        val lendingDeal = FinancialDeal(
            id = "lend-1",
            title = "Cho bạn Nam mượn",
            category = com.finlux.app.domain.model.DealCategory.LENDING,
            totalCapitalOutlay = Money(50_000_000L),
            totalRecovered = Money(20_000_000L),
            status = DealStatus.ACTIVE,
        )
        fakeRepository.deals.value = listOf(lendingDeal)

        assertEquals(com.finlux.app.domain.model.DealCategory.LENDING, lendingDeal.category)
        assertEquals(30_000_000L, lendingDeal.remainingCapital.value)
        assertEquals(0.4f, lendingDeal.recoveryProgress)

        val result = recordDealInflowUseCase(
            deal = lendingDeal,
            walletId = "wallet-1",
            amount = 35_000_000L,
            note = "Nam trả hết và trả thêm tiền lãi",
        )

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(50_000_000L, updated.totalRecovered.value)
        assertEquals(0L, updated.remainingCapital.value)
        assertEquals(5_000_000L, updated.netProfitLoss.value)
        assertEquals(DealStatus.COMPLETED, updated.status)
        assertTrue(updated.isFullyRecovered)
    }

    @Test
    fun `reopen deal changes status to active and clears endDate`() = runTest {
        val deal = FinancialDeal(
            id = "deal-completed",
            title = "Khoản vay đã xong",
            status = DealStatus.COMPLETED,
            endDate = Instant.now(),
        )
        fakeRepository.deals.value = listOf(deal)

        val result = reopenDealUseCase("deal-completed")

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(DealStatus.ACTIVE, updated.status)
        assertEquals(null, updated.endDate)
    }
}

private class FakeDealRepository : DealRepository {
    val deals = MutableStateFlow<List<FinancialDeal>>(emptyList())
    val recordedTransactions = mutableListOf<FinanceTransaction>()

    override fun observeDeals(): Flow<List<FinancialDeal>> = deals

    override fun observeDeal(dealId: String): Flow<FinancialDeal?> =
        deals.map { list -> list.find { it.id == dealId } }

    override suspend fun upsertDeal(deal: FinancialDeal): AppResult<String> {
        deals.value = listOf(deal) + deals.value.filterNot { it.id == deal.id }
        return AppResult.Success(deal.id)
    }

    override suspend fun deleteDeal(dealId: String): AppResult<Unit> {
        deals.value = deals.value.filterNot { it.id == dealId }
        recordedTransactions.removeAll { it.dealId == dealId }
        return AppResult.Success(Unit)
    }

    override suspend fun reopenDeal(dealId: String): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val updated = deal.copy(
            status = DealStatus.ACTIVE,
            endDate = null,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }
        return AppResult.Success(Unit)
    }

    override suspend fun recordDealOutlay(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        if (deal.status == DealStatus.COMPLETED) {
            return AppResult.Error("Thương vụ đã hoàn tất đóng sổ, không thể xuất thêm vốn")
        }
        val updated = deal.copy(
            totalCapitalOutlay = Money(deal.totalCapitalOutlay.value + amount),
            status = DealStatus.ACTIVE,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == deal.id }
        recordedTransactions.add(
            FinanceTransaction(
                id = "tx-outlay",
                type = TransactionType.EXPENSE,
                amount = Money(amount),
                categoryId = null,
                walletId = walletId,
                dealId = deal.id,
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
                note = note,
                date = date,
            )
        )
        return AppResult.Success(Unit)
    }

    override suspend fun recordDealInflow(
        deal: FinancialDeal,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        if (deal.status == DealStatus.COMPLETED) {
            return AppResult.Error("Thương vụ đã hoàn tất đóng sổ, không thể thu hồi thêm")
        }
        val totalOutlay = deal.totalCapitalOutlay.value
        val totalRecovered = deal.totalRecovered.value
        val currentProfit = deal.netProfitLoss.value
        val currentWrittenOff = deal.writtenOffCapital.value
        val remainingCapital = (totalOutlay - totalRecovered - currentWrittenOff).coerceAtLeast(0L)

        if (amount <= remainingCapital) {
            val newRecovered = totalRecovered + amount
            val updated = deal.copy(
                totalRecovered = Money(newRecovered),
                status = if (newRecovered + currentWrittenOff >= totalOutlay && totalOutlay > 0) DealStatus.COMPLETED else DealStatus.ACTIVE,
            )
            deals.value = listOf(updated) + deals.value.filterNot { it.id == deal.id }
            recordedTransactions.add(
                FinanceTransaction(
                    id = "tx-recovery",
                    type = TransactionType.INCOME,
                    amount = Money(amount),
                    categoryId = null,
                    walletId = walletId,
                    dealId = deal.id,
                    dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
                    note = note,
                    date = date,
                )
            )
        } else {
            val principalPortion = remainingCapital
            val gainPortion = amount - remainingCapital
            val updated = deal.copy(
                totalRecovered = Money(totalRecovered + principalPortion),
                netProfitLoss = Money(currentProfit + gainPortion),
                status = DealStatus.COMPLETED,
            )
            deals.value = listOf(updated) + deals.value.filterNot { it.id == deal.id }

            if (principalPortion > 0) {
                recordedTransactions.add(
                    FinanceTransaction(
                        id = "tx-recovery",
                        type = TransactionType.INCOME,
                        amount = Money(principalPortion),
                        categoryId = null,
                        walletId = walletId,
                        dealId = deal.id,
                        dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
                        note = note,
                        date = date,
                    )
                )
            }
            recordedTransactions.add(
                FinanceTransaction(
                    id = "tx-gain",
                    type = TransactionType.INCOME,
                    amount = Money(gainPortion),
                    categoryId = null,
                    walletId = walletId,
                    dealId = deal.id,
                    dealFlowType = DealFlowType.CAPITAL_GAIN,
                    note = note,
                    date = date,
                )
            )
        }
        return AppResult.Success(Unit)
    }

    override suspend fun closeDealWithLoss(
        deal: FinancialDeal,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        if (deal.status == DealStatus.COMPLETED) {
            return AppResult.Error("Thương vụ đã hoàn tất đóng sổ")
        }
        val totalOutlay = deal.totalCapitalOutlay.value
        val totalRecovered = deal.totalRecovered.value
        val currentProfit = deal.netProfitLoss.value
        val currentWrittenOff = deal.writtenOffCapital.value
        val lossAmount = (totalOutlay - totalRecovered - currentWrittenOff).coerceAtLeast(0L)

        val updated = deal.copy(
            writtenOffCapital = Money(currentWrittenOff + lossAmount),
            netProfitLoss = Money(currentProfit - lossAmount),
            status = DealStatus.COMPLETED,
            endDate = date,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == deal.id }

        if (lossAmount > 0) {
            recordedTransactions.add(
                FinanceTransaction(
                    id = "tx-loss",
                    type = TransactionType.EXPENSE,
                    amount = Money(lossAmount),
                    categoryId = null,
                    walletId = "wal_cash",
                    dealId = deal.id,
                    dealFlowType = DealFlowType.CAPITAL_LOSS,
                    note = note,
                    date = date,
                )
            )
        }
        return AppResult.Success(Unit)
    }

    override suspend fun revertDealLoss(dealId: String): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val lossTxs = recordedTransactions.filter { it.dealId == dealId && it.dealFlowType == DealFlowType.CAPITAL_LOSS }
        val totalLoss = lossTxs.sumOf { it.amount.value }

        recordedTransactions.removeAll { it.dealId == dealId && it.dealFlowType == DealFlowType.CAPITAL_LOSS }
        val newWrittenOff = (deal.writtenOffCapital.value - totalLoss).coerceAtLeast(0L)
        val updated = deal.copy(
            writtenOffCapital = Money(newWrittenOff),
            netProfitLoss = Money(deal.netProfitLoss.value + totalLoss),
            status = DealStatus.ACTIVE,
            endDate = null,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }
        return AppResult.Success(Unit)
    }

    override suspend fun closeDeal(dealId: String, date: Instant): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val updated = deal.copy(
            status = DealStatus.COMPLETED,
            endDate = date,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }
        return AppResult.Success(Unit)
    }
}
