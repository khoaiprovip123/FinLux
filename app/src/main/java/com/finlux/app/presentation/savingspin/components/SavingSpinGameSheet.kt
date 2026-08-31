package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinGameSheet(
    state: SavingSpinUiState,
    onAction: (SavingSpinAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isGameOpen) return
    val session = state.session ?: return
    val tokens = LocalFinluxTokens.current
    FinluxBottomSheet(
        onDismissRequest = { onAction(SavingSpinAction.CloseGame) },
        modifier = modifier,
        title = "Vòng quay tiết kiệm",
        subtitle = "Mỗi lịch chỉ có một kết quả và không thể quay lại",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (session.status) {
                SavingSpinStatus.READY -> {
                    SavingSpinWheel(
                        values = session.wheelValues,
                        selectedIndex = session.selectedIndex,
                        isSpinning = state.isSpinning,
                        modifier = Modifier.size(300.dp).heightIn(max = 320.dp),
                    )
                    Text("Quay để chọn mệnh giá tiết kiệm hôm nay", style = FinluxTextStyles.Body, color = tokens.onSurface)
                    Button(
                        onClick = { onAction(SavingSpinAction.Spin) },
                        enabled = !state.isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onHero),
                        modifier = Modifier.fillMaxWidth().testTag("saving_spin_button"),
                    ) {
                        if (state.isSpinning) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = tokens.onHero)
                        else Text("QUAY NGAY", fontWeight = FontWeight.Bold)
                    }
                }
                SavingSpinStatus.SPUN_PENDING,
                SavingSpinStatus.SNOOZED -> SavingSpinResultContent(
                    session = session,
                    destinations = state.destinations,
                    selectedDestinationId = state.selectedDestinationId,
                    allowSkip = state.config.allowSkip,
                    onSelectDestination = { onAction(SavingSpinAction.SelectDestination(it)) },
                    onConfirm = { onAction(SavingSpinAction.ConfirmDeposit) },
                    onSnooze = { onAction(SavingSpinAction.Snooze(Instant.now().plusSeconds(30L * 60L))) },
                    onSkip = { onAction(SavingSpinAction.Skip) },
                )
                SavingSpinStatus.COMPLETED -> {
                    Text("Đã hoàn thành", style = FinluxTextStyles.SectionTitle, color = tokens.primary)
                    Text(
                        formatVndAmount(session.selectedAmount?.value ?: 0L),
                        style = FinluxTextStyles.DisplayAmount,
                        color = tokens.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Khoản này đã được ghi vào báo cáo tiết kiệm.", style = FinluxTextStyles.Body, color = tokens.onSurfaceVariant)
                }
                SavingSpinStatus.SKIPPED -> {
                    Text("Lượt này đã được bỏ qua", style = FinluxTextStyles.SectionTitle, color = tokens.onSurface)
                    Text("Bạn có thể quay lại vào kỳ tiếp theo.", style = FinluxTextStyles.Body, color = tokens.onSurfaceVariant)
                }
            }
            state.errorMessage?.let { Text(it, style = FinluxTextStyles.Caption, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        }
    }
}
