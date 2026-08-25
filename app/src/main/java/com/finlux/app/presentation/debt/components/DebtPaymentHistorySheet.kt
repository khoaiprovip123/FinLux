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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtPaymentHistory
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtPaymentHistorySheet(
    debts: List<DebtAccount>,
    wallets: List<Wallet>,
    paymentHistory: List<DebtPaymentHistory>,
    initialDebtId: String? = null,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedDebtId by remember(initialDebtId) { mutableStateOf(initialDebtId) }

    val filteredHistory = remember(paymentHistory, selectedDebtId) {
        if (selectedDebtId.isNullOrBlank()) {
            paymentHistory.sortedByDescending { it.paymentDate }
        } else {
            paymentHistory.filter { it.debtId == selectedDebtId }.sortedByDescending { it.paymentDate }
        }
    }

    val totalPaid = remember(filteredHistory) { filteredHistory.sumOf { it.amount.value } }
    val totalPrincipal = remember(filteredHistory) { filteredHistory.sumOf { it.principalPaid.value } }
    val totalInterest = remember(filteredHistory) { filteredHistory.sumOf { it.interestPaid.value } }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FinluxBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = FinluxBlue,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Lịch sử thanh toán nợ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            ),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "Đối soát các lần trả nợ & phân bổ lãi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = tokens.onSurfaceVariant,
                        )
                    }
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

            // 2. Summary Hero Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (tokens.isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "TỔNG TIỀN ĐÃ THANH TOÁN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = tokens.onSurfaceVariant,
                    )
                    Text(
                        text = totalPaid.toVnd(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = Color(0xFF10B981),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Giảm gốc
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tokens.surface,
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Đã giảm gốc",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = tokens.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = totalPrincipal.toVnd(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                    ),
                                    color = FinluxBlue,
                                )
                            }
                        }

                        // Tiền lãi
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tokens.surface,
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Tiền lãi đã trả",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = tokens.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = totalInterest.toVnd(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                    ),
                                    color = Color(0xFFF43F5E),
                                )
                            }
                        }
                    }
                }
            }

            // 3. Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    val isSelected = selectedDebtId == null
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDebtId = null },
                        label = {
                            Text(
                                text = "Tất cả (${paymentHistory.size})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FinluxBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }

                items(debts) { debt ->
                    val isSelected = selectedDebtId == debt.id
                    val debtCount = paymentHistory.count { it.debtId == debt.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDebtId = if (isSelected) null else debt.id },
                        label = {
                            Text(
                                text = "${debt.name} ($debtCount)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorFromHex(debt.colorHex),
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            // 4. Payment History List
            if (filteredHistory.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tokens.surfaceSoft,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = tokens.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "Chưa có lịch sử thanh toán",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredHistory) { item ->
                        val targetDebt = debts.firstOrNull { it.id == item.debtId }
                        val sourceWallet = wallets.firstOrNull { it.id == item.walletId }
                        val debtColor = targetDebt?.let { colorFromHex(it.colorHex) } ?: FinluxBlue
                        val formattedDate = item.paymentDate.atZone(ZoneId.systemDefault()).format(dateFormatter)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tokens.surfaceSoft,
                            border = BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = debtColor.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = targetDebt?.type?.let { debtTypeIcon(it) } ?: Icons.Default.Payments,
                                                    contentDescription = null,
                                                    tint = debtColor,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = targetDebt?.name ?: "Khoản nợ",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp,
                                                ),
                                                color = tokens.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                color = tokens.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    Text(
                                        text = item.amount.value.toVnd(),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.5.sp,
                                        ),
                                        color = Color(0xFF10B981),
                                    )
                                }

                                // Principal & Interest Allocation Tags
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = FinluxBlue.copy(alpha = 0.10f),
                                    ) {
                                        Text(
                                            text = "Gốc: ${item.principalPaid.value.toVnd()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = FinluxBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        )
                                    }

                                    if (item.interestPaid.value > 0L) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF43F5E).copy(alpha = 0.10f),
                                        ) {
                                            Text(
                                                text = "Lãi: ${item.interestPaid.value.toVnd()}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = Color(0xFFF43F5E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            )
                                        }
                                    }

                                    if (sourceWallet != null) {
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = sourceWallet.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = tokens.onSurfaceVariant,
                                            ),
                                        )
                                    }
                                }

                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = "“${item.note}”",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        ),
                                        color = tokens.onSurfaceVariant,
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
