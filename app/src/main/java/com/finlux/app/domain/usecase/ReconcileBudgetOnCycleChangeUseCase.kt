package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.BudgetProrationCalculator
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinancialPeriod
import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.isLivingExpense
import com.finlux.app.domain.repository.BudgetRepository
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * UseCase tái thiết lập và đồng bộ hóa ngân sách khi chu kỳ lương thay đổi (Reconcile Budget on Cycle Change).
 *
 * Nghiệp vụ cốt lõi:
 * 1. Kế thừa ý chí định mức ([Budget.limitAmount] theo từng danh mục) từ kỳ nguồn sang kỳ mới.
 *    Hỗ trợ tùy chọn phân bổ tỷ lệ hạn mức ([BudgetProrationCalculator]) khi kỳ chuyển tiếp bị co ngắn/kéo dài.
 * 2. Quét toàn bộ giao dịch chi tiêu thực tế trong khoảng `[newPeriod.start, newPeriod.endExclusive)`
 *    và tính toán chính xác tổng chi tiêu [Budget.spentAmount] (chỉ tính [isLivingExpense]).
 * 3. Đồng bộ cờ cảnh báo [Budget.notified80], [Budget.notified100] theo [spentAmount] thực tế
 *    để tránh tình trạng spam hoặc bỏ sót thông báo khi kỳ chu kỳ dịch chuyển.
 * 4. Ghi cập nhật nguyên tử lên Firestore qua [BudgetRepository.upsertBudgets].
 */
class ReconcileBudgetOnCycleChangeUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository? = null,
) {
    suspend operator fun invoke(
        newPeriod: FinancialPeriod,
        sourcePeriod: FinancialPeriod? = null,
        applyProration: Boolean = false,
        standardDays: Long = BudgetProrationCalculator.DEFAULT_STANDARD_CYCLE_DAYS,
    ): AppResult<List<Budget>> {
        // 1. Thu thập ngân sách đã có ở kỳ đích và kỳ nguồn để kế thừa định mức
        val existingTargetBudgets = budgetRepository.observeBudgets(newPeriod.key).firstOrNull().orEmpty()
        val sourceBudgets = if (sourcePeriod != null) {
            budgetRepository.observeBudgets(sourcePeriod.key).firstOrNull().orEmpty()
        } else {
            emptyList()
        }

        val targetLimits = mutableMapOf<String, Money>()

        // Kế thừa từ kỳ nguồn (có thể áp dụng proration)
        for (src in sourceBudgets) {
            val limit = if (applyProration) {
                BudgetProrationCalculator.calculateProratedLimitForPeriod(
                    standardLimit = src.limitAmount,
                    period = newPeriod,
                    standardDays = standardDays,
                )
            } else {
                src.limitAmount
            }
            targetLimits[src.categoryId] = limit
        }

        // Ưu tiên ngân sách đã tồn tại ở kỳ đích nếu người dùng đã tự cấu hình hạn mức > 0
        for (existing in existingTargetBudgets) {
            if (existing.limitAmount.value > 0L) {
                targetLimits[existing.categoryId] = existing.limitAmount
            }
        }

        if (targetLimits.isEmpty()) {
            return AppResult.Success(emptyList())
        }

        // 2. Quét toàn bộ giao dịch thực tế trong phạm vi của newPeriod
        val periodTransactions = transactionRepository.observePeriod(
            start = newPeriod.start,
            endExclusive = newPeriod.endExclusive,
        ).firstOrNull().orEmpty()

        // Lấy danh mục để hỗ trợ map legacy category name -> id nếu có
        val categories = categoryRepository?.observeCategories()?.firstOrNull().orEmpty()
        val nameToId = categories.associate { it.name.lowercase().trim() to it.id }

        // Nhóm và tính tổng chi tiêu sinh hoạt (isLivingExpense)
        val spentByCategoryId = mutableMapOf<String, Long>()
        val livingExpenses = periodTransactions.filter { it.isLivingExpense() }

        for (tx in livingExpenses) {
            val rawCat = tx.categoryId?.trim() ?: continue
            val resolvedId = nameToId[rawCat.lowercase()] ?: rawCat
            spentByCategoryId[resolvedId] = (spentByCategoryId[resolvedId] ?: 0L) + tx.amount.value
        }

        // 3. Khởi tạo danh sách Budget tái thiết lập với cờ cảnh báo chuẩn xác
        val reconciledBudgets = targetLimits.map { (categoryId, limitAmount) ->
            val spent = spentByCategoryId[categoryId] ?: 0L
            val limit = limitAmount.value
            val reached100 = limit > 0L && spent >= limit
            val reached80 = limit > 0L && spent >= (limit * 80L) / 100L

            Budget(
                id = "${categoryId}_${newPeriod.key}",
                categoryId = categoryId,
                periodKey = newPeriod.key,
                periodStart = newPeriod.start,
                periodEndExclusive = newPeriod.endExclusive,
                periodBasis = newPeriod.basis.name,
                limitAmount = limitAmount,
                spentAmount = Money(spent),
                notified80 = reached80,
                notified100 = reached100,
            )
        }

        // 4. Lưu nguyên tử danh sách ngân sách đã đối soát
        return when (val saveResult = budgetRepository.upsertBudgets(reconciledBudgets)) {
            is AppResult.Success -> AppResult.Success(reconciledBudgets)
            is AppResult.Error -> AppResult.Error(saveResult.message)
        }
    }
}
