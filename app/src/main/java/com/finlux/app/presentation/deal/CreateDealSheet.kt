package com.finlux.app.presentation.deal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
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
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.DealStatus
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

    var category by remember { mutableStateOf(initialDeal?.category ?: DealCategory.INVESTMENT) }
    var status by remember { mutableStateOf(initialDeal?.status ?: DealStatus.ACTIVE) }
    var title by remember { mutableStateOf(initialDeal?.title.orEmpty()) }
    var description by remember { mutableStateOf(initialDeal?.description.orEmpty()) }
    var targetAmountText by remember {
        mutableStateOf(if ((initialDeal?.targetAmount?.value ?: 0L) > 0) initialDeal?.targetAmount?.value.toString() else "")
    }

    val isLending = category == DealCategory.LENDING

    val headerTitle = if (initialDeal == null) {
        if (isLending) "Tạo Khoản Cho Vay Mới" else "Tạo Thương Vụ Mới"
    } else {
        if (isLending) "Chỉnh Sửa Khoản Cho Vay" else "Chỉnh Sửa Thương Vụ"
    }

    val headerSubtitle = if (isLending) {
        "Theo dõi nợ gốc, thu hồi nợ và tiền lãi phát sinh"
    } else {
        "Theo dõi xuất vốn, thu hồi & tính tỷ suất ROI thời gian thực"
    }

    val titleLabel = if (isLending) "TÊN KHOẢN CHO VAY / NGƯỜI VAY *" else "TÊN THƯƠNG VỤ / DỰ ÁN *"
    val titlePlaceholder = if (isLending) "Ví dụ: Cho bạn Nam mượn, Góp vốn anh Tín..." else "Ví dụ: Lướt sóng iPhone, Góp vốn lô hàng..."

    val descriptionLabel = if (isLending) "GHI CHÚ / ĐỐI TÁC / THỜI HẠN (TÙY CHỌN)" else "MÔ TẢ / ĐỐI TÁC (TÙY CHỌN)"
    val descriptionPlaceholder = if (isLending) "Ghi chú thời hạn trả, lãi suất nếu có..." else "Ghi chú đối tác, thời hạn..."

    val targetLabel = if (isLending) "TỔNG NỢ GỐC DỰ KIẾN / KỲ VỌNG THU HỒI" else "MỤC TIÊU THU VỀ KỲ VỌNG"

    val buttonLabel = if (initialDeal == null) {
        if (isLending) "Tạo Khoản Cho Vay" else "Tạo Thương Vụ"
    } else {
        "Lưu Thay Đổi"
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                    Text(
                        text = headerSubtitle,
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

            // Category Selector Tabs (Đầu Tư / Cho Vay)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = tokens.surfaceSoft,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Tab 1: Đầu Tư
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { category = DealCategory.INVESTMENT },
                            ),
                        shape = RoundedCornerShape(10.dp),
                        color = if (category == DealCategory.INVESTMENT) tokens.surface else Color.Transparent,
                        border = if (category == DealCategory.INVESTMENT) BorderStroke(1.dp, tokens.border) else null,
                        shadowElevation = if (category == DealCategory.INVESTMENT) 1.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (category == DealCategory.INVESTMENT) tokens.primary else tokens.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Đầu Tư",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (category == DealCategory.INVESTMENT) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (category == DealCategory.INVESTMENT) tokens.primary else tokens.onSurfaceVariant,
                            )
                        }
                    }

                    // Tab 2: Cho Vay
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { category = DealCategory.LENDING },
                            ),
                        shape = RoundedCornerShape(10.dp),
                        color = if (category == DealCategory.LENDING) tokens.surface else Color.Transparent,
                        border = if (category == DealCategory.LENDING) BorderStroke(1.dp, tokens.border) else null,
                        shadowElevation = if (category == DealCategory.LENDING) 1.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = if (category == DealCategory.LENDING) Color(0xFFF59E0B) else tokens.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Cho Vay / Mượn",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (category == DealCategory.LENDING) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (category == DealCategory.LENDING) Color(0xFFD97706) else tokens.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Tên thương vụ / Khoản vay (ErgonomicInputRow)
            ErgonomicInputRow(
                label = titleLabel,
                value = title,
                onValueChange = { title = it },
                placeholder = titlePlaceholder,
                icon = if (isLending) Icons.Default.Handshake else Icons.AutoMirrored.Filled.TrendingUp,
                iconBgColor = if (isLending) Color(0xFFF59E0B).copy(alpha = 0.12f) else tokens.primary.copy(alpha = 0.12f),
                iconTintColor = if (isLending) Color(0xFFD97706) else tokens.primary,
                onClear = { title = "" },
            )

            // Mô tả / Đối tác (ErgonomicInputRow)
            ErgonomicInputRow(
                label = descriptionLabel,
                value = description,
                onValueChange = { description = it },
                placeholder = descriptionPlaceholder,
                icon = Icons.Default.Description,
                iconBgColor = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                iconTintColor = Color(0xFF8B5CF6),
                onClear = { description = "" },
            )

            // Mục tiêu kỳ vọng (ErgonomicCompactAmountCard)
            ErgonomicCompactAmountCard(
                label = targetLabel,
                amountText = targetAmountText,
                onAmountChange = { targetAmountText = it },
                amountColor = if (isLending) Color(0xFFD97706) else tokens.primary,
            )

            if (initialDeal != null) {
                // Trạng thái Deal
                Text(
                    text = "TRẠNG THÁI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = tokens.textSecondary,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.surfaceSoft,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { status = DealStatus.ACTIVE },
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (status == DealStatus.ACTIVE) tokens.surface else Color.Transparent,
                            border = if (status == DealStatus.ACTIVE) BorderStroke(1.dp, tokens.border) else null,
                            shadowElevation = if (status == DealStatus.ACTIVE) 1.dp else 0.dp,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Đang Chạy",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (status == DealStatus.ACTIVE) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (status == DealStatus.ACTIVE) tokens.primary else tokens.onSurfaceVariant,
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { status = DealStatus.COMPLETED },
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (status == DealStatus.COMPLETED) tokens.surface else Color.Transparent,
                            border = if (status == DealStatus.COMPLETED) BorderStroke(1.dp, tokens.border) else null,
                            shadowElevation = if (status == DealStatus.COMPLETED) 1.dp else 0.dp,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Đã Hoàn Tất",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (status == DealStatus.COMPLETED) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (status == DealStatus.COMPLETED) Color(0xFF10B981) else tokens.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    val targetVal = targetAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    val newDeal = initialDeal?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        targetAmount = Money(targetVal),
                        status = status,
                        endDate = if (status == DealStatus.ACTIVE) null else initialDeal.endDate,
                        updatedAt = Instant.now(),
                    ) ?: FinancialDeal(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
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
                    containerColor = if (isLending) Color(0xFFD97706) else tokens.primary,
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = buttonLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}
