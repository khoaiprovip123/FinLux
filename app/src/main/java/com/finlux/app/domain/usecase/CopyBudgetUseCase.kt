package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CopyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val saveBudget: SaveBudgetUseCase,
) {
    /**
     * Sao chép toàn bộ danh sách ngân sách từ [sourcePeriod] sang [targetPeriod].
     *
     * @param sourcePeriod Kỳ nguồn có sẵn ngân sách
     * @param targetPeriod Kỳ đích cần sao chép sang
     * @param overwriteExisting Nếu true, sẽ ghi đè hạn mức ngân sách đã tồn tại ở kỳ đích
     * @return [AppResult.Success] với số lượng ngân sách đã được sao chép thành công
     */
    suspend operator fun invoke(
        sourcePeriod: FinancialPeriod,
        targetPeriod: FinancialPeriod,
        overwriteExisting: Boolean = false,
    ): AppResult<Int> {
        val sourceBudgets = budgetRepository.observeBudgets(sourcePeriod.key).firstOrNull().orEmpty()
        if (sourceBudgets.isEmpty()) {
            return AppResult.Error("Kỳ ${sourcePeriod.displayLabel} chưa có ngân sách nào để sao chép")
        }

        val targetBudgets = budgetRepository.observeBudgets(targetPeriod.key).firstOrNull().orEmpty()
        val targetCategoryIds = targetBudgets.map { it.categoryId }.toSet()

        var copiedCount = 0
        for (src in sourceBudgets) {
            if (!overwriteExisting && targetCategoryIds.contains(src.categoryId)) {
                continue
            }

            val newBudget = Budget(
                id = "${src.categoryId}_${targetPeriod.key}",
                categoryId = src.categoryId,
                periodKey = targetPeriod.key,
                periodStart = targetPeriod.start,
                periodEndExclusive = targetPeriod.endExclusive,
                periodBasis = targetPeriod.basis.name,
                limitAmount = src.limitAmount,
                spentAmount = Money(0L),
                notified80 = false,
                notified100 = false,
            )

            when (val res = saveBudget(newBudget)) {
                is AppResult.Success -> copiedCount++
                is AppResult.Error -> {
                    // Tiếp tục sao chép các ngân sách còn lại nếu một cái gặp lỗi
                }
            }
        }

        return AppResult.Success(copiedCount)
    }
}
