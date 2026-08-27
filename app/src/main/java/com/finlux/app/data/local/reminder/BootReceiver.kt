package com.finlux.app.data.local.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SalaryCycleScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var scheduler: ReminderScheduler

    @Inject
    lateinit var salaryCycleRepository: SalaryCycleRepository

    @Inject
    lateinit var salaryCycleScheduler: SalaryCycleScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = repository.observeReminders().firstOrNull().orEmpty()
                reminders.forEach { reminder ->
                    if (reminder.enabled) {
                        scheduler.schedule(reminder)
                    }
                }
                val salaryConfig = salaryCycleRepository.observeConfig().firstOrNull()
                if (salaryConfig != null && salaryConfig.enabled) {
                    salaryCycleScheduler.scheduleNextPayday(salaryConfig)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
