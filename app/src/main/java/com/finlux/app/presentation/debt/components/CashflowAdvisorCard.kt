package com.finlux.app.presentation.debt.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtCashflowAnalysis
import com.finlux.app.domain.model.PayoffScenario
import com.finlux.app.presentation.home.toShortVnd

@Composable
fun CashflowAdvisorCard(
    analysis: DebtCashflowAnalysis,
    currentExtraPayment: Long,
    onScenarioSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = tokens.surfaceSoft.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, tokens.border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            // Header: AI Icon + Title & Horizontal APR Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(FinluxColors.PrimaryBlue, FinluxColors.PrimaryCyan))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Trợ Lý Dòng Tiền AI",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                            color = tokens.onSurface,
                        )

                        // Clean Horizontal APR Pill
                        if (analysis.weightedApr > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FinluxColors.WarningAmber.copy(alpha = 0.14f),
                                border = BorderStroke(0.6.dp, FinluxColors.WarningAmber.copy(alpha = 0.35f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Percent,
                                        contentDescription = null,
                                        tint = FinluxColors.WarningAmber,
                                        modifier = Modifier.size(10.dp),
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = "APR TB: ${analysis.weightedApr}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = FinluxColors.WarningAmber,
                                        ),
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Phân tích trung bình thu chi 3 tháng gần nhất",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = tokens.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Metrics Summary Grid (3 Pillars)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Thu nhập TB
                MetricPill(
                    title = "Thu nhập TB",
                    value = analysis.averageMonthlyIncome.value.toShortVnd(),
                    valueColor = FinluxColors.IncomeGreen,
                    modifier = Modifier.weight(1f),
                )
                // Chi thiết yếu
                MetricPill(
                    title = "Chi thiết yếu",
                    value = analysis.averageEssentialExpense.value.toShortVnd(),
                    valueColor = FinluxColors.ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
                // Dòng tiền tự do (FCF)
                MetricPill(
                    title = "Dòng tiền FCF",
                    value = if (analysis.isDeficit) "-${(-analysis.freeCashFlow.value).toShortVnd()}" else analysis.freeCashFlow.value.toShortVnd(),
                    valueColor = if (analysis.isDeficit) FinluxColors.WarningAmber else FinluxColors.PrimaryBlue,
                    modifier = Modifier.weight(1.1f),
                )
            }

            // Deficit Warning or Smart Scenarios
            if (analysis.isDeficit) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FinluxColors.WarningAmber.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, FinluxColors.WarningAmber.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = FinluxColors.WarningAmber,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Dòng tiền tự do đang âm/sát trần. Hãy ưu tiên cắt giảm chi tiêu linh hoạt.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = tokens.onSurface,
                            ),
                        )
                    }
                }
            } else if (analysis.scenarios.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Gợi ý mức trả thêm tối ưu (Chạm 1 lần để áp dụng):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = tokens.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    analysis.scenarios.forEach { scenario ->
                        val isSelected = currentExtraPayment == scenario.extraMonthlyAmount.value
                        ScenarioChip(
                            scenario = scenario,
                            isSelected = isSelected,
                            onClick = { onScenarioSelected(scenario.extraMonthlyAmount.value) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tokens.surface.copy(alpha = 0.7f),
        border = BorderStroke(0.6.dp, tokens.border),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = tokens.onSurfaceVariant,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                ),
                color = valueColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ScenarioChip(
    scenario: PayoffScenario,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val accentColor = when (scenario.name) {
        "Thư thái" -> FinluxColors.IncomeGreen
        "Cân bằng" -> FinluxColors.PrimaryBlue
        else -> FinluxColors.PrimaryViolet
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.16f) else tokens.surface.copy(alpha = 0.65f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.8.dp,
            if (isSelected) accentColor else tokens.border,
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = scenario.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.5.sp,
                        color = if (isSelected) accentColor else tokens.onSurface,
                    ),
                    maxLines = 1,
                )
                if (scenario.isRecommended) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinluxColors.WarningAmber,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = "+${scenario.extraMonthlyAmount.value.toShortVnd()}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.5.sp,
                    color = if (isSelected) accentColor else tokens.onSurface,
                ),
                maxLines = 1,
            )
        }
    }
}
