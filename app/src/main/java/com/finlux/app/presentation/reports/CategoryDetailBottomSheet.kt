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
import com.finlux.app.core.designsystem.theme.FinluxColors
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
        containerColor = tokens.surface,
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
                            color = tokens.textSecondary,
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
            val totalCardColor = if (isExpense) FinluxColors.ExpenseRed else FinluxColors.IncomeGreen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Tổng số tiền
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, totalCardColor.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isExpense) "Tổng chi tiêu" else "Tổng thu nhập", fontSize = 11.sp, color = totalCardColor)
                        Text(
                            "${if (isExpense) "-" else "+"}${formatVndAmount(detail.totalAmount)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = totalCardColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Tỷ trọng %
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, FinluxColors.TransferBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tỷ trọng cơ cấu", fontSize = 11.sp, color = FinluxColors.TransferBlue)
                        Text(
                            "${(detail.shareOfTotal * 100).roundToInt()}% tổng kỳ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinluxColors.TransferBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Trung bình mỗi giao dịch
                val avgTx = if (detail.transactionCount > 0) detail.totalAmount / detail.transactionCount else 0L
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Bình quân / gd", fontSize = 11.sp, color = tokens.textSecondary)
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
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
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
                                    color = tokens.error.copy(alpha = 0.15f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = tokens.error, modifier = Modifier.size(12.dp))
                                        Text(
                                            "Vượt ${formatVndAmount(budgetItem.spent - budgetItem.limit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tokens.error,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Còn lại ${formatVndAmount(budgetItem.remaining)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FinluxColors.IncomeGreen,
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Đã chi ${formatVndAmount(budgetItem.spent)} / ${formatVndAmount(budgetItem.limit)}",
                                fontSize = 12.sp,
                                color = tokens.textSecondary,
                            )
                            Text(
                                "${(budgetItem.percent * 100).roundToInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (budgetItem.isOverBudget) tokens.error else tokens.primary,
                            )
                        }

                        LinearProgressIndicator(
                            progress = { budgetItem.percent.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (budgetItem.isOverBudget) tokens.error else tokens.primary,
                            trackColor = tokens.border,
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
                        color = tokens.textSecondary,
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
                                color = if (isSelected) tokens.primary.copy(alpha = 0.18f) else tokens.surfaceSoft,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) tokens.primary else tokens.border,
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
                                        color = if (isSelected) tokens.primary else tokens.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = tokens.border)

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
                        color = tokens.textSecondary,
                    )
                }

                if (detail.transactions.isEmpty()) {
                    Text(
                        "Không có giao dịch nào phù hợp với bộ lọc trong kỳ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        detail.transactions.forEach { tx ->
                            val txDateStr = tx.date.atZone(FinanceTime.VIETNAM_ZONE).format(dateFormatter)
                            val wName = detail.walletBreakdown.find { it.wallet.id == tx.walletId }?.wallet?.name ?: "Ví"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.surfaceSoft,
                                border = BorderStroke(1.dp, tokens.border),
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
                                                color = tokens.textSecondary,
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
                                            color = totalCardColor,
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
