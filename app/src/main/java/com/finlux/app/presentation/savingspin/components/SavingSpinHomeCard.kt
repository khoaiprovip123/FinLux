package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SavingSpinHomeCard(
    state: SavingSpinUiState,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = state.session ?: return
    if (!state.config.enabled || !state.config.showOnHome) return
    val tokens = LocalFinluxTokens.current
    val modern = LocalAppUiStyle.current == AppUiStyle.MODERN_LUXURY
    val title = when (session.status) {
        SavingSpinStatus.READY -> "Vòng quay tiết kiệm hôm nay"
        SavingSpinStatus.SPUN_PENDING -> "Đang chờ bạn cất tiền"
        SavingSpinStatus.SNOOZED -> "Đã nhắc lại lượt tiết kiệm"
        SavingSpinStatus.COMPLETED -> "Bạn đã hoàn thành lượt này"
        SavingSpinStatus.SKIPPED -> "Lượt tiết kiệm đã bỏ qua"
    }
    val detail = when (session.status) {
        SavingSpinStatus.READY -> "Chạm để quay một mệnh giá duy nhất"
        SavingSpinStatus.SPUN_PENDING -> formatVndAmount(session.selectedAmount?.value ?: 0L)
        SavingSpinStatus.SNOOZED -> session.snoozedUntil?.atZone(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("HH:mm • dd/MM")) ?: "Sẽ nhắc lại sau"
        SavingSpinStatus.COMPLETED -> "${formatVndAmount(session.selectedAmount?.value ?: 0L)} • Chuỗi +1"
        SavingSpinStatus.SKIPPED -> "Hẹn bạn ở kỳ tiếp theo"
    }
    val icon = when (session.status) {
        SavingSpinStatus.COMPLETED -> Icons.Filled.CheckCircle
        SavingSpinStatus.SNOOZED -> Icons.Filled.Schedule
        else -> Icons.Filled.Casino
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.radius.standardCard))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(tokens.radius.standardCard),
        color = if (modern) tokens.surface else tokens.surfaceSoft,
        tonalElevation = tokens.elevation,
    ) {
        Row(
            modifier = Modifier
                .then(if (modern) Modifier.background(tokens.heroBrush) else Modifier)
                .padding(tokens.spacing.base),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            Surface(
                shape = CircleShape,
                color = if (modern) tokens.heroGlassSurface else tokens.primary.copy(alpha = .12f),
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (modern) tokens.onHero else tokens.primary,
                    modifier = Modifier.padding(tokens.spacing.md),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
                Text(
                    title,
                    style = FinluxTextStyles.CardTitle,
                    color = if (modern) tokens.onHero else tokens.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    detail,
                    style = FinluxTextStyles.Caption,
                    color = if (modern) tokens.onHeroMuted else tokens.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Mở vòng quay",
                tint = if (modern) tokens.onHero else tokens.primary,
            )
        }
    }
}
