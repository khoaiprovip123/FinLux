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
    override fun observeGoals(): Flow<List<FinancialGoal>> = flowOf(emptyList())
    override suspend fun upsertGoal(goal: FinancialGoal): AppResult<String> {
        upsertCalls++
        lastGoal = goal
        return AppResult.Success("goal-1")
    }
    override suspend fun deleteGoal(goal: FinancialGoal): AppResult<Unit> = AppResult.Success(Unit)
}
