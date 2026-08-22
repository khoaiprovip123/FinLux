package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.FinancialGoal
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class GoalUseCasesTest {
    private val repository = RecordingGoalRepository()

    @Test
    fun `save rejects blank name without writing`() = runTest {
        val result = SaveGoalUseCase(repository)(validGoal().copy(name = "  "))
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.upsertCalls)
    }

    @Test
    fun `save rejects zero target without writing`() = runTest {
        val result = SaveGoalUseCase(repository)(validGoal().copy(targetAmount = Money(0)))
        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.upsertCalls)
    }

    @Test
    fun `save normalizes name and delegates valid goal`() = runTest {
        val result = SaveGoalUseCase(repository)(validGoal().copy(name = "  Mua xe  "))
        assertEquals(AppResult.Success("goal-1"), result)
        assertEquals("Mua xe", repository.lastGoal?.name)
    }

    @Test
    fun `deposit rejects invalid goalId or walletId or non-positive amount`() = runTest {
        val useCase = DepositToGoalUseCase(repository)

        val res1 = useCase("", "wallet-1", 1_000_000L)
        assertInstanceOf(AppResult.Error::class.java, res1)

        val res2 = useCase("goal-1", "", 1_000_000L)
        assertInstanceOf(AppResult.Error::class.java, res2)

        val res3 = useCase("goal-1", "wallet-1", 0L)
        assertInstanceOf(AppResult.Error::class.java, res3)

        val res4 = useCase("goal-1", "wallet-1", -500_000L)
        assertInstanceOf(AppResult.Error::class.java, res4)

        assertEquals(0, repository.depositCalls)
    }

    @Test
    fun `deposit delegates to repository when inputs are valid`() = runTest {
        val useCase = DepositToGoalUseCase(repository)
        val result = useCase("goal-1", "wallet-1", 2_000_000L, "Nạp tiền thưởng")

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.depositCalls)
        assertEquals("goal-1", repository.lastDepositGoalId)
        assertEquals("wallet-1", repository.lastDepositWalletId)
        assertEquals(2_000_000L, repository.lastDepositAmount)
        assertEquals("Nạp tiền thưởng", repository.lastDepositNote)
    }

    @Test
    fun `withdraw rejects invalid goalId or walletId or non-positive amount`() = runTest {
        val useCase = WithdrawFromGoalUseCase(repository)

        val res1 = useCase("", "wallet-1", 1_000_000L)
        assertInstanceOf(AppResult.Error::class.java, res1)

        val res2 = useCase("goal-1", "", 1_000_000L)
        assertInstanceOf(AppResult.Error::class.java, res2)

        val res3 = useCase("goal-1", "wallet-1", 0L)
        assertInstanceOf(AppResult.Error::class.java, res3)

        assertEquals(0, repository.withdrawCalls)
    }

    @Test
    fun `withdraw delegates to repository when inputs are valid`() = runTest {
        val useCase = WithdrawFromGoalUseCase(repository)
        val result = useCase("goal-1", "wallet-1", 1_500_000L, "Rút tiền mua sắm")

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, repository.withdrawCalls)
        assertEquals("goal-1", repository.lastWithdrawGoalId)
        assertEquals("wallet-1", repository.lastWithdrawWalletId)
        assertEquals(1_500_000L, repository.lastWithdrawAmount)
        assertEquals("Rút tiền mua sắm", repository.lastWithdrawNote)
    }

    private fun validGoal() = FinancialGoal(
        name = "Quỹ dự phòng",
        targetAmount = Money(50_000_000),
        deadline = Instant.parse("2027-01-01T00:00:00Z"),
        category = "Khác",
        monthlyContribution = Money(5_000_000),
    )
}

private class RecordingGoalRepository : GoalRepository {
    var upsertCalls = 0
    var lastGoal: FinancialGoal? = null
    var depositCalls = 0
    var lastDepositGoalId: String? = null
    var lastDepositWalletId: String? = null
    var lastDepositAmount: Long? = null
    var lastDepositNote: String? = null

    var withdrawCalls = 0
    var lastWithdrawGoalId: String? = null
    var lastWithdrawWalletId: String? = null
    var lastWithdrawAmount: Long? = null
    var lastWithdrawNote: String? = null

    override fun observeGoals(): Flow<List<FinancialGoal>> = flowOf(emptyList())

    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> {
        upsertCalls++
        lastGoal = goal
        return AppResult.Success("goal-1")
    }

    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun depositToGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> {
        depositCalls++
        lastDepositGoalId = goalId
        lastDepositWalletId = walletId
        lastDepositAmount = amount
        lastDepositNote = note
        return AppResult.Success(Unit)
    }

    override suspend fun withdrawFromGoal(
        goalId: String,
        walletId: String,
        amount: Long,
        note: String,
        date: Instant,
    ): AppResult<Unit> {
        withdrawCalls++
        lastWithdrawGoalId = goalId
        lastWithdrawWalletId = walletId
        lastWithdrawAmount = amount
        lastWithdrawNote = note
        return AppResult.Success(Unit)
    }
}
