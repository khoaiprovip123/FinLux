package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
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
import com.finlux.app.domain.model.DealCategory
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
    onEditDeal: () -> Unit = {},
    onAddOutlay: () -> Unit,
    onAddInflow: () -> Unit,
    onCloseWithLoss: () -> Unit,
    onCloseDeal: () -> Unit = {},
    onRevertStopLoss: () -> Unit = {},
    onReopenDeal: () -> Unit = {},
    onDelete: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStopLossConfirm by remember { mutableStateOf(false) }
    var showCloseDealConfirm by remember { mutableStateOf(false) }
    var showRevertStopLossConfirm by remember { mutableStateOf(false) }
    var showReopenDealConfirm by remember { mutableStateOf(false) }

    val isLending = deal.category == DealCategory.LENDING

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header: Title, Category Badge, Status & Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = deal.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary,
                            ),
                        )

                        // Badge Category
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isLending) Color(0xFFF59E0B).copy(alpha = 0.14f) else tokens.primary.copy(alpha = 0.14f),
                        ) {
                            Text(
                                text = if (isLending) "Cho vay" else "Đầu tư",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = if (isLending) Color(0xFFD97706) else tokens.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    if (deal.description.isNotBlank()) {
                        Text(
                            text = deal.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = tokens.textSecondary,
                            ),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Badge Status
                    val statusBg = when (deal.status) {
                        DealStatus.ACTIVE -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                        DealStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.15f)
                        DealStatus.CANCELLED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    }
                    val statusText = when (deal.status) {
                        DealStatus.ACTIVE -> if (isLending) "Đang vay" else "Đang chạy"
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
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                            ),
                        )
                    }

                    // Nút Sửa Deal (Icon Cây Bút)
                    IconButton(
                        onClick = onEditDeal,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(tokens.surfaceSoft),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = tokens.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // Metrics Grid Card (Phân nhánh theo Category: Đầu Tư vs Cho Vay)
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
                                text = if (isLending) "Tổng nợ gốc đã cho vay" else "Tổng vốn đã xuất",
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
                                text = if (isLending) "Nợ gốc đã thu hồi" else "Vốn gốc đã thu hồi",
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
                                text = if (isLending) "Tiến độ thu hồi nợ" else "Tiến độ hoàn vốn",
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

                    // Row 2: Chỉ số chi tiết
                    if (isLending) {
                        // Cho vay: Dư nợ còn lại & Tiền lãi nhận được & Trạng thái
                        val errorColor = MaterialTheme.colorScheme.error
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Dư nợ gốc còn lại",
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
                                val profitVal = deal.netProfitLoss.value
                                val isLoss = profitVal < 0
                                Text(
                                    text = if (isLoss) "Mất vốn / Xóa nợ" else "Tiền lãi nhận được",
                                    style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                                )
                                Text(
                                    text = if (profitVal > 0) "+${profitVal.toVnd()}" else profitVal.toVnd(),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (profitVal > 0) Color(0xFF8B5CF6) else if (isLoss) errorColor else tokens.textSecondary,
                                    ),
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Trạng thái nợ",
                                    style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                                )
                                val debtStatusText = when {
                                    deal.status == DealStatus.COMPLETED && deal.writtenOffCapital.value > 0 -> "Đã xóa nợ"
                                    deal.status == DealStatus.COMPLETED || deal.isFullyRecovered -> "Đã thu đủ"
                                    deal.totalRecovered.value > 0 -> "Đang trả nợ"
                                    else -> "Chưa thu hồi"
                                }
                                val debtStatusColor = when {
                                    deal.status == DealStatus.COMPLETED && deal.writtenOffCapital.value > 0 -> errorColor
                                    deal.status == DealStatus.COMPLETED || deal.isFullyRecovered -> Color(0xFF10B981)
                                    deal.totalRecovered.value > 0 -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                                Text(
                                    text = debtStatusText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = debtStatusColor,
                                    ),
                                )
                            }
                        }
                    } else {
                        // Đầu tư: Vốn chưa thu hồi & Lợi nhuận ròng & ROI %
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
                                val isZero = deal.netProfitLoss.value == 0L
                                Text(
                                    text = if (isZero) "0.0%" else String.format(java.util.Locale.US, "%+.1f%%", roi),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isZero) tokens.textSecondary else if (roi >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // Dòng thời gian giao dịch (Transaction Timeline)
            Text(
                text = if (isLending) "Lịch sử dòng tiền khoản vay (${transactions.size})" else "Lịch sử dòng tiền thương vụ (${transactions.size})",
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
                        text = if (isLending) "Chưa có giao dịch phát sinh cho khoản vay này" else "Chưa có giao dịch phát sinh cho thương vụ này",
                        style = MaterialTheme.typography.bodySmall.copy(color = tokens.textSecondary),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(transactions) { tx ->
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                        val flowLabel = when (tx.dealFlowType) {
                            DealFlowType.OUTLAY_CAPITAL -> if (isLending) "Cho vay" else "Xuất vốn"
                            DealFlowType.PRINCIPAL_RECOVERY -> if (isLending) "Thu gốc" else "Hoàn gốc"
                            DealFlowType.CAPITAL_GAIN -> if (isLending) "Tiền lãi" else "Lãi ròng"
                            DealFlowType.CAPITAL_LOSS -> if (isLending) "Xóa nợ" else "Lỗ chốt deal"
                            null -> tx.type.name
                        }
                        val flowColor = when (tx.dealFlowType) {
                            DealFlowType.OUTLAY_CAPITAL -> Color(0xFFEF4444)
                            DealFlowType.PRINCIPAL_RECOVERY -> Color(0xFF2563EB)
                            DealFlowType.CAPITAL_GAIN -> if (isLending) Color(0xFF8B5CF6) else Color(0xFF10B981)
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
                if (deal.status == DealStatus.COMPLETED) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surfaceSoft),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (deal.writtenOffCapital.value > 0) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (deal.writtenOffCapital.value > 0) {
                                    if (isLending) "Khoản vay đã xóa nợ và đóng sổ" else "Thương vụ đã chốt lỗ và đóng sổ"
                                } else {
                                    if (isLending) "Khoản vay đã hoàn tất đóng sổ" else "Thương vụ đã hoàn tất đóng sổ"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = tokens.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }

                    // Deal COMPLETED: Nút Khôi phục nợ/deal và Nút Xóa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val hasStopLoss = transactions.any { it.dealFlowType == DealFlowType.CAPITAL_LOSS }
                        if (hasStopLoss) {
                            OutlinedButton(
                                onClick = { showRevertStopLossConfirm = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6)),
                            ) {
                                Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isLending) "Khôi Phục Nợ" else "Hồi Phục Deal", fontSize = 13.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showReopenDealConfirm = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                            ) {
                                Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isLending) "Mở Lại Khoản Vay" else "Mở Lại Deal", fontSize = 13.sp)
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
                            Text(if (isLending) "Xóa Khoản Vay" else "Xóa Deal", fontSize = 13.sp)
                        }
                    }
                } else {
                    // Deal ACTIVE:
                    // Row 1: Giao dịch dòng tiền
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
                            Icon(
                                imageVector = if (isLending) Icons.Default.Handshake else Icons.Rounded.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isLending) "Cho Vay Thêm" else "Xuất Thêm Vốn", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onAddInflow,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLending) Color(0xFFD97706) else Color(0xFF10B981),
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isLending) "Thu Nợ / Lãi" else "Thu Hồi / Lời", fontSize = 13.sp)
                        }
                    }

                    // Row 2: Các nút chốt sổ / tất toán
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (deal.remainingCapital.value > 0L) {
                            OutlinedButton(
                                onClick = { showStopLossConfirm = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            ) {
                                Text(if (isLending) "Xóa Nợ & Đóng" else "Chốt Lỗ & Đóng", fontSize = 13.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { showCloseDealConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isLending) "Tất Toán & Đóng" else "Tất Toán Deal", fontSize = 13.sp)
                        }
                    }

                    // Row 3: Xóa deal
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.textSecondary),
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isLending) "Xóa Khoản Cho Vay" else "Xóa Thương Vụ", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa (kèm đếm ngược 5 giây bảo vệ dữ liệu)
    if (showDeleteConfirm) {
        var countdownSeconds by remember(showDeleteConfirm) { mutableIntStateOf(5) }

        LaunchedEffect(showDeleteConfirm) {
            countdownSeconds = 5
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                countdownSeconds -= 1
            }
        }

        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isLending) "Xác Nhận Xóa Khoản Cho Vay" else "Xác Nhận Xóa Thương Vụ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    ),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa '${deal.title}' không?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "⚠️ Lưu ý: Hành động này sẽ hoàn trả số dư các ví đã dùng để xuất vốn hoặc thu hồi tiền về trạng thái ban đầu và xóa vĩnh viễn toàn bộ lịch sử liên quan.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                            ),
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    enabled = countdownSeconds == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        disabledContainerColor = tokens.surfaceSoft,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (countdownSeconds > 0) "Xác nhận xóa (${countdownSeconds}s)" else "Xóa Vĩnh Viễn",
                        color = if (countdownSeconds == 0) Color.White else tokens.textSecondary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Hủy", color = tokens.textPrimary)
                }
            },
        )
    }

    // Dialog xác nhận chốt lỗ / xóa nợ xấu
    if (showStopLossConfirm) {
        AlertDialog(
            onDismissRequest = { showStopLossConfirm = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isLending) "Xác Nhận Xóa Nợ Xấu & Đóng" else "Xác Nhận Chốt Lỗ & Đóng Deal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    ),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isLending) {
                            "Khoản nợ chưa thu hồi: ${deal.remainingCapital.value.toVnd()}\n\nKhi đóng, số tiền nợ chưa thu hồi sẽ được ghi nhận là một khoản Thất thoát/Chi phí vào báo cáo thu chi."
                        } else {
                            "Vốn chưa thu hồi: ${deal.remainingCapital.value.toVnd()}\n\nKhi chốt lỗ, số vốn chưa thu hồi sẽ được ghi nhận là một khoản Chi phí thực tế vào báo cáo thu chi và chuyển trạng thái deal sang Đã hoàn tất."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopLossConfirm = false
                        onCloseWithLoss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (isLending) "Đồng Ý Xóa Nợ" else "Đồng Ý Chốt Lỗ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStopLossConfirm = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Hủy", color = tokens.textPrimary)
                }
            },
        )
    }

    // Dialog xác nhận hủy chốt lỗ
    if (showRevertStopLossConfirm) {
        AlertDialog(
            onDismissRequest = { showRevertStopLossConfirm = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Hồi Phục Trạng Thái Deal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    ),
                )
            },
            text = {
                Text(
                    text = "Giao dịch chi phí lỗ sẽ được xóa bỏ và deal sẽ được mở lại trạng thái Đang chạy.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRevertStopLossConfirm = false
                        onRevertStopLoss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Đồng Ý Hồi Phục", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRevertStopLossConfirm = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Hủy", color = tokens.textPrimary)
                }
            },
        )
    }

    // Dialog xác nhận mở lại deal / khoản vay đã hoàn tất
    if (showReopenDealConfirm) {
        AlertDialog(
            onDismissRequest = { showReopenDealConfirm = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isLending) "Mở Lại Khoản Vay?" else "Mở Lại Thương Vụ?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    ),
                )
            },
            text = {
                Text(
                    text = if (isLending) {
                        "Chuyển khoản vay \"${deal.title}\" từ trạng thái Đã Hoàn Tất về Đang Chạy để tiếp tục ghi nhận thu nợ và xuất vốn."
                    } else {
                        "Chuyển thương vụ \"${deal.title}\" từ trạng thái Đã Hoàn Tất về Đang Chạy để tiếp tục ghi nhận dòng tiền thu hồi và xuất vốn."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReopenDealConfirm = false
                        onReopenDeal()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Mở Lại", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReopenDealConfirm = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Hủy", color = tokens.textPrimary)
                }
            },
        )
    }

    // Dialog xác nhận Tất Toán & Đóng Deal
    if (showCloseDealConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseDealConfirm = false },
            containerColor = tokens.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isLending) "Tất Toán & Đóng Khoản Vay" else "Tất Toán & Đóng Thương Vụ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.onSurface,
                    ),
                )
            },
            text = {
                Text(
                    text = if (deal.remainingCapital.value > 0) {
                        "Khoản vốn chưa thu hồi còn ${deal.remainingCapital.value.toVnd()}. Bạn có chắc chắn muốn tất toán và chuyển thương vụ này sang trạng thái Đã Hoàn Tất?"
                    } else {
                        "Thương vụ đã hoàn tất và thu hồi đủ vốn. Xác nhận chuyển sang danh sách Đã Hoàn Tất?"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = tokens.onSurface),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseDealConfirm = false
                        onCloseDeal()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Xác Nhận Đóng", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCloseDealConfirm = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Hủy", color = tokens.textPrimary)
                }
            },
        )
    }
}
