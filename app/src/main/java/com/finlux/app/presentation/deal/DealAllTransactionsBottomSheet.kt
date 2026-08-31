package com.finlux.app.presentation.deal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.FinluxBottomSheet
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Wallet
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class DealHistoryFilter(val label: String) {
    ALL("Tất cả"),
    OUTLAY("Xuất vốn / Cho vay"),
    RECOVERY("Thu hồi gốc"),
    GAIN("Tiền lời / Lãi"),
    LOSS("Chốt lỗ / Xóa nợ"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealAllTransactionsBottomSheet(
    deals: List<FinancialDeal>,
    transactions: List<FinanceTransaction>,
    wallets: List<Wallet> = emptyList(),
    financeZone: ZoneId = ZoneId.systemDefault(),
    onDismiss: () -> Unit,
    onSelectDeal: ((FinancialDeal) -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    var selectedFilter by remember { mutableStateOf(DealHistoryFilter.ALL) }

    val dealsMap = remember(deals) { deals.associateBy { it.id } }
    val walletsMap = remember(wallets) { wallets.associateBy { it.id } }

    // Lọc tất cả các giao dịch liên quan đến Deal
    val dealTransactions = remember(transactions) {
        transactions.filter { it.dealId != null }
    }

    val filteredTransactions = remember(dealTransactions, selectedFilter) {
        when (selectedFilter) {
            DealHistoryFilter.ALL -> dealTransactions
            DealHistoryFilter.OUTLAY -> dealTransactions.filter { it.dealFlowType == DealFlowType.OUTLAY_CAPITAL }
            DealHistoryFilter.RECOVERY -> dealTransactions.filter { it.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY }
            DealHistoryFilter.GAIN -> dealTransactions.filter { it.dealFlowType == DealFlowType.CAPITAL_GAIN }
            DealHistoryFilter.LOSS -> dealTransactions.filter { it.dealFlowType == DealFlowType.CAPITAL_LOSS }
        }
    }

    // Thống kê tổng hợp
    val totalOutlay = remember(dealTransactions) {
        dealTransactions.filter { it.dealFlowType == DealFlowType.OUTLAY_CAPITAL }.sumOf { it.amount.value }
    }
    val totalRecovery = remember(dealTransactions) {
        dealTransactions.filter { it.dealFlowType == DealFlowType.PRINCIPAL_RECOVERY }.sumOf { it.amount.value }
    }
    val totalGain = remember(dealTransactions) {
        dealTransactions.filter { it.dealFlowType == DealFlowType.CAPITAL_GAIN }.sumOf { it.amount.value }
    }

    val groupedTransactions = remember(filteredTransactions, financeZone) {
        filteredTransactions.groupBy { tx ->
            tx.date.atZone(financeZone).toLocalDate()
        }
    }

    val today = remember(financeZone) { LocalDate.now(financeZone) }
    val yesterday = remember(today) { today.minusDays(1) }

    FinluxBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nhật Ký Dòng Tiền Deal & Khoản Vay",
                        style = FinluxTextStyles.SectionTitle.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tổng ${dealTransactions.size} lượt xuất vốn, thu hồi và lời/lãi",
                        style = FinluxTextStyles.Caption,
                        color = tokens.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tokens.surfaceSoft),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 2. Thẻ Thống Kê Tổng Hợp Dòng Tiền Deal
            FinluxSoftCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Cột 1: Tổng vốn xuất / Cho vay
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Tổng Xuất / Cho Vay",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = tokens.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = formatVndAmount(totalOutlay, isCompact = true),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                            ),
                        )
                    }

                    // Cột 2: Thu hồi gốc
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Gốc Đã Thu Hồi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = tokens.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = formatVndAmount(totalRecovery, isCompact = true),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FinluxColors.IncomeGreen,
                            ),
                        )
                    }

                    // Cột 3: Tiền lời / Lãi
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Tiền Lời / Lãi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = tokens.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = "+${formatVndAmount(totalGain, isCompact = true)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6),
                            ),
                        )
                    }
                }
            }

            // 3. Bộ Lọc Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(DealHistoryFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                ),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.primary.copy(alpha = 0.16f),
                            selectedLabelColor = tokens.primary,
                            containerColor = tokens.surfaceSoft,
                            labelColor = tokens.onSurfaceVariant,
                        ),
                        border = if (isSelected) BorderStroke(1.dp, tokens.primary) else null,
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            }

            // 4. Danh Sách Giao Dịch
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                ) {
                    FinluxEmptyState(
                        title = "Không có giao dịch nào",
                        description = "Chưa có giao dịch dòng tiền thương vụ nào phù hợp với bộ lọc.",
                        actionLabel = null,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    groupedTransactions.forEach { (date, txList) ->
                        item(key = "header_$date") {
                            val headerTitle = when (date) {
                                today -> "Hôm nay, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                yesterday -> "Hôm qua, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                                else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            }

                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = tokens.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp, top = 6.dp, bottom = 2.dp),
                            )
                        }

                        items(
                            items = txList,
                            key = { it.id },
                        ) { transaction ->
                            val deal = transaction.dealId?.let { dealsMap[it] }
                            val wallet = walletsMap[transaction.walletId]

                            DealTransactionHistoryCard(
                                transaction = transaction,
                                deal = deal,
                                wallet = wallet,
                                zone = financeZone,
                                onClick = {
                                    if (deal != null) {
                                        onDismiss()
                                        onSelectDeal?.invoke(deal)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Thẻ giao dịch hiển thị trong Nhật Ký Dòng Tiền Toàn Bộ Deal
 */
@Composable
private fun DealTransactionHistoryCard(
    transaction: FinanceTransaction,
    deal: FinancialDeal?,
    wallet: Wallet?,
    zone: ZoneId,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val flowType = transaction.dealFlowType ?: DealFlowType.OUTLAY_CAPITAL
    val isLending = deal?.category == DealCategory.LENDING

    val (flowLabel, amountPrefix, flowColor, iconBrush) = when (flowType) {
        DealFlowType.OUTLAY_CAPITAL -> {
            val label = if (isLending) "Cho vay thêm" else "Xuất vốn deal"
            Quad(label, "−", Color(0xFFEF4444), Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
        }
        DealFlowType.PRINCIPAL_RECOVERY -> {
            val label = if (isLending) "Thu hồi nợ gốc" else "Thu hồi vốn gốc"
            Quad(label, "+", FinluxColors.IncomeGreen, Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
        }
        DealFlowType.CAPITAL_GAIN -> {
            val label = if (isLending) "Tiền lãi nhận được" else "Lợi nhuận vượt vốn"
            Quad(label, "+", Color(0xFF8B5CF6), Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))))
        }
        DealFlowType.CAPITAL_LOSS -> {
            val label = if (isLending) "Xóa nợ xấu" else "Chốt lỗ đóng deal"
            Quad(label, "−", Color(0xFFF97316), Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))))
        }
    }

    val displayAmount = amountPrefix + formatVndAmount(transaction.amount.value).replace("đ", "₫")
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeText = remember(transaction.date, zone) {
        transaction.date.atZone(zone).format(timeFormatter)
    }

    val dealTitle = deal?.title ?: "Thương vụ đã đóng"
    val subInfo = buildString {
        append(flowLabel)
        if (wallet != null) append(" • ${wallet.name}")
        append(" • $timeText")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (tokens.isDark) tokens.surfaceSoft else Color.White,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (flowType) {
                        DealFlowType.OUTLAY_CAPITAL -> if (isLending) Icons.Default.Handshake else Icons.AutoMirrored.Filled.TrendingDown
                        DealFlowType.PRINCIPAL_RECOVERY -> Icons.Default.Savings
                        DealFlowType.CAPITAL_GAIN -> Icons.AutoMirrored.Filled.TrendingUp
                        DealFlowType.CAPITAL_LOSS -> Icons.AutoMirrored.Filled.ReceiptLong
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = dealTitle,
                        style = FinluxTextStyles.CardTitle.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    // Badge category
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLending) Color(0xFFF59E0B).copy(alpha = 0.14f) else tokens.primary.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = if (isLending) "Cho vay" else "Đầu tư",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (isLending) Color(0xFFD97706) else tokens.primary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                        )
                    }
                }

                Text(
                    text = subInfo,
                    style = FinluxTextStyles.Caption.copy(fontSize = 11.5.sp),
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Amount
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = flowColor,
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
