package com.finlux.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.home.HomeViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Quick Add Hub Modal Sheet matching the mockup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onIncome: () -> Unit,
    onExpense: () -> Unit,
    onTransfer: () -> Unit,
    onReceipt: () -> Unit,
    onGoal: () -> Unit,
    onViewAllTransactions: (() -> Unit)? = null,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val homeState = homeViewModel.state.collectAsStateWithLifecycle().value
    val recentTransactions = homeState.transactions.take(3)
    val categoriesMap = homeState.categories.associateBy { it.id }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Top Header: Title + Subtitle + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Tạo giao dịch",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = "Chọn loại giao dịch bạn muốn thực hiện",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFF6B7280),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // 2. 2x2 Bento Action Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Thêm thu
                QuickActionBentoCard(
                    title = "Thêm thu",
                    subtitle = "Dòng tiền vào",
                    icon = Icons.AutoMirrored.Filled.CallReceived,
                    iconBgColor = Color(0xFFDCFCE7),
                    iconTintColor = Color(0xFF10B981),
                    cardBgColor = if (tokens.isDark) Color(0xFF1E2E2A) else Color(0xFFF0FDF4),
                    borderColor = Color(0xFF10B981).copy(alpha = 0.20f),
                    modifier = Modifier.weight(1f),
                    onClick = onIncome,
                )

                // Thêm chi
                QuickActionBentoCard(
                    title = "Thêm chi",
                    subtitle = "Dòng tiền ra",
                    icon = Icons.Default.CallMade,
                    iconBgColor = Color(0xFFFFE4E6),
                    iconTintColor = Color(0xFFF43F5E),
                    cardBgColor = if (tokens.isDark) Color(0xFF2E1E24) else Color(0xFFFFF1F2),
                    borderColor = Color(0xFFF43F5E).copy(alpha = 0.20f),
                    modifier = Modifier.weight(1f),
                    onClick = onExpense,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Chuyển tiền
                QuickActionBentoCard(
                    title = "Chuyển tiền",
                    subtitle = "Giữa các ví",
                    icon = Icons.Default.SwapHoriz,
                    iconBgColor = Color(0xFFDBEAFE),
                    iconTintColor = Color(0xFF3B82F6),
                    cardBgColor = if (tokens.isDark) Color(0xFF1E2438) else Color(0xFFEFF6FF),
                    borderColor = Color(0xFF3B82F6).copy(alpha = 0.20f),
                    modifier = Modifier.weight(1f),
                    onClick = onTransfer,
                )

                // Scan hóa đơn
                QuickActionBentoCard(
                    title = "Scan hóa đơn",
                    subtitle = "Nhập khoản chi",
                    icon = Icons.Default.DocumentScanner,
                    iconBgColor = Color(0xFFF3E8FF),
                    iconTintColor = Color(0xFF9333EA),
                    cardBgColor = if (tokens.isDark) Color(0xFF261E38) else Color(0xFFFAF5FF),
                    borderColor = Color(0xFF9333EA).copy(alpha = 0.20f),
                    modifier = Modifier.weight(1f),
                    onClick = onReceipt,
                )
            }

            // 3. Thêm mục tiêu (Full Width Banner)
            QuickActionFullBanner(
                title = "Thêm mục tiêu",
                subtitle = "Lập kế hoạch tích lũy",
                icon = Icons.Default.TrackChanges,
                iconBgColor = Color(0xFFEDE9FE),
                iconTintColor = Color(0xFF7C3AED),
                cardBgColor = if (tokens.isDark) Color(0xFF201B3E) else Color(0xFFF5F3FF),
                borderColor = Color(0xFF7C3AED).copy(alpha = 0.18f),
                onClick = onGoal,
            )

            // 4. Giao dịch gần đây Section
            if (recentTransactions.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Giao dịch gần đây",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )

                    Text(
                        text = "Xem tất cả",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFF3B5DF8),
                        modifier = Modifier.clickable {
                            onDismiss()
                            onViewAllTransactions?.invoke()
                        },
                    )
                }

                // Recent Transaction Items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    recentTransactions.forEach { tx ->
                        val category = tx.categoryId?.let { categoriesMap[it] }
                        QuickAddRecentTransactionRow(
                            transaction = tx,
                            category = category,
                        )
                    }
                }
            }

            // 5. Footer Lightbulb Tip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (tokens.isDark) Color(0xFF201B3E) else Color(0xFFF5F3FF),
                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Bạn cũng có thể nhấn giữ nút + để tạo nhanh",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = if (tokens.isDark) Color(0xFFDDD6FE) else Color(0xFF6D28D9),
                    )
                }
            }
        }
    }
}

/**
 * 2x2 Bento Action Card
 */
@Composable
private fun QuickActionBentoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    cardBgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = Color(0xFF6B7280),
                )
            }
        }
    }
}

/**
 * Full Width Banner Action Card
 */
@Composable
private fun QuickActionFullBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    cardBgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTintColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = Color(0xFF6B7280),
                )
            }
        }
    }
}

/**
 * Recent Transaction Item row inside QuickAddSheet
 */
@Composable
private fun QuickAddRecentTransactionRow(
    transaction: FinanceTransaction,
    category: Category?,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = category?.let { colorFromHex(it.colorHex) } ?: if (isIncome) Color(0xFF10B981) else Color(0xFFF43F5E)
    val title = transaction.note.ifBlank { category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu" }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeText = remember(transaction.date) {
        timeFormatter.format(transaction.date.atZone(ZoneId.systemDefault()))
    }
    val subtitleText = "$timeText • ${category?.name ?: "Khác"}"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category?.let { categoryIcon(it.icon) } ?: Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    color = Color(0xFF9CA3AF),
                )
            }

            Spacer(Modifier.width(8.dp))

            // Amount & Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = (if (isIncome) "+" else "-") + formatVndAmount(transaction.amount.value),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFEF4444),
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }
}
