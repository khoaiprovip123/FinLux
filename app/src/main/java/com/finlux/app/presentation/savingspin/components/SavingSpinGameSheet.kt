package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinGameSheet(
    state: SavingSpinUiState,
    onAction: (SavingSpinAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isGameOpen) return
    val session = state.session ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tokens = LocalFinluxTokens.current

    ModalBottomSheet(
        onDismissRequest = { onAction(SavingSpinAction.CloseGame) },
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(tokens.border),
            )
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SavingSpinHeader(
                onClose = { onAction(SavingSpinAction.CloseGame) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Error banner nếu có
            if (state.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FinluxColors.ExpenseRed.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    Text(
                        text = state.errorMessage,
                        fontSize = 13.sp,
                        color = FinluxColors.ExpenseRed,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }

            when {
                // Trạng thái READY hoặc đang quay animation
                session.status == SavingSpinStatus.READY || (session.status == SavingSpinStatus.SPUN_PENDING && state.isWheelAnimating) -> {
                    SavingSpinReadyContent(
                        state = state,
                        session = session,
                        onSpin = { onAction(SavingSpinAction.Spin) },
                        onWheelAnimationFinished = { onAction(SavingSpinAction.WheelAnimationFinished) },
                    )
                }

                // Trạng thái đã quay xong và chờ xác nhận (SPUN_PENDING hoặc SNOOZED)
                session.status == SavingSpinStatus.SPUN_PENDING || session.status == SavingSpinStatus.SNOOZED -> {
                    SavingSpinResultContent(
                        session = session,
                        destinations = state.destinations,
                        selectedDestinationId = state.selectedDestinationId,
                        wallets = state.wallets,
                        sourceWalletId = state.sourceWalletId,
                        streakCount = state.streakCount,
                        allowSkip = state.config.allowSkip,
                        isConfirming = state.isConfirming,
                        onSelectDestination = { onAction(SavingSpinAction.SelectDestination(it)) },
                        onSelectSourceWallet = { onAction(SavingSpinAction.SelectSourceWallet(it)) },
                        onConfirm = { onAction(SavingSpinAction.ConfirmDeposit) },
                        onOpenSnooze = { onAction(SavingSpinAction.OpenSnoozeSheet) },
                        onSkip = { onAction(SavingSpinAction.Skip) },
                    )
                }

                // Trạng thái COMPLETED
                session.status == SavingSpinStatus.COMPLETED -> {
                    SavingSpinCompletedContent(
                        session = session,
                        destinations = state.destinations,
                        wallets = state.wallets,
                        sourceWalletId = state.sourceWalletId,
                        onClose = { onAction(SavingSpinAction.CloseGame) },
                    )
                }

                // Trạng thái SKIPPED
                session.status == SavingSpinStatus.SKIPPED -> {
                    SavingSpinSkippedContent(
                        onClose = { onAction(SavingSpinAction.CloseGame) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Snooze
    SavingSpinSnoozeSheet(
        isOpen = state.isSnoozeSheetOpen,
        onDismiss = { onAction(SavingSpinAction.CloseSnoozeSheet) },
        onSelectPreset = { targetTime -> onAction(SavingSpinAction.Snooze(targetTime)) },
    )
}
