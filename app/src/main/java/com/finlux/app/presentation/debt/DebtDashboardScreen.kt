package com.finlux.app.presentation.debt

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.presentation.debt.components.DebtBurndownChart
import com.finlux.app.presentation.debt.components.DebtCard
import com.finlux.app.presentation.debt.components.DebtPaymentHistorySheet
import com.finlux.app.presentation.debt.components.StrategySelectorCard
import com.finlux.app.presentation.home.toShortVnd
import com.finlux.app.presentation.home.toVnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import com.finlux.app.core.designsystem.GlassTopBar

@Composable
fun DebtDashboardScreen(
    onBack: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddEditSheet by remember { mutableStateOf(false) }
    var showPaymentHistorySheet by remember { mutableStateOf(false) }
    var historyDebtId by remember { mutableStateOf<String?>(null) }
    var editingDebt by remember { mutableStateOf<DebtAccount?>(null) }
    var payingDebt by remember { mutableStateOf<DebtAccount?>(null) }
    var selectedFilterType by remember { mutableStateOf<DebtType?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    val tokens = LocalFinluxTokens.current

    Box(modifier = Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                FinluxScreenHeader(
                    title = "Quản lý nợ & Tín dụng",
                    subtitle = if (uiState.activeDebtsCount > 0) "${uiState.activeDebtsCount} khoản nợ đang hoạt động" else "Chưa có khoản nợ",
                    onBack = onBack,
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tokens.surfaceSoft,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        historyDebtId = null
                                        showPaymentHistorySheet = true
                                    },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Lịch sử thanh toán",
                                        tint = tokens.onSurface,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = tokens.primary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        editingDebt = null
                                        showAddEditSheet = true
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = tokens.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Thêm",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = tokens.primary,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                // Overview Hero Card
                item {
                    DebtOverviewHeroCard(uiState = uiState)
                }

                // Unified Bento Container: Strategy + AI Cashflow Advisor + Slider + Burndown Chart
                item {
                    StrategySelectorCard(
                        currentStrategy = uiState.strategy,
                        extraMonthlyPayment = uiState.extraMonthlyPayment,
                        payoffPlan = uiState.payoffPlan,
                        initialDebtAmount = uiState.totalRemainingDebt.value,
                        cashflowAnalysis = uiState.cashflowAnalysis,
                        onStrategySelected = viewModel::setStrategy,
                        onExtraPaymentChanged = viewModel::setExtraMonthlyPayment,
                    )
                }

                // Section Header & Filters
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Danh sách khoản nợ (${uiState.debts.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChipItem(
                                label = "Tất cả",
                                isSelected = selectedFilterType == null,
                                onClick = { selectedFilterType = null },
                            )
                            DebtType.entries.forEach { type ->
                                FilterChipItem(
                                    label = when (type) {
                                        DebtType.CREDIT_CARD -> "Thẻ"
                                        DebtType.BANK_LOAN -> "Vay bank"
                                        DebtType.PERSONAL_LOAN -> "Cá nhân"
                                        DebtType.INSTALLMENT -> "Trả góp"
                                    },
                                    isSelected = selectedFilterType == type,
                                    onClick = { selectedFilterType = type },
                                )
                            }
                        }
                    }
                }

                // Debts List Items
                val filteredDebts = uiState.debts.filter { debt ->
                    selectedFilterType == null || debt.type == selectedFilterType
                }

                if (filteredDebts.isEmpty()) {
                    item {
                        FinluxEmptyState(
                            title = if (uiState.debts.isEmpty()) "Chưa có khoản nợ nào" else "Không có khoản nợ trong danh mục này",
                            description = if (uiState.debts.isEmpty()) "Thêm khoản nợ hoặc thẻ tín dụng để theo dõi lộ trình trả nợ tối ưu." else "Hãy chọn danh mục khác hoặc thêm khoản nợ mới.",
                            icon = Icons.Default.CreditScore,
                            actionLabel = if (uiState.debts.isEmpty()) "+ Thêm khoản nợ" else null,
                            onActionClick = if (uiState.debts.isEmpty()) { { editingDebt = null; showAddEditSheet = true } } else null,
                        )
                    }
                } else {
                    items(filteredDebts, key = { it.id }) { debt ->
                        DebtCard(
                            debt = debt,
                            onPayClick = { payingDebt = debt },
                            onEditClick = {
                                editingDebt = debt
                                showAddEditSheet = true
                            },
                            onDeleteClick = { viewModel.deleteDebt(debt) },
                            onHistoryClick = {
                                historyDebtId = debt.id
                                showPaymentHistorySheet = true
                            },
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FinluxBlue)
                }
            }
        }
    }

    // Add / Edit Sheet
    if (showAddEditSheet) {
        AddEditDebtSheet(
            debt = editingDebt,
            onDismiss = {
                showAddEditSheet = false
                editingDebt = null
            },
            onSave = { newDebt ->
                viewModel.saveDebt(newDebt) {
                    showAddEditSheet = false
                    editingDebt = null
                }
            },
            onDelete = { debtToDelete ->
                viewModel.deleteDebt(debtToDelete) {
                    showAddEditSheet = false
                    editingDebt = null
                }
            },
            isSubmitting = uiState.isSubmitting,
        )
    }

    // Payment Sheet
    payingDebt?.let { debtToPay ->
        DebtPaymentSheet(
            debt = debtToPay,
            wallets = uiState.wallets,
            onDismiss = { payingDebt = null },
            onConfirmPayment = { walletId, amount, principal, interest, note ->
                viewModel.payDebt(
                    debtId = debtToPay.id,
                    walletId = walletId,
                    amount = amount,
                    principalPaid = principal,
                    interestPaid = interest,
                    note = note,
                ) {
                    payingDebt = null
                }
            },
            isSubmitting = uiState.isSubmitting,
        )
    }

    // Payment History Sheet
    if (showPaymentHistorySheet) {
        DebtPaymentHistorySheet(
            debts = uiState.debts,
            wallets = uiState.wallets,
            paymentHistory = uiState.paymentHistory,
            initialDebtId = historyDebtId,
            onDismiss = {
                showPaymentHistorySheet = false
                historyDebtId = null
            },
        )
    }
}
}

@Composable
private fun DebtOverviewHeroCard(
    uiState: DebtUiState,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFF9333EA)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditScore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tổng dư nợ hiện tại",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = uiState.totalRemainingDebt.value.toVnd(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE11D48).copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "${uiState.activeDebtsCount} khoản nợ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { uiState.overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF10B981),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Đã thanh toán: ${uiState.totalPaidDebt.value.toShortVnd()} (${(uiState.overallProgress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981),
                    ),
                )
                Text(
                    text = "Tổng vay: ${uiState.totalOriginalDebt.value.toShortVnd()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) FinluxBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) BorderStroke(1.dp, FinluxBlue) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

