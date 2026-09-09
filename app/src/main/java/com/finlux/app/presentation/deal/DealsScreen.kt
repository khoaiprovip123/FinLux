package com.finlux.app.presentation.deal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.component.FinluxSnackbarHost
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.DealCategory
import com.finlux.app.domain.model.FinancialDeal
import com.finlux.app.presentation.home.toVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DealsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val tokens = LocalFinluxTokens.current
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var editingDeal by remember { mutableStateOf<FinancialDeal?>(null) }
    var showInflowDialog by remember { mutableStateOf(false) }
    var showOutlayDialog by remember { mutableStateOf(false) }
    var targetDealForAction by remember { mutableStateOf<FinancialDeal?>(null) }

    // Xử lý thông báo Toast
    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FinluxStyleBackdrop()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = {
                        Text(
                            text = "Thương Vụ & Đầu Tư",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary,
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = tokens.textPrimary,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showHistorySheet = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = "Lịch sử dòng tiền",
                                tint = tokens.textPrimary,
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = tokens.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text("Tạo Deal Mới", fontWeight = FontWeight.Bold)
                    }
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                // 1. HERO ROI SUMMARY CARD
                item {
                    HeroSummaryCard(state = state)
                }

                // 2. TAB SWITCHER (ACTIVE / COMPLETED)
                item {
                    TabSwitcher(
                        selectedTab = state.selectedTab,
                        activeCount = state.activeDeals.size,
                        completedCount = state.completedDeals.size,
                        onTabSelected = { viewModel.selectTab(it) },
                    )
                }

                // 3. LIST OF DEALS
                val currentDeals = if (state.selectedTab == DealTab.ACTIVE) state.activeDeals else state.completedDeals
                if (currentDeals.isEmpty()) {
                    item {
                        EmptyDealsView(
                            tab = state.selectedTab,
                            onCreateClick = { showCreateDialog = true },
                        )
                    }
                } else {
                    items(currentDeals, key = { it.id }) { deal ->
                        DealCardItem(
                            deal = deal,
                            onClick = {
                                viewModel.selectDeal(deal)
                            },
                        )
                    }
                }
            }
        }

        // Bottom Sheet chi tiết deal
        state.selectedDeal?.let { selected ->
            DealDetailBottomSheet(
                deal = selected,
                transactions = state.transactions,
                onDismiss = { viewModel.selectDeal(null) },
                onEditDeal = {
                    editingDeal = selected
                    viewModel.selectDeal(null)
                },
                onAddOutlay = {
                    targetDealForAction = selected
                    showOutlayDialog = true
                },
                onAddInflow = {
                    targetDealForAction = selected
                    showInflowDialog = true
                },
                onCloseWithLoss = {
                    val dealId = selected.id
                    viewModel.closeDealWithLoss(selected)
                },
                onCloseDeal = {
                    val dealId = selected.id
                    viewModel.closeDeal(dealId)
                },
                onRevertStopLoss = {
                    val dealId = selected.id
                    viewModel.revertDealLoss(dealId)
                },
                onReopenDeal = {
                    val dealId = selected.id
                    viewModel.reopenDeal(dealId)
                },
                onDelete = {
                    val dealId = selected.id
                    viewModel.deleteDeal(dealId, onSuccess = {
                        viewModel.selectDeal(null)
                    })
                },
            )
        }

        // Sheet Tạo Deal Mới
        if (showCreateDialog) {
            CreateDealSheet(
                onDismiss = { showCreateDialog = false },
                onConfirm = { newDeal ->
                    showCreateDialog = false
                    viewModel.createOrUpdateDeal(newDeal)
                },
                isSubmitting = state.isSubmitting,
            )
        }

        // Sheet Chỉnh Sửa Deal
        editingDeal?.let { currentDeal ->
            CreateDealSheet(
                initialDeal = currentDeal,
                onDismiss = { editingDeal = null },
                onConfirm = { updatedDeal ->
                    editingDeal = null
                    viewModel.createOrUpdateDeal(updatedDeal)
                },
                isSubmitting = state.isSubmitting,
            )
        }

        // Sheet Nhật Ký Dòng Tiền Toàn Bộ Deal & Khoản Vay
        if (showHistorySheet) {
            DealAllTransactionsBottomSheet(
                deals = state.deals,
                transactions = state.allDealTransactions,
                wallets = state.wallets,
                onDismiss = { showHistorySheet = false },
                onSelectDeal = { deal ->
                    viewModel.selectDeal(deal)
                },
            )
        }

        // Sheet Thu hồi vốn / Lãi
        if (showInflowDialog && targetDealForAction != null) {
            RecordDealInflowSheet(
                deal = targetDealForAction!!,
                wallets = state.wallets,
                onDismiss = {
                    showInflowDialog = false
                    targetDealForAction = null
                },
                onConfirm = { walletId, amount, date, note ->
                    val deal = targetDealForAction!!
                    showInflowDialog = false
                    targetDealForAction = null
                    viewModel.recordInflow(deal, walletId, amount, date = date, note = note)
                },
                isSubmitting = state.isSubmitting,
            )
        }

        // Sheet Chi xuất thêm vốn
        if (showOutlayDialog && targetDealForAction != null) {
            RecordDealOutlaySheet(
                deal = targetDealForAction!!,
                wallets = state.wallets,
                onDismiss = {
                    showOutlayDialog = false
                    targetDealForAction = null
                },
                onConfirm = { walletId, amount, date, note ->
                    val deal = targetDealForAction!!
                    showOutlayDialog = false
                    targetDealForAction = null
                    viewModel.recordOutlay(deal, walletId, amount, date = date, note = note)
                },
                isSubmitting = state.isSubmitting,
            )
        }
    }
}

