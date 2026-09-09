package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinFrequency
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.presentation.savingspin.SavingSpinUiState

@Composable
fun SavingSpinReadyContent(
    state: SavingSpinUiState,
    session: SavingSpinSession,
    onSpin: () -> Unit,
    onWheelAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    val scheduleText = when (state.config.frequency) {
        SavingSpinFrequency.DAILY, SavingSpinFrequency.SELECTED_WEEKDAYS -> "Lượt tiết kiệm hôm nay"
        SavingSpinFrequency.WEEKLY -> "Lượt tiết kiệm tuần này"
        SavingSpinFrequency.SALARY_CYCLE -> "Lượt tiết kiệm kỳ này"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Badge lượt quay
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
            contentColor = tokens.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = scheduleText,
                    fontSize = 12.sp,
                    color = tokens.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vòng quay Wheel Canvas
        SavingSpinWheel(
            values = session.wheelValues,
            selectedIndex = session.selectedIndex,
            isSpinning = state.isSpinning,
            onAnimationFinished = onWheelAnimationFinished,
            modifier = Modifier.size(270.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút QUAY NGAY
        Button(
            onClick = onSpin,
            enabled = !state.isSpinning,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.primary,
                contentColor = tokens.onHero,
                disabledContainerColor = tokens.primary.copy(alpha = 0.5f),
                disabledContentColor = tokens.onHero.copy(alpha = 0.7f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("saving_spin_button"),
        ) {
            if (state.isSpinning) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = tokens.onHero)
            } else {
                Text("QUAY NGAY", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thẻ info: Khoảng tiền và bước mệnh giá
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = tokens.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Khoảng tiền: ${formatVndAmount(state.config.minAmount.value, isCompact = true)} - ${formatVndAmount(state.config.maxAmount.value, isCompact = true)} (Bước: ${formatVndAmount(state.config.step.amount, isCompact = true)})",
                    fontSize = 11.5.sp,
                    color = tokens.onSurfaceVariant,
                )
            }
        }
    }
}
