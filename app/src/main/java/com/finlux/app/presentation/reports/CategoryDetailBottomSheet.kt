package com.finlux.app.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
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
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.core.time.FinanceTime
import com.finlux.app.domain.model.FinanceTransaction
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * BottomSheet phân tích chuyên sâu Danh mục (Drill-down Level 2 & 3).
 *
 * Cấp 2: Thông số tổng quan danh mục, so sánh ngân sách, phân bổ các ví.
 * Cấp 3: Bấm vào ví để lọc danh sách giao dịch cấu thành trong kỳ.
 * Cấp 4: Bấm vào giao dịch để mở TransactionDetailSheet xem / sửa / xóa nhanh.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDetailBottomSheet(
    detail: CategorySpendingDetail,
    selectedWalletFilterId: String? = null,
    onSelectWalletFilter: (String?) -> Unit = {},
    onTransactionClick: (FinanceTransaction) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val category = detail.category
    val accentColor = colorFromHex(category.colorHex)
    val isExpense = detail.isExpense
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

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
            // Header: Nhận diện danh mục
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            categoryIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = accentColor,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = (if (isExpense) "Khoản chi tiêu" else "Khoản thu nhập") + " • ${detail.transactionCount} giao dịch",
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

            // 1. KPI Cards (Tổng số tiền, Tỷ trọng, Trung bình / gd)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Tổng số tiền
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else if (isExpense) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else if (isExpense) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isExpense) "Tổng chi tiêu" else "Tổng thu nhập", fontSize = 11.sp, color = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981))
                        Text(
                            "${if (isExpense) "-" else "+"}${formatVndAmount(detail.totalAmount)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Tỷ trọng %
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE0F2FE)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tỷ trọng cơ cấu", fontSize = 11.sp, color = Color(0xFF0284C7))
                        Text(
                            "${(detail.shareOfTotal * 100).roundToInt()}% tổng kỳ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Trung bình mỗi giao dịch
                val avgTx = if (detail.transactionCount > 0) detail.totalAmount / detail.transactionCount else 0L
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Bình quân / gd", fontSize = 11.sp, color = Color(0xFF6B7280))
                        Text(
                            formatVndAmount(avgTx),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 2. So sánh ngân sách (nếu có thiết lập budget)
            detail.budgetItem?.let { budgetItem ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (tokens.isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Tiến độ Ngân sách danh mục",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.onSurface,
                            )
                            if (budgetItem.isOverBudget) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                        Text(
                                            "Vượt ${formatVndAmount(budgetItem.spent - budgetItem.limit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444),
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Còn lại ${formatVndAmount(budgetItem.remaining)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10B981),
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Đã chi ${formatVndAmount(budgetItem.spent)} / ${formatVndAmount(budgetItem.limit)}",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                            )
                            Text(
                                "${(budgetItem.percent * 100).roundToInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (budgetItem.isOverBudget) Color(0xFFEF4444) else tokens.primary,
                            )
                        }

                        LinearProgressIndicator(
                            progress = { budgetItem.percent.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (budgetItem.isOverBudget) Color(0xFFEF4444) else tokens.primary,
                            trackColor = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB),
                        )
                    }
                }
            }

            // 3. Phân bổ chi tiêu / thu nhập theo ví (Wallet Breakdown - Level 2 & 3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Phân bổ theo ví (${detail.walletBreakdown.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    if (selectedWalletFilterId != null) {
                        Text(
                            "Xem tất cả ví",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primary,
                            modifier = Modifier.clickable { onSelectWalletFilter(null) },
                        )
                    }
                }

                if (detail.walletBreakdown.isEmpty()) {
                    Text(
                        "Chưa có phân bổ ví ghi nhận trong kỳ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        detail.walletBreakdown.forEach { share ->
                            val isSelected = selectedWalletFilterId == share.wallet.id
                            val wAccent = colorFromHex(share.wallet.colorHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) tokens.primary.copy(alpha = 0.18f) else if (tokens.isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF9FAFB),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) tokens.primary else if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB),
                                ),
                                modifier = Modifier.clickable {
                                    onSelectWalletFilter(if (isSelected) null else share.wallet.id)
                                },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = tokens.primary,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    } else {
                                        Icon(
                                            walletIcon(share.wallet.type),
                                            contentDescription = null,
                                            tint = wAccent,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    Text(
                                        text = share.wallet.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) tokens.primary else tokens.onSurface,
                                    )
                                    Text(
                                        text = "${formatVndAmount(share.amount)} (${(share.percentage * 100).roundToInt()}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) tokens.primary else Color(0xFF6B7280),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = if (tokens.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB))

            // 4. Danh sách giao dịch cấu thành trong kỳ (Level 3 & 4)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Giao dịch chi tiết (${detail.transactions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Text(
                        "Chạm để xem / sửa",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                    )
                }

                if (detail.transactions.isEmpty()) {
                    Text(
                        "Không có giao dịch nào phù hợp với bộ lọc trong kỳ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        detail.transactions.forEach { tx ->
                            val txDateStr = tx.date.atZone(FinanceTime.VIETNAM_ZONE).format(dateFormatter)
                            val wName = detail.walletBreakdown.find { it.wallet.id == tx.walletId }?.wallet?.name ?: "Ví"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (tokens.isDark) Color.White.copy(alpha = 0.025f) else Color(0xFFF9FAFB),
                                border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFEEEEEE)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTransactionClick(tx) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
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
                                                .size(32.dp)
                                                .background(accentColor.copy(alpha = 0.16f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                categoryIcon(category.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = accentColor,
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = if (tx.note.isNotBlank()) tx.note else category.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = tokens.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "$txDateStr • $wName",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Color(0xFF6B7280),
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = "${if (isExpense) "-" else "+"}${formatVndAmount(tx.amount.value)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981),
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = "Xem chi tiết",
                                            modifier = Modifier.size(12.dp),
                                            tint = tokens.onSurfaceVariant.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
