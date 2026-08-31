package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
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

    @BeforeEach
    fun setUp() {
        fakeRepository = FakeDealRepository()
        saveDealUseCase = SaveDealUseCase(fakeRepository)
        deleteDealUseCase = DeleteDealUseCase(fakeRepository)
        recordDealOutlayUseCase = RecordDealOutlayUseCase(fakeRepository)
        recordDealInflowUseCase = RecordDealInflowUseCase(fakeRepository)
        closeDealWithLossUseCase = CloseDealWithLossUseCase(fakeRepository)
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
            dealId = "deal-1",
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
            dealId = "deal-1",
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
            dealId = "deal-1",
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

        val result = closeDealWithLossUseCase(dealId = "deal-1", note = "Chốt lỗ đóng deal")

        assertTrue(result is AppResult.Success)
        val updated = fakeRepository.deals.value.first()
        assertEquals(-20_000_000L, updated.netProfitLoss.value)
        assertEquals(DealStatus.COMPLETED, updated.status)

        assertEquals(1, fakeRepository.recordedTransactions.size)
        val lossTx = fakeRepository.recordedTransactions.first()
        assertEquals(DealFlowType.CAPITAL_LOSS, lossTx.dealFlowType)
        assertEquals(20_000_000L, lossTx.amount.value)
        assertEquals(TransactionType.EXPENSE, lossTx.type)
    }

    @Test
    fun `roi percentage calculated correctly for profit, loss, and breakeven`() {
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

        val zeroOutlayDeal = FinancialDeal(
            title = "Zero Deal",
            totalCapitalOutlay = Money(0L),
        )
        assertEquals(0.0, zeroOutlayDeal.roiPercentage, 0.001)
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

    override suspend fun recordDealOutlay(
        dealId: String,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val updated = deal.copy(
            totalCapitalOutlay = Money(deal.totalCapitalOutlay.value + amount),
            status = DealStatus.ACTIVE,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }
        recordedTransactions.add(
            FinanceTransaction(
                id = "tx-outlay",
                type = TransactionType.EXPENSE,
                amount = Money(amount),
                categoryId = null,
                walletId = walletId,
                dealId = dealId,
                dealFlowType = DealFlowType.OUTLAY_CAPITAL,
                note = note,
                date = date,
            )
        )
        return AppResult.Success(Unit)
    }

    override suspend fun recordDealInflow(
        dealId: String,
        walletId: String,
        amount: Long,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val totalOutlay = deal.totalCapitalOutlay.value
        val totalRecovered = deal.totalRecovered.value
        val currentProfit = deal.netProfitLoss.value
        val remainingCapital = (totalOutlay - totalRecovered).coerceAtLeast(0L)

        if (amount <= remainingCapital) {
            val newRecovered = totalRecovered + amount
            val updated = deal.copy(
                totalRecovered = Money(newRecovered),
                status = if (newRecovered >= totalOutlay && totalOutlay > 0) DealStatus.COMPLETED else DealStatus.ACTIVE,
            )
            deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }
            recordedTransactions.add(
                FinanceTransaction(
                    id = "tx-recovery",
                    type = TransactionType.INCOME,
                    amount = Money(amount),
                    categoryId = null,
                    walletId = walletId,
                    dealId = dealId,
                    dealFlowType = DealFlowType.PRINCIPAL_RECOVERY,
                    note = note,
                    date = date,
                )
            )
        } else {
            val principalPortion = remainingCapital
            val gainPortion = amount - remainingCapital
            val updated = deal.copy(
                totalRecovered = Money(totalOutlay),
                netProfitLoss = Money(currentProfit + gainPortion),
                status = DealStatus.COMPLETED,
            )
            deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }

            if (principalPortion > 0) {
                recordedTransactions.add(
                    FinanceTransaction(
                        id = "tx-recovery",
                        type = TransactionType.INCOME,
                        amount = Money(principalPortion),
                        categoryId = null,
                        walletId = walletId,
                        dealId = dealId,
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
                    dealId = dealId,
                    dealFlowType = DealFlowType.CAPITAL_GAIN,
                    note = note,
                    date = date,
                )
            )
        }
        return AppResult.Success(Unit)
    }

    override suspend fun closeDealWithLoss(
        dealId: String,
        date: Instant,
        note: String,
    ): AppResult<Unit> {
        val deal = deals.value.find { it.id == dealId } ?: return AppResult.Error("Not found")
        val totalOutlay = deal.totalCapitalOutlay.value
        val totalRecovered = deal.totalRecovered.value
        val lossAmount = (totalOutlay - totalRecovered).coerceAtLeast(0L)

        val updated = deal.copy(
            netProfitLoss = Money(deal.netProfitLoss.value - lossAmount),
            status = DealStatus.COMPLETED,
            endDate = date,
        )
        deals.value = listOf(updated) + deals.value.filterNot { it.id == dealId }

        if (lossAmount > 0) {
            recordedTransactions.add(
                FinanceTransaction(
                    id = "tx-loss",
                    type = TransactionType.EXPENSE,
                    amount = Money(lossAmount),
                    categoryId = null,
                    walletId = "DEAL_SETTLEMENT",
                    dealId = dealId,
                    dealFlowType = DealFlowType.CAPITAL_LOSS,
                    note = note,
                    date = date,
                )
            )
        }
        return AppResult.Success(Unit)
    }
}
