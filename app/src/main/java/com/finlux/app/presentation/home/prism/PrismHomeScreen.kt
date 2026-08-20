package com.finlux.app.presentation.home.prism

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.FinluxInsightCard
import com.finlux.app.core.designsystem.component.FinluxMetricCard
import com.finlux.app.core.designsystem.component.FinluxSectionHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.FinluxTransactionRow
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.HomeViewModel

@Composable
fun PrismHomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val totalBalance = state.wallets.sumOf { it.balance.value }
    val categories = state.categories.associateBy(Category::id)
    var showBalance by remember { mutableStateOf(true) }
    val tokens = LocalFinluxTokens.current

    Scaffold(
        bottomBar = { MainBottomBar(Route.Home.value, onNavigate, onAdd) },
        containerColor = tokens.background,
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(
                start = tokens.spacing.lg,
                end = tokens.spacing.lg,
                top = tokens.spacing.xs,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            // 1. App Header with User profile & Notifications
            item {
                PrismHomeHeader(
                    displayName = state.user?.displayName?.ifBlank { "Bạn" } ?: "Bạn",
                    photoUrl = state.user?.photoUrl,
                    onProfile = { onNavigate(Route.Settings.value) },
                    onNotifications = onNotifications,
                )
            }

            // 2. Hero Card (Total Net Worth, Net Cash Flow, Hide/Show balance)
            item {
                val net = state.summary.net
                val deltaText = if (net > 0) {
                    "+${formatVndAmount(net, isCompact = true)} dòng tiền"
                } else if (net < 0) {
                    "-${formatVndAmount(-net, isCompact = true)} dòng tiền"
                } else {
                    "0 đ dòng tiền"
                }

                FinluxHeroCard(
                    title = "Tổng số dư tài sản",
                    amountText = if (showBalance) formatVndAmount(totalBalance) else "••••••••",
                    deltaText = if (showBalance) deltaText else null,
                    isPositiveDelta = net >= 0,
                    isAmountVisible = showBalance,
                    onToggleVisibility = { showBalance = !showBalance },
                )
            }

            // 3. Bento Metric Cards (Income, Expense, Budget)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                ) {
                    FinluxMetricCard(
                        title = "Thu tháng này",
                        value = if (showBalance) formatVndAmount(state.summary.income.value, isCompact = true) else "••••",
                        accentColor = FinluxColors.IncomeGreen,
                        supportingText = "▲ Dòng tiền vào",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Route.Income.value) },
                    )
                    FinluxMetricCard(
                        title = "Chi tháng này",
                        value = if (showBalance) formatVndAmount(state.summary.expense.value, isCompact = true) else "••••",
                        accentColor = FinluxColors.ExpenseRed,
                        supportingText = "▼ Đang theo dõi",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Route.Expense.value) },
                    )
                }
            }

            // 4. Quick Action Bar (Bento grid 4 items)
            item {
                PrismQuickActionsBento(
                    onAddIncome = { onNavigate(Route.Income.value) },
                    onAddExpense = { onNavigate(Route.Expense.value) },
                    onWallets = { onNavigate(Route.Wallets.value) },
                    onBudget = { onNavigate(Route.Budget.value) },
                )
            }

            // 5. Intelligent Rule-Based Insight Card
            item {
                val expense = state.summary.expense.value
                val income = state.summary.income.value
                val insightTitle = "Góc nhìn tài chính FinLux"
                val insightMsg = when {
                    expense > income && income > 0L ->
                        "Chi tiêu tháng này đang vượt thu nhập. Hãy cân nhắc xem xét lại các khoản chưa thiết yếu trong mục Báo cáo."
                    income > 0L && (expense.toDouble() / income.toDouble()) < 0.6 ->
                        "Tuyệt vời! Bạn đang duy trì tỷ lệ tiết kiệm trên 40% thu nhập trong tháng này."
                    state.wallets.isEmpty() ->
                        "Bạn chưa liên kết ví tiền nào. Hãy thêm ví để bắt đầu quản lý dòng tiền chuẩn xác."
                    else ->
                        "Mọi chỉ số tài chính đều ổn định. Chúc bạn có một ngày quản lý chi tiêu hiệu quả!"
                }

                FinluxInsightCard(
                    title = insightTitle,
                    description = insightMsg,
                    accentColor = FinluxColors.PrimaryBlue,
                )
            }

            // 6. Recent Transactions Section Header
            item {
                FinluxSectionHeader(
                    title = "Giao dịch gần đây",
                    action = "Xem tất cả",
                    onActionClick = { onNavigate(Route.Transactions.value) },
                )
            }

            // 7. Recent Transactions List
            if (state.transactions.isEmpty()) {
                item {
                    FinluxEmptyState(
                        title = "Chưa có giao dịch nào",
                        description = "Bấm '+' để ghi lại khoản thu chi đầu tiên của bạn.",
                    )
                }
            } else {
                items(
                    items = state.transactions.take(8),
                    key = { it.id },
                ) { tx ->
                    val category = tx.categoryId?.let { categories[it] }

                    FinluxTransactionRow(
                        transaction = tx,
                        category = category,
                        onClick = {
                            if (onSelectTransaction != null) {
                                onSelectTransaction(tx)
                            } else if (onEditTransaction != null) {
                                onEditTransaction(tx)
                            }
                        },
                        onLongClick = onActionTransaction?.let { { it(tx) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun PrismHomeHeader(
    displayName: String,
    photoUrl: String?,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = tokens.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            modifier = Modifier.clickable(onClick = onProfile),
        ) {
            FinluxUserAvatar(
                photoUrl = photoUrl,
                displayName = displayName,
                size = 44.dp,
                editable = false,
                onClick = onProfile,
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FinluxBrandMark(size = 14.dp)
                    Text(
                        text = "FINLUX",
                        style = FinluxTextStyles.MicroLabel.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = tokens.primary,
                    )
                }
                Text(
                    text = "Xin chào, $displayName",
                    style = FinluxTextStyles.CardTitle.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    ),
                    color = tokens.onSurface,
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = tokens.surfaceSoft,
            modifier = Modifier.size(42.dp),
        ) {
            IconButton(onClick = onNotifications) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Thông báo",
                    tint = tokens.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun PrismQuickActionsBento(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onWallets: () -> Unit,
    onBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FinluxSoftCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrismQuickActionButton(
                label = "Ghi thu",
                icon = Icons.Default.ArrowDownward,
                accent = FinluxColors.IncomeGreen,
                onClick = onAddIncome,
            )
            PrismQuickActionButton(
                label = "Ghi chi",
                icon = Icons.Default.ArrowUpward,
                accent = FinluxColors.ExpenseRed,
                onClick = onAddExpense,
            )
            PrismQuickActionButton(
                label = "Ví & Thẻ",
                icon = Icons.Default.AccountBalanceWallet,
                accent = FinluxColors.PrimaryBlue,
                onClick = onWallets,
            )
            PrismQuickActionButton(
                label = "Ngân sách",
                icon = Icons.Default.BarChart,
                accent = FinluxColors.BudgetViolet,
                onClick = onBudget,
            )
        }
    }
}

@Composable
private fun PrismQuickActionButton(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(tokens.radius.standardCard))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = label,
            style = FinluxTextStyles.Caption.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            ),
            color = tokens.onSurface,
        )
    }
}
