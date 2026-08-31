package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
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

    // Nền gradient ấm theo mockup: từ vàng kem/cam đào nhẹ sang trắng ngà
    val warmBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF7ED), // #fff7ed Amber/Orange 50
            Color(0xFFFFFBEB), // #fffbeb Amber 50
            Color(0xFFFEF3C7).copy(alpha = 0.5f),
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(warmBackgroundBrush)
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left Column: Tiêu đề + mô tả + badge thời gian
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2), // Đỏ nhạt
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TrackChanges,
                                contentDescription = null,
                                tint = Color(0xFFEF4444), // Đỏ bia ngắm
                                modifier = Modifier.padding(3.dp),
                            )
                        }
                        Text(
                            "Vòng quay tiết kiệm",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                        )
                    }

                    Text(
                        "Quay xem hôm nay\nđể dành bao nhiêu nhé",
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF64748B),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badge giờ nhắc
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7), // Vàng kem đậm hơn chút
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp),
                            )
                            val timeStr = when (session.status) {
                                SavingSpinStatus.SNOOZED -> session.snoozedUntil?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Nhắc lại"
                                SavingSpinStatus.COMPLETED -> "Đã nạp thành công"
                                else -> String.format("%02d:%02d sáng", state.config.reminderHour, state.config.reminderMinute)
                            }
                            Text(
                                timeStr,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309),
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
                        values = session.wheelValues.ifEmpty { listOf(Money(10000), Money(20000), Money(30000), Money(50000), Money(100000), Money(200000)) },
                        selectedIndex = null,
                        isSpinning = false,
                        modifier = Modifier.size(108.dp),
                    )

                    // Nút QUAY đè ở phía trước dưới tâm vòng quay theo mockup
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB), // Xanh dương đậm theo mockup
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                            .size(width = 82.dp, height = 34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(
                            when (session.status) {
                                SavingSpinStatus.SPUN_PENDING -> "NẠP"
                                SavingSpinStatus.COMPLETED -> "XEM"
                                else -> "QUAY"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
