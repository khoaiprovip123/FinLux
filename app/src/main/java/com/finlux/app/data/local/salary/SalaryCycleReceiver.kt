package com.finlux.app.data.local.salary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.finlux.app.core.common.AppResult
import com.finlux.app.core.time.FinanceClock
import com.finlux.app.data.local.notification.SystemNotificationHelper
import com.finlux.app.domain.model.AppNotification
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.NotificationType
import com.finlux.app.domain.model.SalaryCycleConfig
import com.finlux.app.domain.repository.NotificationRepository
import com.finlux.app.domain.repository.SalaryCycleRepository
import com.finlux.app.domain.repository.SalaryCycleScheduler
import com.finlux.app.domain.repository.WalletRepository
import com.finlux.app.domain.usecase.ExecuteSalaryRolloverUseCase
import com.finlux.app.domain.usecase.SalaryRolloverResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private const val TAG = "SalaryCycleReceiver"

@AndroidEntryPoint
class SalaryCycleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var salaryCycleRepository: SalaryCycleRepository

    @Inject
    lateinit var walletRepository: WalletRepository

    @Inject
    lateinit var executeSalaryRolloverUseCase: ExecuteSalaryRolloverUseCase

    @Inject
    lateinit var systemNotificationHelper: SystemNotificationHelper

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var salaryCycleScheduler: SalaryCycleScheduler

    @Inject
    lateinit var clock: FinanceClock

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered with action: ${intent.action}")
        if (intent.action != ACTION_SALARY_PAYDAY) {
            Log.w(TAG, "Action mismatch: expected $ACTION_SALARY_PAYDAY, got ${intent.action}")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = salaryCycleRepository.observeConfig().firstOrNull() ?: SalaryCycleConfig()
                val isForced = intent.getBooleanExtra("force", false)
                Log.d(TAG, "SalaryCycleConfig: enabled=${config.enabled}, paydayDay=${config.paydayDay}, rolloverRule=${config.rolloverRule}, isForced=$isForced")

                if (!config.enabled && !isForced) {
                    Log.w(TAG, "Salary cycle is disabled in settings. Skipping execution. (Use --ez force true to test).")
                    return@launch
                }

                val now = clock.now()

                // 1. Send Welcome Notification for the new financial cycle
                val welcomeTitle = "🎉 Chu kỳ tài chính mới!"
                val welcomeBody = "Hôm nay là ngày nhận lương của kỳ mới! Bắt đầu kế hoạch chi tiêu mới cùng FinLux."
                val welcomeNoti = AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = welcomeTitle,
                    body = welcomeBody,
                    type = NotificationType.SYSTEM,
                    targetRoute = "reports",
                    timestamp = now,
                )
                val inAppResult = notificationRepository.saveNotification(welcomeNoti)
                Log.d(TAG, "Saved in-app welcome notification: $inAppResult")

                systemNotificationHelper.postGeneralNotification(
                    title = welcomeTitle,
                    body = welcomeBody,
                    targetRoute = "reports",
                    notificationId = 9925,
                )
                Log.d(TAG, "Dispatched system push notification (id=9925)")

                // 2. Process Rollover Rules
                val wallets = walletRepository.observeWallets().firstOrNull().orEmpty()
                when (config.rolloverRule) {
                    CycleRolloverRule.MOVE_TO_SAVINGS -> {
                        Log.d(TAG, "Processing rollover rule MOVE_TO_SAVINGS with ${wallets.size} wallets")
                        val rolloverResult = executeSalaryRolloverUseCase(config, wallets, now)
                        Log.d(TAG, "executeSalaryRolloverUseCase result: $rolloverResult")
                        if (rolloverResult is AppResult.Success && rolloverResult.value is SalaryRolloverResult.Transferred) {
                            val transferred = rolloverResult.value as SalaryRolloverResult.Transferred
                            val amountStr = formatVnd(transferred.amount)
                            val rolloverTitle = "💰 Tích lũy chu kỳ lương"
                            val rolloverBody = "Đã tự động kết chuyển $amountStr tiền dư của kỳ trước sang ví tích lũy."
                            val rolloverNoti = AppNotification(
                                id = UUID.randomUUID().toString(),
                                title = rolloverTitle,
                                body = rolloverBody,
                                type = NotificationType.SYSTEM,
                                targetRoute = "wallets",
                                timestamp = now,
                            )
                            notificationRepository.saveNotification(rolloverNoti)
                            systemNotificationHelper.postGeneralNotification(
                                title = rolloverTitle,
                                body = rolloverBody,
                                targetRoute = "wallets",
                                notificationId = 9926,
                            )
                            Log.d(TAG, "Dispatched rollover transfer notification (id=9926, amount=$amountStr)")
                        }
                    }
                    CycleRolloverRule.ASK_EACH_CYCLE -> {
                        Log.d(TAG, "Processing rollover rule ASK_EACH_CYCLE")
                        val askTitle = "💼 Kết chuyển tiền dư kỳ lương"
                        val askBody = "Kỳ lương trước đã kết thúc. Bạn có muốn kiểm tra và chuyển tiền dư sang ví tích lũy?"
                        val askNoti = AppNotification(
                            id = UUID.randomUUID().toString(),
                            title = askTitle,
                            body = askBody,
                            type = NotificationType.SYSTEM,
                            targetRoute = "wallets",
                            timestamp = now,
                        )
                        notificationRepository.saveNotification(askNoti)
                        systemNotificationHelper.postGeneralNotification(
                            title = askTitle,
                            body = askBody,
                            targetRoute = "wallets",
                            notificationId = 9926,
                        )
                        Log.d(TAG, "Dispatched rollover reminder notification (id=9926)")
                    }
                    CycleRolloverRule.KEEP_IN_WALLET -> {
                        Log.d(TAG, "Rollover rule is KEEP_IN_WALLET. No rollover action needed.")
                    }
                }

                // 3. Reschedule Next Payday Trigger
                if (config.enabled) {
                    salaryCycleScheduler.scheduleNextPayday(config)
                    Log.d(TAG, "Scheduled next payday trigger")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing salary cycle broadcast", e)
            } finally {
                pendingResult.finish()
                Log.d(TAG, "Broadcast processing finished")
            }
        }
    }

    private fun formatVnd(amount: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount)
}
