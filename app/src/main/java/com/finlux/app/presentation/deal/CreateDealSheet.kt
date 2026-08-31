package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicInputRow
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.domain.model.Money
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDealSheet(
    initialDeal: FinancialDeal? = null,
    onDismiss: () -> Unit,
    onConfirm: (FinancialDeal) -> Unit,
    isSubmitting: Boolean = false,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initialDeal?.title.orEmpty()) }
    var description by remember { mutableStateOf(initialDeal?.description.orEmpty()) }
    var targetAmountText by remember {
        mutableStateOf(if ((initialDeal?.targetAmount?.value ?: 0L) > 0) initialDeal?.targetAmount?.value.toString() else "")
    }

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
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (initialDeal == null) "Tạo Thương Vụ Mới" else "Chỉnh Sửa Thương Vụ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = "Theo dõi xuất vốn, thu hồi & tính tỷ suất ROI thời gian thực",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = tokens.onSurfaceVariant,
                        ),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Tên thương vụ (ErgonomicInputRow)
            ErgonomicInputRow(
                label = "TÊN THƯƠNG VỤ / DỰ ÁN *",
                value = title,
                onValueChange = { title = it },
                placeholder = "Ví dụ: Lướt sóng iPhone, Góp vốn lô hàng...",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconBgColor = tokens.primary.copy(alpha = 0.12f),
                iconTintColor = tokens.primary,
                onClear = { title = "" },
            )

            // Mô tả / Đối tác (ErgonomicInputRow)
            ErgonomicInputRow(
                label = "MÔ TẢ / ĐỐI TÁC (TÙY CHỌN)",
                value = description,
                onValueChange = { description = it },
                placeholder = "Ghi chú đối tác, thời hạn...",
                icon = Icons.Default.Description,
                iconBgColor = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                iconTintColor = Color(0xFF8B5CF6),
                onClear = { description = "" },
            )

            // Mục tiêu kỳ vọng (ErgonomicCompactAmountCard)
            ErgonomicCompactAmountCard(
                label = "MỤC TIÊU THU VỀ KỲ VỌNG",
                amountText = targetAmountText,
                onAmountChange = { targetAmountText = it },
                amountColor = tokens.primary,
            )

            Spacer(Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    val targetVal = targetAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    val newDeal = initialDeal?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        targetAmount = Money(targetVal),
                        updatedAt = Instant.now(),
                    ) ?: FinancialDeal(
                        title = title.trim(),
                        description = description.trim(),
                        targetAmount = Money(targetVal),
                    )
                    onConfirm(newDeal)
                },
                enabled = title.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (initialDeal == null) "Tạo Thương Vụ" else "Lưu Thay Đổi",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}
