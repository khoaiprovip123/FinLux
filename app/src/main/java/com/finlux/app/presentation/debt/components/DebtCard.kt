package com.finlux.app.presentation.debt.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.presentation.home.toVnd
import java.time.LocalDate

@Composable
fun DebtCard(
    debt: DebtAccount,
    onPayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onHistoryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val themeColor = colorFromHex(debt.colorHex, tokens.primary)
    val isSettled = debt.isSettled || debt.remainingBalance.value <= 0L
    val today = LocalDate.now().dayOfMonth
    val isDueSoon = !isSettled && (debt.dueDate - today) in 0..5
    val isOverdue = !isSettled && today > debt.dueDate && (today - debt.dueDate) <= 15

    val animatedProgress by animateFloatAsState(
        targetValue = debt.progress,
        animationSpec = tween(800),
        label = "debt_progress",
    )

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onEditClick,
                onLongClick = onDeleteClick,
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Header: Icon + Name & Subtitle + Glass Action Button [Trả nợ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeColor.copy(alpha = 0.14f))
                            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = debtTypeIcon(debt.type),
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = debt.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                            color = tokens.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = debtTypeName(debt.type),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = tokens.onSurfaceVariant,
                            )
                            if (debt.interestRateApr > 0) {
                                Text(
                                    text = "• ${debt.interestRateApr}% APR",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FinluxColors.WarningAmber,
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Right Action Header: History Button + Status Badge or Compact Glass [Trả nợ] Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (onHistoryClick != null) {
                        Surface(
                            shape = CircleShape,
                            color = tokens.surfaceSoft,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onHistoryClick),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Lịch sử trả nợ",
                                    tint = tokens.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }

                    if (isSettled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FinluxColors.IncomeGreen.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, FinluxColors.IncomeGreen.copy(alpha = 0.35f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FinluxColors.IncomeGreen,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "Đã tất toán",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FinluxColors.IncomeGreen,
                                    ),
                                )
                            }
                        }
                    } else {
                        // Refined Glass Button [Trả nợ]
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tokens.primary.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, tokens.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onPayClick),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = tokens.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Trả nợ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = tokens.primary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Body: Remaining Balance & Original Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Dư nợ hiện tại",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = tokens.onSurfaceVariant,
                    )
                    Text(
                        text = debt.remainingBalance.value.toVnd(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (isSettled) FinluxColors.IncomeGreen else tokens.onSurface,
                        ),
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Gốc ban đầu",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = tokens.onSurfaceVariant,
                    )
                    Text(
                        text = debt.totalAmount.value.toVnd(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress Bar (Thin & Smooth)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isSettled) FinluxColors.IncomeGreen else themeColor,
                trackColor = tokens.surfaceSoft,
            )

            Spacer(Modifier.height(8.dp))

            // Footer: Progress % + Minimum Monthly + Due Date Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Đã trả ${(debt.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (isSettled) FinluxColors.IncomeGreen else themeColor,
                        ),
                    )

                    if (!isSettled && debt.minimumPayment.value > 0L) {
                        Text(
                            text = "• Tối thiểu: ${debt.minimumPayment.value.toVnd()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                // Right badges: Reminder chip + Due date badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    if (!isSettled && debt.isReminderEnabled) {
                        val remindDay = if (debt.dueDate > debt.reminderDaysBefore) {
                            debt.dueDate - debt.reminderDaysBefore
                        } else {
                            (30 + debt.dueDate - debt.reminderDaysBefore).coerceAtLeast(1)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tokens.primary.copy(alpha = 0.10f),
                            border = BorderStroke(0.6.dp, tokens.primary.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = tokens.primary,
                                    modifier = Modifier.size(11.dp),
                                )
                                Text(
                                    text = "Nhắc ngày $remindDay",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = tokens.primary,
                                    ),
                                )
                            }
                        }
                    }

                    // Due Date Badge
                    if (!isSettled) {
                        val badgeColor = when {
                            isOverdue -> FinluxColors.ExpenseRed
                            isDueSoon -> FinluxColors.WarningAmber
                            else -> tokens.onSurfaceVariant
                        }
                        val badgeBg = when {
                            isOverdue -> FinluxColors.ExpenseRed.copy(alpha = 0.12f)
                            isDueSoon -> FinluxColors.WarningAmber.copy(alpha = 0.12f)
                            else -> tokens.surfaceSoft
                        }
                        val label = when {
                            isOverdue -> "Quá hạn (${debt.dueDate})"
                            isDueSoon -> "Sắp đến hạn (${debt.dueDate})"
                            else -> "Hạn ngày ${debt.dueDate}"
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    color = badgeColor,
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

fun debtTypeIcon(type: DebtType): ImageVector = when (type) {
    DebtType.CREDIT_CARD -> Icons.Default.CreditCard
    DebtType.BANK_LOAN -> Icons.Default.AccountBalance
    DebtType.PERSONAL_LOAN -> Icons.Default.Handshake
    DebtType.INSTALLMENT -> Icons.Default.ShoppingBag
}

fun debtTypeName(type: DebtType): String = when (type) {
    DebtType.CREDIT_CARD -> "Thẻ tín dụng"
    DebtType.BANK_LOAN -> "Vay ngân hàng"
    DebtType.PERSONAL_LOAN -> "Vay cá nhân"
    DebtType.INSTALLMENT -> "Trả góp / BNPL"
}
