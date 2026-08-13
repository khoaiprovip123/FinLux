package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import javax.inject.Inject

class SaveReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminder: Reminder): AppResult<String> {
        if (reminder.title.isBlank()) return AppResult.Error("Vui lòng nhập tên nhắc nhở")
        if (reminder.amount.value <= 0L) return AppResult.Error("Số tiền phải lớn hơn 0")
        if (reminder.categoryId.isBlank() || reminder.walletId.isBlank()) return AppResult.Error("Vui lòng chọn danh mục và ví")
        val result = repository.upsertReminder(reminder)
        if (result is AppResult.Success) {
            val stored = reminder.copy(id = result.value)
            if (stored.enabled) scheduler.schedule(stored) else scheduler.cancel(stored.id)
        }
        return result
    }
}
