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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class SnoozePreset(
    val label: String,
    val targetTime: Instant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinSnoozeSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectPreset: (Instant) -> Unit,
    modifier: Modifier = Modifier,
    zoneId: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh"),
    now: Instant = Instant.now(),
) {
    if (!isOpen) return
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentZdt = now.atZone(zoneId)
    val presets = buildList {
        add(SnoozePreset("30 phút nữa", now.plusSeconds(30L * 60L)))
        add(SnoozePreset("1 giờ nữa", now.plusSeconds(60L * 60L)))

        val twelveOClock = currentZdt.with(LocalTime.of(12, 0, 0))
        if (currentZdt.isBefore(twelveOClock)) {
            add(SnoozePreset("12:00 trưa nay", twelveOClock.toInstant()))
        }

        val eighteenOClock = currentZdt.with(LocalTime.of(18, 0, 0))
        if (currentZdt.isBefore(eighteenOClock)) {
            add(SnoozePreset("18:00 chiều nay", eighteenOClock.toInstant()))
        }

        val tomorrowMorning = currentZdt.plusDays(1).with(LocalTime.of(9, 0, 0))
        add(SnoozePreset("09:00 sáng mai", tomorrowMorning.toInstant()))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Nhắc tôi sau",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Chọn thời gian để FinLux nhắc bạn quay lại hoàn thành lượt tiết kiệm này:",
                fontSize = 13.sp,
                color = tokens.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { preset ->
                    OutlinedButton(
                        onClick = {
                            onSelectPreset(preset.targetTime)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, tokens.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Text(preset.label, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