@Composable
private fun HeroSummaryCard(state: DealsUiState) {
    val tokens = LocalFinluxTokens.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tokens.surface,
                        tokens.surfaceSoft,
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Vốn Đang Lưu Động",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = tokens.textSecondary,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Text(
                        text = state.totalActiveRemainingCapital.value.toVnd(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                            fontSize = 24.sp,
                        ),
                    )
                }

                // Overall ROI Badge
                val overallRoi = state.overallRoiPercentage
                val isZero = state.totalAccumulatedProfit.value == 0L
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isZero) tokens.surfaceSoft
                            else if (overallRoi >= 0) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (isZero) "ROI 0.0%" else String.format(java.util.Locale.US, "ROI %+.1f%%", overallRoi),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isZero) tokens.textSecondary else if (overallRoi >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                        ),
                    )
                }
            }

            HorizontalDivider(color = tokens.border, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Tổng Vốn Xuất (Active)",
                        style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                    )
                    Text(
                        text = state.totalActiveOutlay.value.toVnd(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary,
                        ),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Lợi Nhuận Tích Lũy",
                        style = MaterialTheme.typography.labelSmall.copy(color = tokens.textSecondary),
                    )
                    val profit = state.totalAccumulatedProfit.value
                    Text(
                        text = if (profit > 0) "+${profit.toVnd()}" else profit.toVnd(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabSwitcher(
    selectedTab: DealTab,
    activeCount: Int,
    completedCount: Int,
    onTabSelected: (DealTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.surfaceSoft)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selectedTab == DealTab.ACTIVE) tokens.surface else Color.Transparent)
                .clickable { onTabSelected(DealTab.ACTIVE) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Đang Chạy ($activeCount)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selectedTab == DealTab.ACTIVE) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == DealTab.ACTIVE) tokens.primary else tokens.textSecondary,
                ),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selectedTab == DealTab.COMPLETED) tokens.surface else Color.Transparent)
                .clickable { onTabSelected(DealTab.COMPLETED) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Đã Hoàn Tất ($completedCount)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selectedTab == DealTab.COMPLETED) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == DealTab.COMPLETED) tokens.primary else tokens.textSecondary,
                ),
            )
        }
    }
}

