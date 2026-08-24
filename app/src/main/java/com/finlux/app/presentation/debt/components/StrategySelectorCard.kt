package com.finlux.app.presentation.debt.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtCashflowAnalysis
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
    initialDebtAmount: Long,
    cashflowAnalysis: DebtCashflowAnalysis? = null,
    onStrategySelected: (PayoffStrategy) -> Unit,
    onExtraPaymentChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            // Header: Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(FinluxColors.PrimaryBlue, FinluxColors.PrimaryViolet))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chiến lược & Lộ trình thoát nợ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            ),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "Tối ưu hóa thời gian và tiền lãi phải trả",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Strategy Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.surfaceSoft.copy(alpha = 0.6f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val isSnowball = currentStrategy == PayoffStrategy.SNOWBALL
                val isAvalanche = currentStrategy == PayoffStrategy.AVALANCHE

                StrategyTabItem(
                    title = "❄️ Snowball (Cầu tuyết)",
                    subtitle = "Nợ nhỏ trả trước",
                    isSelected = isSnowball,
                    onClick = { onStrategySelected(PayoffStrategy.SNOWBALL) },
                    modifier = Modifier.weight(1f),
                )

                StrategyTabItem(
                    title = "⚡ Avalanche (Lở tuyết)",
                    subtitle = "Lãi cao trả trước",
                    isSelected = isAvalanche,
                    onClick = { onStrategySelected(PayoffStrategy.AVALANCHE) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(10.dp))

            // Strategy explanation description
            val desc = if (currentStrategy == PayoffStrategy.SNOWBALL) {
                "Ưu tiên dồn tiền trả hết khoản nợ nhỏ nhất trước để nhanh chóng giảm số lượng chủ nợ và tạo động lực tâm lý."
            } else {
                "Ưu tiên dồn tiền trả khoản nợ có lãi suất (APR %) cao nhất trước để triệt tiêu tiền lãi phát sinh và tiết kiệm tiền tối đa."
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                ),
                color = tokens.onSurfaceVariant,
            )

            // Cashflow Advisor Section (AI Integration)
            if (cashflowAnalysis != null) {
                Spacer(Modifier.height(14.dp))
                CashflowAdvisorCard(
                    analysis = cashflowAnalysis,
                    currentExtraPayment = extraMonthlyPayment,
                    onScenarioSelected = onExtraPaymentChanged,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Extra Monthly Payment Slider Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trả thêm mỗi tháng (Extra):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                    color = tokens.onSurface,
                )
                Text(
                    text = "+${extraMonthlyPayment.toVnd()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = FinluxColors.PrimaryBlue,
                    ),
                )
            }

            Slider(
                value = (extraMonthlyPayment / 100_000L).toFloat(),
                onValueChange = { onExtraPaymentChanged(it.toLong() * 100_000L) },
                valueRange = 0f..100f, // 0 đến 10 triệu
                steps = 19, // bước 500k
                colors = SliderDefaults.colors(
                    thumbColor = FinluxColors.PrimaryBlue,
                    activeTrackColor = FinluxColors.PrimaryBlue,
                    inactiveTrackColor = tokens.surfaceSoft,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Quick extra chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(0L, 500_000L, 1_000_000L, 2_000_000L, 5_000_000L).forEach { amount ->
                    val isChipSelected = extraMonthlyPayment == amount
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChipSelected) FinluxColors.PrimaryBlue else tokens.surfaceSoft.copy(alpha = 0.7f),
                        border = BorderStroke(0.6.dp, if (isChipSelected) FinluxColors.PrimaryBlue else tokens.border),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onExtraPaymentChanged(amount) },
                    ) {
                        Text(
                            text = if (amount == 0L) "0 đ" else "+${amount.toShortVnd()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isChipSelected) Color.White else tokens.onSurfaceVariant,
                            ),
                            modifier = Modifier.padding(vertical = 5.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // Results Insight Banner
            if (payoffPlan != null && payoffPlan.totalMonths > 0) {
                Spacer(Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FinluxColors.IncomeGreen.copy(alpha = 0.10f),
                    border = BorderStroke(0.8.dp, FinluxColors.IncomeGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Dự kiến sạch nợ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = tokens.onSurfaceVariant,
                            )
                            val dateText = payoffPlan.estimatedDebtFreeDate?.format(DateTimeFormatter.ofPattern("'Tháng' MM/yyyy")) ?: "—"
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxColors.IncomeGreen,
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(FinluxColors.IncomeGreen.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tiền lãi tiết kiệm",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = tokens.onSurfaceVariant,
                            )
                            Text(
                                text = "+${payoffPlan.totalInterestSaved.value.toVnd()}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinluxColors.IncomeGreen,
                                ),
                            )
                        }
                    }
                }
            }

            // Embedded Burndown Chart
            if (payoffPlan != null && payoffPlan.totalMonths > 0 && initialDebtAmount > 0L) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(
                    color = tokens.border,
                    thickness = 0.8.dp,
                )
                Spacer(Modifier.height(14.dp))
                EmbeddedDebtBurndownChart(
                    payoffPlan = payoffPlan,
                    initialDebtAmount = initialDebtAmount,
                )
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
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) tokens.surface else Color.Transparent,
        border = if (isSelected) BorderStroke(0.8.dp, tokens.border) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) FinluxColors.PrimaryBlue else tokens.onSurface,
                ),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = tokens.onSurfaceVariant,
                ),
            )
        }
    }
}
