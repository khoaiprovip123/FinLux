package com.finlux.app.presentation.settings.salary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.BudgetPeriodBasis
import com.finlux.app.domain.model.CycleRolloverRule
import com.finlux.app.domain.model.PaydayRuleType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.home.toVnd
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryCycleSettingsSheet(
    onDismiss: () -> Unit,
    viewModel: SalaryCycleViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var expectedSalaryText by remember(state.config.expectedSalary) {
        mutableStateOf(state.config.expectedSalary?.toString().orEmpty())
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            android.widget.Toast.makeText(
                context,
                msg,
                android.widget.Toast.LENGTH_SHORT,
            ).show()
            viewModel.clearMessages()
            onDismiss()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tokens.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Tháng tài chính & Chu kỳ lương",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                            ),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "Tùy biến kỳ tính thu chi theo ngày nhận lương",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider(color = tokens.border.copy(alpha = 0.35f))

            // 1. Enable Toggle Switch
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surfaceSoft,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Bật tính năng chu kỳ lương",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = tokens.onSurface,
                        )
                        Text(
                            text = if (state.config.enabled) "Đang áp dụng chu kỳ tài chính tùy biến" else "Mặc định tính theo tháng dương lịch",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.config.enabled,
                        onCheckedChange = { viewModel.setEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                        ),
                    )
                }
            }

            // Live Preview Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF059669).copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
                                    Color(0xFF10B981).copy(alpha = if (tokens.isDark) 0.12f else 0.05f),
                                    tokens.surfaceSoft,
                                ),
                            ),
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Xem trước dải chu kỳ tài chính",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Kỳ hiện tại", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant)
                            Text(
                                text = state.currentCyclePreview.ifBlank { "Chưa xác định" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = tokens.onSurface,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Kỳ tiếp theo", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant)
                            Text(
                                text = state.nextCyclePreview.ifBlank { "Chưa xác định" },
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = tokens.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Options (Visible when enabled or for configuration)
            AnimatedVisibility(
                visible = state.config.enabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 2. Payday Rule Type
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Quy tắc ngày nhận lương",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = tokens.onSurface,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PaydayRuleCard(
                                title = "Ngày cố định",
                                subtitle = "Ngày 1 - 31",
                                isSelected = state.config.paydayRuleType == PaydayRuleType.DAY_OF_MONTH,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setPaydayRuleType(PaydayRuleType.DAY_OF_MONTH) },
                            )
                            PaydayRuleCard(
                                title = "Đầu tháng",
                                subtitle = "Ngày 1",
                                isSelected = state.config.paydayRuleType == PaydayRuleType.FIRST_DAY_OF_MONTH,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setPaydayRuleType(PaydayRuleType.FIRST_DAY_OF_MONTH) },
                            )
                            PaydayRuleCard(
                                title = "Cuối tháng",
                                subtitle = "28 - 31",
                                isSelected = state.config.paydayRuleType == PaydayRuleType.LAST_DAY_OF_MONTH,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setPaydayRuleType(PaydayRuleType.LAST_DAY_OF_MONTH) },
                            )
                        }
                    }

                    // 3. Day of Month Picker (When DAY_OF_MONTH is active)
                    if (state.config.paydayRuleType == PaydayRuleType.DAY_OF_MONTH) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Ngày nhận lương hàng tháng:",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = tokens.onSurfaceVariant,
                                )
                                Text(
                                    text = "Ngày ${state.config.paydayDay}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF10B981),
                                )
                            }

                            // Quick chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(1, 5, 10, 15, 20, 25, 30).forEach { day ->
                                    val isSelected = state.config.paydayDay == day
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.18f) else tokens.surfaceSoft,
                                        border = if (isSelected) BorderStroke(1.2.dp, Color(0xFF10B981)) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setPaydayDay(day) },
                                    ) {
                                        Text(
                                            text = "$day",
                                            textAlign = TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF10B981) else tokens.onSurface,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                        )
                                    }
                                }
                            }

                            // Slider
                            Slider(
                                value = state.config.paydayDay.toFloat(),
                                onValueChange = { viewModel.setPaydayDay(it.roundToInt()) },
                                valueRange = 1f..31f,
                                steps = 29,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF10B981),
                                    activeTrackColor = Color(0xFF10B981),
                                ),
                            )
                        }
                    }

                    // 4. Salary Receiving Wallet Selector
                    if (state.wallets.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Ví nhận lương chính",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = tokens.onSurface,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.wallets) { wallet ->
                                    val isSelected = state.config.salaryWalletId == wallet.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setSalaryWalletId(if (isSelected) null else wallet.id)
                                        },
                                        label = { Text(wallet.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.18f),
                                            selectedLabelColor = Color(0xFF10B981),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    // 5. Expected Salary Input
                    ErgonomicCompactAmountCard(
                        label = "Mức lương dự kiến mỗi kỳ",
                        amountText = expectedSalaryText,
                        onAmountChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }.take(15)
                            expectedSalaryText = digitsOnly
                            val parsed = digitsOnly.toLongOrNull()
                            viewModel.setExpectedSalary(parsed)
                        },
                        placeholder = "20.000.000",
                        amountColor = tokens.primary,
                        showSuggestions = true,
                    )

                    // 6. End-of-Cycle Leftover Handling
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Xử lý số tiền dư cuối kỳ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = tokens.onSurface,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RolloverOptionRow(
                                title = "Giữ nguyên trong ví hiện tại",
                                description = "Không tạo giao dịch chuyển tiền tự động",
                                isSelected = state.config.rolloverRule == CycleRolloverRule.KEEP_IN_WALLET,
                                onClick = { viewModel.setRolloverRule(CycleRolloverRule.KEEP_IN_WALLET) },
                            )
                            RolloverOptionRow(
                                title = "Hỏi tôi khi hết kỳ",
                                description = "Hiển thị thẻ nhắc chuyển tiền dư khi sang kỳ mới",
                                isSelected = state.config.rolloverRule == CycleRolloverRule.ASK_EACH_CYCLE,
                                onClick = { viewModel.setRolloverRule(CycleRolloverRule.ASK_EACH_CYCLE) },
                            )
                            RolloverOptionRow(
                                title = "Đề xuất chuyển vào ví tích lũy",
                                description = "Chuẩn bị sẵn giao dịch chuyển sang ví tiết kiệm (cần bạn xác nhận)",
                                isSelected = state.config.rolloverRule == CycleRolloverRule.MOVE_TO_SAVINGS,
                                onClick = { viewModel.setRolloverRule(CycleRolloverRule.MOVE_TO_SAVINGS) },
                            )
                        }
                    }

                    // 7. Budget Period Basis
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Căn cứ chu kỳ cho Ngân Sách",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = tokens.onSurface,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BudgetBasisCard(
                                title = "Tháng dương lịch",
                                isSelected = state.config.budgetPeriodBasis == BudgetPeriodBasis.CALENDAR_MONTH,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setBudgetPeriodBasis(BudgetPeriodBasis.CALENDAR_MONTH) },
                            )
                            BudgetBasisCard(
                                title = "Theo kỳ lương",
                                isSelected = state.config.budgetPeriodBasis == BudgetPeriodBasis.SALARY_CYCLE,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setBudgetPeriodBasis(BudgetPeriodBasis.SALARY_CYCLE) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Đóng")
                }
                Button(
                    onClick = { viewModel.saveConfig() },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                    ),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Lưu cấu hình", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaydayRuleCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.15f) else tokens.surfaceSoft,
        border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF10B981)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF10B981) else tokens.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = tokens.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RolloverOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.12f) else tokens.surfaceSoft,
        border = if (isSelected) BorderStroke(1.2.dp, Color(0xFF10B981)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isSelected) Color(0xFF10B981) else tokens.onSurface,
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = tokens.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun BudgetBasisCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.15f) else tokens.surfaceSoft,
        border = if (isSelected) BorderStroke(1.2.dp, Color(0xFF10B981)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF10B981) else tokens.onSurface,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
        )
    }
}
