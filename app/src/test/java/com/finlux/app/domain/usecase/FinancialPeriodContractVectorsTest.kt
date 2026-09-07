package com.finlux.app.domain.usecase

import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.SalaryCycleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

class FinancialPeriodContractVectorsTest {
    private val resolver = DefaultFinancialPeriodResolver(DefaultSalaryCycleCalculator())

    @Test
    fun `Kotlin resolver satisfies shared Android and Functions contract vectors`() {
        loadVectors().forEach { vector ->
            val config = SalaryCycleConfig(
                enabled = vector.enabled,
                budgetPeriodBasis = BudgetPeriodBasis.valueOf(vector.budgetPeriodBasis),
                paydayRuleType = PaydayRuleType.valueOf(vector.paydayRuleType),
                paydayDay = vector.paydayDay,
                financeTimeZone = vector.financeTimeZone,
            )
            val actual = resolver.resolvePeriodContaining(Instant.parse(vector.instant), config)

            assertEquals(vector.expectedKey, actual.key, vector.name)
            assertEquals(Instant.parse(vector.expectedStart), actual.start, vector.name)
            assertEquals(Instant.parse(vector.expectedEndExclusive), actual.endExclusive, vector.name)
            assertEquals(BudgetPeriodBasis.valueOf(vector.budgetPeriodBasis), actual.basis, vector.name)
        }
    }

    private fun loadVectors(): List<ContractVector> {
        val fixture = listOf(
            Path.of("contracts", "financial-period-vectors.tsv"),
            Path.of("..", "contracts", "financial-period-vectors.tsv"),
        ).firstOrNull(Files::exists) ?: error("Không tìm thấy contracts/financial-period-vectors.tsv")
        val lines = Files.readAllLines(fixture).filter(String::isNotBlank)
        return lines.drop(1).map { line ->
            val columns = line.split('\t')
            require(columns.size == 10) { "Contract vector không hợp lệ: $line" }
            ContractVector(
                name = columns[0],
                enabled = columns[1].toBooleanStrict(),
                budgetPeriodBasis = columns[2],
                paydayRuleType = columns[3],
                paydayDay = columns[4].toInt(),
                financeTimeZone = columns[5],
                instant = columns[6],
                expectedKey = columns[7],
                expectedStart = columns[8],
                expectedEndExclusive = columns[9],
            )
        }
    }

    private data class ContractVector(
        val name: String,
        val enabled: Boolean,
        val budgetPeriodBasis: String,
        val paydayRuleType: String,
        val paydayDay: Int,
        val financeTimeZone: String,
        val instant: String,
        val expectedKey: String,
        val expectedStart: String,
        val expectedEndExclusive: String,
    )
}
