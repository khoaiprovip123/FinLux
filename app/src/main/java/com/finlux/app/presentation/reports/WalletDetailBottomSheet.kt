package com.finlux.app.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * BottomSheet phân tích chuyên sâu Ví tài chính (Drill-down Level 2 & 3).
 *
 * Cấp 2: Tổng quan dòng tiền ví, phân bổ theo danh mục.
 * Cấp 3: Bấm vào danh mục để lọc danh sách giao dịch bên dưới.
 * Cấp 4: Bấm vào giao dịch bất kỳ để mở TransactionDetailSheet xem / sửa / xóa nhanh.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WalletDetailBottomSheet(
    detail: WalletSpendingDetail,
    isFilterActive: Boolean,
    selectedCategoryFilterId: String? = null,
    onSelectCategoryFilter: (String?) -> Unit = {},
    onTransactionClick: (FinanceTransaction) -> Unit = {},
    onFilterWallet: () -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val wallet = detail.wallet
    val accent = colorFromHex(wallet.colorHex)
    val typeName = when (wallet.type) {
        WalletType.CASH -> "Tiền mặt"
        WalletType.BANK -> "Ngân hàng"
        WalletType.EWALLET -> "Ví điện tử"
        WalletType.CARD -> "Thẻ tín dụng"
        WalletType.INVESTMENT -> "Đầu tư / Tiết kiệm"
        WalletType.OTHER -> "Khác"
    }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    val displayedTransactions = if (selectedCategoryFilterId != null) {
        detail.transactions.filter { it.categoryId == selectedCategoryFilterId }
    } else {
        detail.transactions
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = tokens.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(2.dp),
            ) {
                Box(Modifier.size(width = 36.dp, height = 4.dp))
            }
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Nhận diện ví
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accent.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = accent,
                        )
                    }
                    Column {
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "$typeName • Số dư: ${formatVndAmount(detail.balance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                    )
                }
            }

            // 4 KPI cards (Thu nhập, Chi tiêu, Chuyển tiền, Biến động ví)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Thu nhập
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFDCFCE7)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tổng thu nhập", fontSize = 11.sp, color = Color(0xFF10B981))
                            Text(
                                "+${formatVndAmount(detail.incomeInPeriod)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Chi tiêu
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFFEE2E2)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tổng chi tiêu", fontSize = 11.sp, color = Color(0xFFEF4444))
                            Text(
                                "-${formatVndAmount(detail.expenseInPeriod)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Luân chuyển tiền (Chuyển đi / Nhận chuyển)
                    val hasTransfer = detail.transferOutInPeriod > 0 || detail.transferInInPeriod > 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF0F9FF),
                        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE0F2FE)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Chuyển tiền", fontSize = 11.sp, color = Color(0xFF0284C7))
                            if (hasTransfer) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (detail.transferOutInPeriod > 0) {
                                        Text(
                                            "-${formatVndAmount(detail.transferOutInPeriod)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF97316),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (detail.transferInInPeriod > 0) {
                                        Text(
                                            "+${formatVndAmount(detail.transferInInPeriod)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0EA5E9),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "0 ₫",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }

                    // Biến động số dư ví
                    val isPositiveChange = detail.netWalletChange >= 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else if (isPositiveChange) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else if (isPositiveChange) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Biến động ví", fontSize = 11.sp, color = if (isPositiveChange) Color(0xFF10B981) else Color(0xFFEF4444))
                            Text(
                                "${if (isPositiveChange) "+" else ""}${formatVndAmount(detail.netWalletChange)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositiveChange) Color(0xFF10B981) else Color(0xFFEF4444),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Thẻ tóm tắt biến động số dư thực tế
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tổng kết dòng tiền thực tế của ví",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiền vào (Thu nhập + Nhận chuyển)", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Text(
                            "+${formatVndAmount(detail.totalMoneyIn)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiền ra (Chi tiêu + Chuyển đi)", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Text(
                            "-${formatVndAmount(detail.totalMoneyOut)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                        )
                    }
                    HorizontalDivider(color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biến động số dư ví trong kỳ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                        Text(
                            "${if (detail.netWalletChange >= 0) "+" else ""}${formatVndAmount(detail.netWalletChange)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (detail.netWalletChange >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        )
                    }
                }
            }

            // Tỷ trọng chi tiêu card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tỷ trọng trên tổng chi toàn app", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                        Text(
                            "${(detail.expenseShareOfTotal * 100).roundToInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primary,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { detail.expenseShareOfTotal.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = tokens.primary,
                        trackColor = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB),
                    )
                }
            }

            // Phân bổ chi tiêu theo danh mục (Level 2 & 3: Bấm danh mục để lọc)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Phân bổ chi tiêu (${detail.expensesByCategory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                if (selectedCategoryFilterId != null) {
                    Text(
                        "Xem tất cả danh mục",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primary,
                        modifier = Modifier.clickable { onSelectCategoryFilter(null) },
                    )
                }
            }

            if (detail.expensesByCategory.isEmpty()) {
                Text(
                    "Ví này chưa có khoản chi tiêu nào trong kỳ đã chọn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.expensesByCategory.forEach { catExp ->
                        val cat = catExp.category
                        val catColor = colorFromHex(cat?.colorHex ?: "#6B7280")
                        val catPercent = (catExp.percentage * 100).roundToInt()
                        val isCatSelected = selectedCategoryFilterId == cat?.id

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCatSelected) tokens.primary.copy(alpha = 0.16f) else if (tokens.isDark) Color.White.copy(alpha = 0.03f) else Color(0xFFFAFAFA),
                            border = BorderStroke(
                                if (isCatSelected) 1.5.dp else 1.dp,
                                if (isCatSelected) tokens.primary else if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFEEEEEE),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCategoryFilter(if (isCatSelected) null else cat?.id)
                                },
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(catColor.copy(alpha = 0.16f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                categoryIcon(cat?.icon.orEmpty()),
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp),
                                                tint = catColor,
                                            )
                                        }
                                        Text(
                                            cat?.name ?: "Khác",
                                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isCatSelected) tokens.primary else tokens.onSurface,
                                        )
                                        if (isCatSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = tokens.primary,
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatVndAmount(catExp.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFEF4444),
                                        )
                                        Text(
                                            "$catPercent% của ví",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF6B7280),
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { catExp.percentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp)),
                                    color = if (isCatSelected) tokens.primary else catColor,
                                    trackColor = if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE5E7EB),
                                )
                            }
                        }
                    }
                }
            }

            // Nguồn thu nạp vào ví nếu có
            if (detail.incomeByCategory.isNotEmpty()) {
                Text(
                    "Nguồn thu nạp vào ví (${detail.incomeByCategory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.incomeByCategory.forEach { catInc ->
                        val cat = catInc.category
                        val catColor = colorFromHex(cat?.colorHex ?: "#10B981")
                        val isCatSelected = selectedCategoryFilterId == cat?.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCatSelected) tokens.primary.copy(alpha = 0.16f) else if (tokens.isDark) Color.White.copy(alpha = 0.03f) else Color(0xFFFAFAFA),
                            border = BorderStroke(
                                if (isCatSelected) 1.5.dp else 1.dp,
                                if (isCatSelected) tokens.primary else if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFEEEEEE),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCategoryFilter(if (isCatSelected) null else cat?.id)
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(catColor.copy(alpha = 0.16f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            categoryIcon(cat?.icon.orEmpty()),
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = catColor,
                                        )
                                    }
                                    Text(
                                        cat?.name ?: "Khác",
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isCatSelected) tokens.primary else tokens.onSurface,
                                    )
                                    if (isCatSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = tokens.primary,
                                        )
                                    }
                                }
                                Text(
                                    "+${formatVndAmount(catInc.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF10B981),
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB))

            // Lịch sử giao dịch ví trong kỳ (Level 3 & 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Lịch sử giao dịch (${displayedTransactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Text(
                    "Chạm để xem / sửa",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                )
            }

            if (displayedTransactions.isEmpty()) {
                Text(
                    "Không có giao dịch phát sinh từ ví này phù hợp với bộ lọc trong kỳ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    displayedTransactions.take(30).forEach { tx ->
                        val isIncome = tx.type == TransactionType.INCOME
                        val isTransferOut = tx.type == TransactionType.TRANSFER_OUT
                        val isTransferIn = tx.type == TransactionType.TRANSFER_IN
                        val isTransfer = isTransferOut || isTransferIn
                        val txDateStr = tx.date.atZone(FinanceTime.VIETNAM_ZONE).format(dateFormatter)

                        val amountColor = when {
                            isIncome -> Color(0xFF10B981)
                            isTransferIn -> Color(0xFF0EA5E9)
                            isTransferOut -> Color(0xFFF97316)
                            else -> Color(0xFFEF4444)
                        }
                        val prefix = when {
                            isIncome || isTransferIn -> "+"
                            else -> "-"
                        }
                        val defaultLabel = when {
                            isTransferOut -> "Chuyển tiền sang ví khác"
                            isTransferIn -> "Nhận tiền chuyển từ ví khác"
                            isIncome -> "Thu nhập"
                            else -> "Chi tiêu"
                        }
                        val txIcon = when {
                            isTransfer -> Icons.Default.SwapHoriz
                            isIncome -> Icons.Default.ArrowDownward
                            else -> Icons.Default.ArrowUpward
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (tokens.isDark) Color.White.copy(alpha = 0.025f) else Color(0xFFF9FAFB),
                            border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFEEEEEE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransactionClick(tx) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(amountColor.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            txIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = amountColor,
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (tx.note.isNotBlank()) tx.note else defaultLabel,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.5.sp,
                                            color = tokens.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = txDateStr,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF6B7280),
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "$prefix${formatVndAmount(tx.amount.value)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = amountColor,
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Xem chi tiết",
                                        modifier = Modifier.size(11.dp),
                                        tint = tokens.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Nút bấm lọc toàn app theo ví này
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (isFilterActive) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onClearFilter() },
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("Bỏ lọc ví này", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFilterWallet() },
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("Lọc toàn bộ báo cáo theo ví này", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tokens.onHero)
                        }
                    }
                }
            }
        }
    }
}
