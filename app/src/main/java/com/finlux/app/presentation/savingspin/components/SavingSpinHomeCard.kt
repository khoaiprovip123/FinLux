package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Money
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

    val cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            tokens.primary.copy(alpha = 0.08f),
            tokens.surfaceSoft,
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(24.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundBrush)
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left Column: Tiêu đề + mô tả + badge thời gian
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tokens.primary.copy(alpha = 0.14f),
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TrackChanges,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.padding(3.dp),
                            )
                        }
                        Text(
                            text = "Vòng quay tiết kiệm",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface,
                        )
                    }

                    Text(
                        text = "Quay xem hôm nay\nđể dành bao nhiêu nhé",
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = tokens.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badge giờ nhắc
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.border),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(13.dp),
                            )
                            val timeStr = when (session.status) {
                                SavingSpinStatus.SNOOZED -> session.snoozedUntil?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Nhắc lại"
                                SavingSpinStatus.COMPLETED -> "Đã nạp thành công"
                                else -> String.format("%02d:%02d", state.config.reminderHour, state.config.reminderMinute)
                            }
                            Text(
                                text = timeStr,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Right Column: Bánh xe thu nhỏ + Nút Quay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(width = 118.dp, height = 112.dp),
                ) {
                    SavingSpinWheel(
                        values = session.wheelValues.ifEmpty {
                            listOf(
                                Money(10000),
                                Money(20000),
                                Money(30000),
                                Money(50000),
                                Money(100000),
                                Money(200000),
                            )
                        },
                        selectedIndex = null,
                        isSpinning = false,
                        modifier = Modifier.size(108.dp),
                    )

                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.primary,
                            contentColor = tokens.onHero,
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                            .size(width = 82.dp, height = 34.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = when (session.status) {
                                SavingSpinStatus.SPUN_PENDING -> "NẠP"
                                SavingSpinStatus.COMPLETED -> "XEM"
                                else -> "QUAY"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onHero,
                        )
                    }
                }
            }
        }
    }
}
