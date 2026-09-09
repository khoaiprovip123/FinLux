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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.finlux.app.domain.model.PaydayAllocationPlan
import com.finlux.app.domain.model.PayoffStrategy
import com.finlux.app.domain.model.StrategyComparison
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
    onSchedulePaydayReminder: ((paydayDay: Int, walletName: String?, totalAmount: Long) -> Unit)? = null,
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

            // Strategy Comparison Callout Tag
            StrategyComparisonCard(
                currentStrategy = currentStrategy,
                comparison = payoffPlan?.comparison,
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
                            val cycleText = if (payoffPlan.paydayPlan != null) {
                                "Còn ${formatPayoffDuration(payoffPlan.totalCycles, isSalaryCycle = true)}"
                            } else {
                                "Còn ${formatPayoffDuration(payoffPlan.totalMonths, isSalaryCycle = false)}"
                            }
                            Text(
                                text = cycleText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tokens.primary,
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(FinluxColors.IncomeGreen.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val savedLabel = when {
                                payoffPlan.isBaselineTrap -> "Bẫy nợ tối thiểu"
                                payoffPlan.isZeroAprOnly -> "Tiết kiệm thời gian"
                                else -> "Tiền lãi tiết kiệm"
                            }
                            Text(
                                text = savedLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = tokens.onSurfaceVariant,
                            )
                            val savedValue = when {
                                payoffPlan.isBaselineTrap -> "Chặn lãi thả nổi"
                                payoffPlan.isZeroAprOnly -> {
                                    if (payoffPlan.timeSavedCycles > 0) "Rút ngắn ${formatPayoffDuration(payoffPlan.timeSavedCycles, isSalaryCycle = true)}" else "Gốc 0% APR"
                                }
                                else -> "+${payoffPlan.totalInterestSaved.value.toVnd()}"
                            }
                            Text(
                                text = savedValue,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (payoffPlan.isBaselineTrap) FinluxColors.WarningAmber else FinluxColors.IncomeGreen,
                                ),
                            )
                            if (payoffPlan.isBaselineTrap) {
                                Text(
                                    text = "Thoát bẫy nợ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FinluxColors.IncomeGreen,
                                    ),
                                )
                            } else if (!payoffPlan.isZeroAprOnly && payoffPlan.timeSavedCycles > 0) {
                                Text(
                                    text = "Rút ngắn ${formatPayoffDuration(payoffPlan.timeSavedCycles, isSalaryCycle = true)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = tokens.primary,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Thẻ cảnh báo bẫy nợ (Amber Glass) nằm ngay dưới thẻ kết quả
                if (payoffPlan.isBaselineTrap) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FinluxColors.WarningAmber.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, FinluxColors.WarningAmber.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = FinluxColors.WarningAmber,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Trả tối thiểu không đủ bù tiền lãi phát sinh (Bẫy nợ âm). Kế hoạch này giúp dồn thêm tiền để chặn lãi thả nổi và dứt điểm nợ!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tokens.onSurface,
                                ),
                            )
                        }
                    }
                }
            }

            // Cảnh báo lệch pha dòng tiền (Payday vs Due Date Mismatch)
            val warnings = payoffPlan?.paydayPlan?.mismatchedWarnings.orEmpty()
            if (warnings.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FinluxColors.WarningAmber.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, FinluxColors.WarningAmber.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = FinluxColors.WarningAmber,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Cảnh báo lệch pha dòng tiền",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = FinluxColors.WarningAmber,
                                ),
                            )
                        }
                        warnings.forEach { warning ->
                            Text(
                                text = "• $warning",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                ),
                                color = tokens.onSurface,
                            )
                        }
                    }
                }
            }

            // Bảng phân bổ trích lương cho kỳ lương tới (Payday Allocation Matrix)
            val paydayPlan = payoffPlan?.paydayPlan
            if (paydayPlan != null && paydayPlan.items.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                PaydayAllocationCard(
                    plan = paydayPlan,
                    onScheduleReminder = {
                        val targetPayday = if (paydayPlan.isSemiMonthly) paydayPlan.upcomingPaydayDay else paydayPlan.paydayDay
                        val targetAmount = if (paydayPlan.isSemiMonthly) paydayPlan.upcomingPaydayDeduction.value else paydayPlan.totalDebtDeduction.value
                        onSchedulePaydayReminder?.invoke(
                            targetPayday,
                            paydayPlan.salaryWalletName,
                            targetAmount,
                        )
                    },
                )
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

@Composable
private fun PaydayAllocationCard(
    plan: PaydayAllocationPlan,
    onScheduleReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    var isReminderScheduled by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surfaceSoft.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header: Title & Payday info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(tokens.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column {
                        val titleText = if (plan.isSemiMonthly) plan.upcomingPaydayLabel else "Kế hoạch trích lương ngày ${plan.paydayDay}"
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                            ),
                            color = tokens.onSurface,
                        )
                        val walletInfo = plan.salaryWalletName?.let { "Trích từ ví: $it" } ?: "Trích từ lương dự kiến"
                        val subtitleText = if (plan.isSemiMonthly) "$walletInfo • Đợt sắp tới" else walletInfo
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tokens.primary.copy(alpha = 0.12f),
                ) {
                    val ratioText = if (plan.isSemiMonthly) {
                        "${plan.upcomingDeductionRatioPercent.toInt()}% đợt này"
                    } else {
                        "${plan.deductionRatioPercent.toInt()}% lương"
                    }
                    Text(
                        text = ratioText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = tokens.primary,
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }

            // Allocation total summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val labelText = if (plan.isSemiMonthly) "Trích nợ đợt này:" else "Tổng trích nợ kỳ tới:"
                val totalAmountText = if (plan.isSemiMonthly) {
                    plan.upcomingPaydayDeduction.value.toVnd()
                } else {
                    plan.totalDebtDeduction.value.toVnd()
                }
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                    ),
                    color = tokens.onSurfaceVariant,
                )
                Text(
                    text = totalAmountText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = FinluxColors.ExpenseRed,
                    ),
                )
            }

            // Allocation items list
            val displayItems = if (plan.isSemiMonthly && plan.upcomingItems.isNotEmpty()) plan.upcomingItems else plan.items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                displayItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = item.debtName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                    ),
                                    color = tokens.onSurface,
                                    maxLines = 1,
                                )
                                if (item.isTargetDebt) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = FinluxColors.PrimaryBlue.copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = "🎯 Ưu tiên",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FinluxColors.PrimaryBlue,
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                                if (!item.sponsorLabel.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    ) {
                                        Text(
                                            text = item.sponsorLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF10B981),
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                            }
                            val minText = "Tối thiểu: ${item.minimumPayment.value.toVnd()}"
                            val extraText = if (item.extraPayment.value > 0) " + Extra: ${item.extraPayment.value.toVnd()}" else ""
                            Text(
                                text = "$minText$extraText",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = tokens.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = item.totalPayment.value.toVnd(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (item.isTargetDebt) tokens.primary else tokens.onSurface,
                            ),
                        )
                    }
                }
            }

            // Reminder schedule button
            val targetReminderDay = if (plan.isSemiMonthly) plan.upcomingPaydayDay else plan.paydayDay
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isReminderScheduled) FinluxColors.IncomeGreen.copy(alpha = 0.15f) else tokens.primary.copy(alpha = 0.10f),
                border = BorderStroke(0.8.dp, if (isReminderScheduled) FinluxColors.IncomeGreen.copy(alpha = 0.35f) else tokens.primary.copy(alpha = 0.30f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onScheduleReminder()
                        isReminderScheduled = true
                    },
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isReminderScheduled) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isReminderScheduled) FinluxColors.IncomeGreen else tokens.primary,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isReminderScheduled) "Đã lên lịch nhắc trích nợ đợt $targetReminderDay" else "🔔 Đặt lịch nhắc trích nợ đợt $targetReminderDay",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (isReminderScheduled) FinluxColors.IncomeGreen else tokens.primary,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyComparisonCard(
    currentStrategy: PayoffStrategy,
    comparison: StrategyComparison?,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isAvalanche = currentStrategy == PayoffStrategy.AVALANCHE
    val isZeroApr = comparison?.isZeroAprOnly == true

    val accentColor = when {
        isZeroApr -> tokens.primary
        isAvalanche -> FinluxColors.IncomeGreen
        else -> FinluxColors.PrimaryBlue
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceSoft.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        isZeroApr -> "ℹ️"
                        isAvalanche -> "⚡"
                        else -> "❄️"
                    },
                    fontSize = 16.sp,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (isZeroApr) {
                    Text(
                        text = "Tất cả các khoản nợ đều 0% lãi suất",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = tokens.onSurface,
                        ),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Cả hai chiến lược mang lại hiệu quả tương đương do không phát sinh tiền lãi. Chạm vào thẻ nợ bên dưới để cập nhật lãi suất thực tế (% APR) của thẻ tín dụng hoặc khoản vay để so sánh tối ưu.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                } else if (isAvalanche) {
                    val savedInterest = comparison?.interestSavedWithAvalanche?.value ?: 0L
                    val title = if (savedInterest > 0L) {
                        "Tiết kiệm hơn ${savedInterest.toVnd()} tiền lãi so với Snowball"
                    } else {
                        "Ưu tiên triệt tiêu tiền lãi phát sinh tối đa"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = FinluxColors.IncomeGreen,
                        ),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Dồn tiền trả khoản nợ có lãi suất APR cao nhất trước để triệt tiêu tiền lãi phát sinh nhanh nhất, ngăn chặn nợ chồng nợ và tiết kiệm tối đa tiền trả lãi.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                } else {
                    val speedDiff = comparison?.firstSettledMonthDifference ?: 0
                    val title = if (speedDiff > 0) {
                        "Xóa sạch chủ nợ đầu tiên nhanh hơn $speedDiff kỳ so với Avalanche"
                    } else {
                        "Xóa sạch khoản nợ nhỏ nhất trước để tạo động lực tâm lý"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = FinluxColors.PrimaryBlue,
                        ),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Ưu tiên dồn tiền trả hết khoản nợ có số dư nhỏ nhất trước để nhanh chóng giảm số lượng chủ nợ, tạo chiến thắng tâm lý sớm và củng cố kỷ luật trả nợ.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

fun formatPayoffDuration(count: Int, isSalaryCycle: Boolean = true): String {
    if (count > 120) return "> 10 năm"
    if (count < 12) {
        return if (isSalaryCycle) "$count kỳ lương" else "$count tháng"
    }
    val years = count / 12
    val months = count % 12
    return if (months == 0) {
        "$years năm"
    } else {
        "$years năm $months tháng"
    }
}


