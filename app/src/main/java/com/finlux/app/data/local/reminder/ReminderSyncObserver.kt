package com.finlux.app.data.local.reminder

import android.util.Log
import com.finlux.app.domain.model.Reminder
import com.finlux.app.domain.model.ReminderUtils
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tự động lắng nghe danh sách nhắc nhở từ Firestore khi ứng dụng chạy hoặc người dùng đăng nhập,
 * đồng bộ hóa toàn bộ lịch báo thức vào AlarmManager cục bộ trên thiết bị (Multi-Device Sync).
 */
@Singleton
class ReminderSyncObserver @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    private var observerJob: Job? = null

    fun startObserving(scope: CoroutineScope) {
        if (observerJob?.isActive == true) return
        observerJob = scope.launch {
            reminderRepository.observeReminders()
                .catch { e ->
                    Log.e(TAG, "Error observing reminders for sync", e)
                }
                .collectLatest { reminders ->
                    syncReminders(reminders)
                }
        }
    }

    fun stopObserving() {
        observerJob?.cancel()
        observerJob = null
    }

    suspend fun syncReminders(reminders: List<Reminder>) {
        val now = Instant.now()
        reminders.forEach { reminder ->
            if (reminder.enabled) {
                var targetReminder = reminder
                if (reminder.nextTriggerDate.isBefore(now)) {
                    val updatedNextTrigger = ReminderUtils.computeNextTriggerDate(
                        startDate = reminder.startDate,
                        recurrence = reminder.recurrence,
                        afterInstant = now,
                    )
                    targetReminder = reminder.copy(nextTriggerDate = updatedNextTrigger)
                    try {
                        reminderRepository.upsertReminder(targetReminder)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to auto-advance expired reminder ${reminder.id}", e)
                    }
                }
                reminderScheduler.schedule(targetReminder)
            } else {
                reminderScheduler.cancel(reminder.id)
            }
        }
    }

    companion object {
        private const val TAG = "ReminderSyncObserver"
    }
}