@Composable
private fun DealCardItem(
    deal: FinancialDeal,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val isLending = deal.category == DealCategory.LENDING

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Row 1: Title, Category Badge & Status/ROI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = deal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tokens.textPrimary,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )

                        // Category Tag
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = if (isLending) Color(0xFFF59E0B).copy(alpha = 0.14f) else tokens.primary.copy(alpha = 0.14f),
                        ) {
                            Text(
                                text = if (isLending) "Cho vay" else "Đầu tư",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = if (isLending) Color(0xFFD97706) else tokens.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                            )
                        }
                    }

                    if (deal.description.isNotBlank()) {
                        Text(
                            text = deal.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = tokens.textSecondary,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Top Right Badge (ROI nếu là Đầu tư, Trạng thái thu nợ nếu là Cho vay)
                if (isLending) {
                    val debtLabel = when {
                        deal.isFullyRecovered -> "Đã thu đủ"
                        deal.totalRecovered.value > 0 -> "Đang trả nợ"
                        else -> "Chưa thu hồi"
                    }
                    val debtBg = when {
                        deal.isFullyRecovered -> Color(0xFF10B981).copy(alpha = 0.15f)
                        deal.totalRecovered.value > 0 -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    }
                    val debtColor = when {
                        deal.isFullyRecovered -> Color(0xFF059669)
                        deal.totalRecovered.value > 0 -> Color(0xFF2563EB)
                        else -> Color(0xFFD97706)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(debtBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = debtLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = debtColor,
                            ),
                        )
                    }
                } else {
                    val roi = deal.roiPercentage
                    val isZero = deal.netProfitLoss.value == 0L
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isZero) tokens.surfaceSoft
                                else if (roi >= 0) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (isZero) "0.0%" else String.format(java.util.Locale.US, "%+.1f%%", roi),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isZero) tokens.textSecondary else if (roi >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                            ),
                        )
                    }
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { deal.recoveryProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF10B981),
                    trackColor = tokens.border,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (isLending) "Đã cho vay: ${deal.totalCapitalOutlay.value.toVnd()}" else "Vốn xuất: ${deal.totalCapitalOutlay.value.toVnd()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tokens.textSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                    Text(
                        text = "Đã thu: ${(deal.recoveryProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.textPrimary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            HorizontalDivider(color = tokens.border, thickness = 0.5.dp)

            // Row 3: Vốn/Dư nợ còn lại & Lợi nhuận / Lãi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isLending) "Dư nợ còn:" else "Vốn còn lại:",
                        style = MaterialTheme.typography.bodySmall.copy(color = tokens.textSecondary),
                    )
                    Text(
                        text = deal.remainingCapital.value.toVnd(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (deal.remainingCapital.value > 0) Color(0xFFF59E0B) else tokens.textSecondary,
                        ),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isLending) "Tiền lãi:" else "Lãi ròng:",
                        style = MaterialTheme.typography.bodySmall.copy(color = tokens.textSecondary),
                    )
                    val profit = deal.netProfitLoss.value
                    Text(
                        text = if (profit > 0) "+${profit.toVnd()}" else profit.toVnd(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                profit > 0 -> if (isLending) Color(0xFF8B5CF6) else Color(0xFF10B981)
                                profit < 0 -> Color(0xFFEF4444)
                                else -> tokens.textSecondary
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDealsView(
    tab: DealTab,
    onCreateClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(tokens.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = tokens.textSecondary,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = if (tab == DealTab.ACTIVE) "Chưa có thương vụ đang chạy" else "Chưa có thương vụ nào hoàn tất",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = tokens.textPrimary,
            ),
        )
        Text(
            text = if (tab == DealTab.ACTIVE) "Tạo Deal để theo dõi vốn xuất và lợi nhuận ròng chuẩn xác" else "Các Deal sau khi chốt sổ sẽ được lưu trữ tại đây",
            style = MaterialTheme.typography.bodySmall.copy(
                color = tokens.textSecondary,
            ),
        )
        if (tab == DealTab.ACTIVE) {
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Tạo Thương Vụ Đầu Tiên")
            }
        }
    }
}
