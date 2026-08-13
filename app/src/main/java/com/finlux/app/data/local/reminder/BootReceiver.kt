package com.finlux.app.data.local.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.finlux.app.domain.repository.ReminderRepository
import com.finlux.app.domain.repository.ReminderScheduler
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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
