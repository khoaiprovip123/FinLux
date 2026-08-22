package com.finlux.app.presentation.debt.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.domain.model.DebtPayoffPlan
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd
import java.time.format.DateTimeFormatter

@Composable
fun StrategySelectorCard(
    currentStrategy: PayoffStrategy,
    extraMonthlyPayment: Long,
    payoffPlan: DebtPayoffPlan?,
    onStrategySelected: (PayoffStrategy) -> Unit,
    onExtraPaymentChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(FinluxBlue, FinluxPurple))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chiến lược thoát nợ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Tối ưu hóa thời gian & tiền lãi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Strategy Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val isSnowball = currentStrategy == PayoffStrategy.SNOWBALL
                val isAvalanche = currentStrategy == PayoffStrategy.AVALANCHE

                StrategyTabItem(
                    title = "Snowball (Cầu tuyết)",
                    subtitle = "Nợ nhỏ trước",
                    isSelected = isSnowball,
                    onClick = { onStrategySelected(PayoffStrategy.SNOWBALL) },
                    modifier = Modifier.weight(1f),
                )

                StrategyTabItem(
                    title = "Avalanche (Lở tuyết)",
                    subtitle = "Lãi cao trước",
                    isSelected = isAvalanche,
                    onClick = { onStrategySelected(PayoffStrategy.AVALANCHE) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Strategy explanation description
            val desc = if (currentStrategy == PayoffStrategy.SNOWBALL) {
                "⛄ Ưu tiên dồn tiền trả hết khoản nợ nhỏ nhất trước để nhanh chóng giảm số lượng chủ nợ và tạo động lực tâm lý mạnh mẽ."
            } else {
                "🏔️ Ưu tiên dồn tiền trả khoản nợ có lãi suất (APR %) cao nhất trước để triệt tiêu tiền lãi phát sinh và tiết kiệm tiền tối đa."
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(18.dp))

            // Extra Monthly Payment Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trả thêm mỗi tháng (Extra):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "+${extraMonthlyPayment.toVnd()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = FinluxBlue,
                    ),
                )
            }

            Slider(
                value = (extraMonthlyPayment / 100_000L).toFloat(),
                onValueChange = { onExtraPaymentChanged(it.toLong() * 100_000L) },
                valueRange = 0f..100f, // 0 đến 10 triệu
                steps = 19, // bước 500k
                colors = SliderDefaults.colors(
                    thumbColor = FinluxBlue,
                    activeTrackColor = FinluxBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Quick extra chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(0L, 500_000L, 1_000_000L, 2_000_000L, 5_000_000L).forEach { amount ->
                    val isChipSelected = extraMonthlyPayment == amount
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChipSelected) FinluxBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onExtraPaymentChanged(amount) },
                    ) {
                        Text(
                            text = if (amount == 0L) "0 đ" else "+${amount.toShortVnd()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChipSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            // Results Insight Banner
            if (payoffPlan != null && payoffPlan.totalMonths > 0) {
                Spacer(Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Dự kiến sạch nợ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val dateText = payoffPlan.estimatedDebtFreeDate?.format(DateTimeFormatter.ofPattern("'Tháng' MM/yyyy")) ?: "—"
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tiết kiệm tiền lãi",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = payoffPlan.totalInterestSaved.value.toVnd(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxPurple,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyTabItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) FinluxBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    color = if (isSelected) FinluxPurple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                ),
            )
        }
    }
}
