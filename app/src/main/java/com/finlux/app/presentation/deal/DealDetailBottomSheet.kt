package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DealFlowType
import com.finlux.app.domain.model.DealStatus
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailBottomSheet(
    deal: FinancialDeal,
    transactions: List<FinanceTransaction>,
    onDismiss: () -> Unit,
    onAddOutlay: () -> Unit,
    onAddInflow: () -> Unit,
    onCloseWithLoss: () -> Unit,
    onDelete: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStopLossConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                        ),
                    )
                    if (deal.description.isNotBlank()) {
                        Text(
                            text = deal.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = tokens.textSecondary,
                            ),
                        )
                    }
                }

                // Badge Status
                val statusBg = when (deal.status) {
                    DealStatus.ACTIVE -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                    DealStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.15f)
                    DealStatus.CANCELLED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                }
                val statusText = when (deal.status) {
                    DealStatus.ACTIVE -> "Đang chạy"
                    DealStatus.COMPLETED -> "Đã hoàn tất"
                    DealStatus.CANCELLED -> "Đã hủy"
                }
                val statusColor = when (deal.status) {
                    DealStatus.ACTIVE -> Color(0xFF2563EB)
                    DealStatus.COMPLETED -> Color(0xFF059669)
                    DealStatus.CANCELLED -> Color(0xFFDC2626)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        ),
                    )
                }
            }

            // Metrics Grid Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surfaceSoft),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Row 1: Vốn đã chi & Vốn đã thu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "Tổng vốn đã xuất",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            Text(
                                text = deal.totalCapitalOutlay.value.toVnd(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textPrimary,
                                ),
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Vốn gốc đã thu hồi",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            Text(
                                text = deal.totalRecovered.value.toVnd(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                ),
                            )
                        }
                    }

                    // Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { deal.recoveryProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF10B981),
                            trackColor = tokens.border,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Tiến độ hoàn vốn",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            Text(
                                text = "${(deal.recoveryProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.textPrimary,
                                ),
                            )
                        }
                    }

                    HorizontalDivider(color = tokens.border, thickness = 0.5.dp)

                    // Row 2: Vốn còn lại & Lợi nhuận ròng & ROI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "Vốn chưa thu hồi",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            Text(
                                text = deal.remainingCapital.value.toVnd(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (deal.remainingCapital.value > 0) Color(0xFFF59E0B) else tokens.textSecondary,
                                ),
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lợi nhuận ròng",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            val profitVal = deal.netProfitLoss.value
                            Text(
                                text = if (profitVal > 0) "+${profitVal.toVnd()}" else profitVal.toVnd(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        profitVal > 0 -> Color(0xFF10B981)
                                        profitVal < 0 -> Color(0xFFEF4444)
                                        else -> tokens.textSecondary
                                    },
                                ),
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tỷ suất ROI",
                                style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                            )
                            val roi = deal.roiPercentage
                            Text(
                                text = String.format(java.util.Locale.US, "%+.1f%%", roi),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (roi >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                ),
                            )
                        }
                    }
                }
            }

            // Dòng thời gian giao dịch (Transaction Timeline)
            Text(
                text = "Lịch sử dòng tiền thương vụ (${transactions.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                ),
            )

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Chưa có giao dịch phát sinh cho thương vụ này",
                        style = MaterialTheme.typography.bodySmall.copy(color = tokens.textSecondary),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(transactions) { tx ->
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                        val flowLabel = when (tx.dealFlowType) {
                            DealFlowType.OUTLAY_CAPITAL -> "Xuất vốn"
                            DealFlowType.PRINCIPAL_RECOVERY -> "Hoàn gốc"
                            DealFlowType.CAPITAL_GAIN -> "Lãi ròng"
                            DealFlowType.CAPITAL_LOSS -> "Lỗ chốt deal"
                            null -> tx.type.name
                        }
                        val flowColor = when (tx.dealFlowType) {
                            DealFlowType.OUTLAY_CAPITAL -> Color(0xFFEF4444)
                            DealFlowType.PRINCIPAL_RECOVERY -> Color(0xFF2563EB)
                            DealFlowType.CAPITAL_GAIN -> Color(0xFF10B981)
                            DealFlowType.CAPITAL_LOSS -> Color(0xFFDC2626)
                            null -> tokens.textPrimary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(tokens.surfaceSoft)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = flowLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = flowColor,
                                        ),
                                    )
                                    Text(
                                        text = "• ${dateFormatter.format(tx.date)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = tokens.textSecondary,
                                            fontSize = 11.sp,
                                        ),
                                    )
                                }
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = tx.note,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = tokens.textSecondary,
                                            fontSize = 12.sp,
                                        ),
                                    )
                                }
                            }
                            Text(
                                text = tx.amount.value.toVnd(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = flowColor,
                                ),
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onAddOutlay,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.surfaceSoft,
                            contentColor = tokens.textPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Xuất Thêm Vốn", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onAddInflow,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Rounded.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thu Hồi / Lời", fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (deal.status == DealStatus.ACTIVE && deal.remainingCapital.value > 0L) {
                        OutlinedButton(
                            onClick = { showStopLossConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        ) {
                            Text("Chốt Lỗ & Đóng", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary),
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xóa Deal", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa thương vụ '${deal.title}' không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Xóa", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    // Dialog xác nhận chốt lỗ
    if (showStopLossConfirm) {
        AlertDialog(
            onDismissRequest = { showStopLossConfirm = false },
            title = { Text("Chốt Lỗ & Đóng Thương Vụ") },
            text = {
                Text("Khoản vốn chưa thu hồi (${deal.remainingCapital.value.toVnd()}) sẽ được ghi nhận là Khoản Lỗ Đầu Tư vào Báo cáo Chi tiêu của bạn và đóng thương vụ này.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopLossConfirm = false
                        onCloseWithLoss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                ) {
                    Text("Xác Nhận Chốt Lỗ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopLossConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }
}
