package com.finlux.app.presentation.transaction.prism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxTransactionGroup
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.transaction.DayFinancialSummary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Spending Calendar Heatmap View for Finlux Prism History 2.0
 */
@Composable
fun PrismSpendingCalendarView(
    dailySummaries: Map<LocalDate, DayFinancialSummary>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate?) -> Unit,
    transactions: List<FinanceTransaction>,
    categories: Map<String, Category>,
    wallets: Map<String, Wallet>,
    onTransactionClick: (FinanceTransaction) -> Unit,
    onTransactionLongClick: (FinanceTransaction) -> Unit,
    modifier: Modifier = Modifier,
    zone: java.time.ZoneId = java.time.ZoneId.systemDefault(),
) {
    val tokens = LocalFinluxTokens.current
    var currentMonth by remember { mutableStateOf(YearMonth.now(zone)) }
    val today = remember(zone) { LocalDate.now(zone) }

    val daysOfWeek = remember {
        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    }

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("'Tháng' MM, yyyy", Locale("vi", "VN"))
    }

    // Filter transactions for selected date
    val effectiveSelectedDate = selectedDate ?: today
    val selectedDayTransactions = remember(transactions, effectiveSelectedDate, zone) {
        transactions.filter { it.date.atZone(zone).toLocalDate() == effectiveSelectedDate }
    }
    val selectedDaySummary = dailySummaries[effectiveSelectedDate]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Calendar Container Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Month Selector Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(tokens.surfaceSoft, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Tháng trước",
                            tint = tokens.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = currentMonth.format(monthFormatter),
                            style = FinluxTextStyles.SectionTitle.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )
                    }

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(tokens.surfaceSoft, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Tháng sau",
                            tint = tokens.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Days of Week Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = tokens.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Calendar Grid Calculation
                val firstDayOfMonth = currentMonth.atDay(1)
                val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
                val daysInMonth = currentMonth.lengthOfMonth()

                val totalCells = ((dayOfWeekValue - 1 + daysInMonth + 6) / 7) * 7

                for (row in 0 until (totalCells / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - (dayOfWeekValue - 1) + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(dayNumber)
                                val isSelected = cellDate == effectiveSelectedDate
                                val isToday = cellDate == today
                                val summary = dailySummaries[cellDate]

                                CalendarDayCell(
                                    date = cellDate,
                                    dayNumber = dayNumber,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    summary = summary,
                                    onClick = { onSelectDate(cellDate) },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f).height(48.dp))
                            }
                        }
                    }
                }

                // Heatmap Legend Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(tokens.surfaceSoft, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                        Text("Tiết kiệm/Thu", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp), color = tokens.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Text("Chi vừa phải", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp), color = tokens.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                        Text("Chi nhiều", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp), color = tokens.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. Selected Day Detail Section
        val dayTitleFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("vi", "VN")) }
        val dayTitle = remember(effectiveSelectedDate) {
            when (effectiveSelectedDate) {
                today -> "Hôm nay, ${effectiveSelectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                today.minusDays(1) -> "Hôm qua, ${effectiveSelectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                else -> effectiveSelectedDate.format(dayTitleFormatter).replaceFirstChar { it.uppercase() }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = tokens.surfaceSoft,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = dayTitle,
                        style = FinluxTextStyles.SectionTitle.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = "${selectedDayTransactions.size} giao dịch",
                        style = FinluxTextStyles.Caption.copy(fontSize = 12.sp),
                        color = tokens.onSurfaceVariant,
                    )
                }

                if (selectedDaySummary != null && (selectedDaySummary.totalIncome > 0 || selectedDaySummary.totalExpense > 0)) {
                    val net = selectedDaySummary.netAmount
                    Text(
                        text = if (net >= 0) "+${formatVndAmount(net, isCompact = true)}" else "-${formatVndAmount(-net, isCompact = true)}",
                        style = FinluxTextStyles.CardTitle.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (net >= 0) IncomeGreen else ExpenseRed,
                        ),
                    )
                }
            }
        }

        // 3. Transactions List of the Selected Day
        if (selectedDayTransactions.isEmpty()) {
            FinluxEmptyState(
                title = "Không có giao dịch",
                description = "Chưa có khoản thu chi nào được ghi nhận trong ngày này.",
            )
        } else {
            FinluxTransactionGroup(
                transactions = selectedDayTransactions,
                categories = categories,
                wallets = wallets,
                onTransactionClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick,
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    summary: DayFinancialSummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    val cellBackground = when {
        isSelected -> tokens.primary
        isToday -> tokens.primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday -> tokens.primary
        else -> tokens.onSurface
    }

    val dotColor: Color? = when {
        summary == null || summary.transactionCount == 0 -> null
        summary.netAmount > 0 -> IncomeGreen
        summary.totalExpense > 500_000L -> ExpenseRed
        else -> Color(0xFFF59E0B)
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cellBackground)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, tokens.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                ),
                color = textColor,
            )

            Spacer(Modifier.height(3.dp))

            // Heatmap dot or spacer
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else dotColor),
                )
            } else {
                Spacer(Modifier.size(5.dp))
            }
        }
    }
}
