package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingSpinSession

@Composable
fun SavingSpinResultContent(
    session: SavingSpinSession,
    destinations: List<SavingDestination>,
    selectedDestinationId: String?,
    allowSkip: Boolean,
    onSelectDestination: (String) -> Unit,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val enabledDestinations = destinations.filter { it.enabled }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Mệnh giá của bạn", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
        Text(
            text = formatVndAmount(session.selectedAmount?.value ?: 0L),
            style = FinluxTextStyles.DisplayAmount,
            color = tokens.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("saving_spin_selected_amount"),
        )
        Text("Chọn nơi bạn sẽ cất khoản tiền này", style = FinluxTextStyles.Body, color = tokens.onSurface)

        if (enabledDestinations.isEmpty()) {
            Text(
                "Bạn chưa có nơi tiết kiệm. Hãy thêm một nơi tiết kiệm trong phần Cài đặt.",
                style = FinluxTextStyles.Caption,
                color = tokens.onSurfaceVariant,
            )
        } else {
            enabledDestinations.forEach { destination ->
                SavingDestinationCard(
                    destination = destination,
                    selected = destination.id == selectedDestinationId,
                    onClick = { onSelectDestination(destination.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(
            onClick = onConfirm,
            enabled = selectedDestinationId != null && enabledDestinations.any { it.id == selectedDestinationId },
            modifier = Modifier.fillMaxWidth().testTag("saving_spin_confirm"),
            colors = ButtonDefaults.buttonColors(containerColor = tokens.primary, contentColor = tokens.onHero),
        ) {
            Text("ĐÃ CẤT TIỀN")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
            OutlinedButton(onClick = onSnooze, modifier = Modifier.weight(1f)) { Text("Nhắc sau") }
            if (allowSkip) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).testTag("saving_spin_skip")) {
                    Text("Bỏ qua")
                }
            }
        }
    }
}
