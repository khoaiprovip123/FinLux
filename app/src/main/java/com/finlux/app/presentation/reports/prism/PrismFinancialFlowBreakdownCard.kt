package com.finlux.app.presentation.reports.prism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.presentation.reports.ReportsUiState

@Composable
internal fun PrismFinancialFlowBreakdownCard(
    state: ReportsUiState,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val flow = state.financialFlowBreakdown
    val operating = state.operatingSummary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Cơ cấu dòng tiền trong kỳ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface,
                )
                Text(
                    text = "Tách chi tiêu thực khỏi tiết kiệm, trả gốc nợ, đầu tư và chuyển nội bộ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowMetricTile(
                    label = "Thu hoạt động",
                    amount = operating.income.value,
                    prefix = "+",
                    valueColor = FinluxColors.IncomeGreen,
                    modifier = Modifier.weight(1f),
                )
                FlowMetricTile(
                    label = "Chi hoạt động",
                    amount = operating.expense.value,
                    prefix = "-",
                    valueColor = FinluxColors.ExpenseRed,
                    modifier = Modifier.weight(1f),
                )
            }

            FlowMetricTile(
                label = "Thặng dư hoạt động",
                amount = operating.net,
                prefix = if (operating.net >= 0L) "+" else "",
                valueColor = if (operating.net >= 0L) FinluxColors.IncomeGreen else FinluxColors.ExpenseRed,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = tokens.border)

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (flow.goalAllocation > 0L) {
                    FlowDetailRow("Phân bổ vào mục tiêu", -flow.goalAllocation)
                }
                if (flow.goalRelease > 0L) {
                    FlowDetailRow("Rút từ mục tiêu", flow.goalRelease)
                }
                if (flow.debtPrincipalOutflow > 0L) {
                    FlowDetailRow("Trả gốc nợ", -flow.debtPrincipalOutflow)
                }
                if (flow.debtInterestExpense > 0L) {
                    FlowDetailRow(
                        label = "Lãi nợ · đã tính trong chi hoạt động",
                        signedAmount = -flow.debtInterestExpense,
                        emphasized = false,
                    )
                }

                val investingOut = flow.investmentOutlay + flow.lendingOutlay
                if (investingOut > 0L) {
                    FlowDetailRow("Xuất vốn đầu tư / cho vay", -investingOut)
                }

                val principalRecovery =
                    flow.investmentPrincipalRecovery + flow.lendingPrincipalRecovery
                if (principalRecovery > 0L) {
                    FlowDetailRow("Thu hồi vốn / gốc cho vay", principalRecovery)
                }

                if (flow.dealGain > 0L) {
                    FlowDetailRow("Lãi thương vụ", flow.dealGain)
                }
                if (flow.dealLoss > 0L) {
                    FlowDetailRow(
                        label = "Lỗ thương vụ · ghi nhận P&L",
                        signedAmount = -flow.dealLoss,
                        emphasized = false,
                    )
                }

                if (flow.transferIn > 0L || flow.transferOut > 0L) {
                    FlowDetailRow(
                        label = "Chuyển nội bộ · vào / ra",
                        valueText = "+${formatVndAmount(flow.transferIn)} / -${formatVndAmount(flow.transferOut)}",
                        valueColor = FinluxColors.TransferBlue,
                    )
                }
            }

            HorizontalDivider(color = tokens.border)

            FlowDetailRow(
                label = "Biến động số dư thực tế",
                signedAmount = state.closingBalance - state.openingBalance,
                emphasized = true,
            )
            Text(
                text = "Số dư đầu kỳ ${formatVndAmount(state.openingBalance)} → cuối kỳ ${formatVndAmount(state.closingBalance)}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FlowMetricTile(
    label: String,
    amount: Long,
    prefix: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.border),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.onSurfaceVariant,
            )
            Text(
                text = "$prefix${formatVndAmount(amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun FlowDetailRow(
    label: String,
    signedAmount: Long = 0L,
    valueText: String? = null,
    valueColor: androidx.compose.ui.graphics.Color? = null,
    emphasized: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val positive = signedAmount >= 0L
    val resolvedColor = valueColor ?: when {
        signedAmount > 0L -> FinluxColors.IncomeGreen
        signedAmount < 0L -> FinluxColors.ExpenseRed
        else -> tokens.onSurfaceVariant
    }
    val resolvedText = valueText ?: buildString {
        if (signedAmount > 0L) append("+")
        append(formatVndAmount(signedAmount))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasized) tokens.onSurface else tokens.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = resolvedText,
            style = if (emphasized) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = resolvedColor,
        )
    }
}
