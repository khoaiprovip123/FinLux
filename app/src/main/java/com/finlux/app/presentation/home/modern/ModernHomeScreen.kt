package com.finlux.app.presentation.home.modern

import com.finlux.app.presentation.home.*
import com.finlux.app.core.designsystem.modern.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxCyan
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WarningAmber
import com.finlux.app.core.designsystem.modern.WaterGlassCard
import com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.VisualStyle
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import com.finlux.app.presentation.savingspin.components.SavingSpinHomeCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ModernHomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    savingSpinState: SavingSpinUiState = SavingSpinUiState(),
    onSavingSpinAction: (SavingSpinAction) -> Unit = {},
) {
    NotificationPermissionHandler()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val totalBalance = state.netWorth
    val categories = state.categories.associateBy(Category::id)
    val showBalance = state.showBalance
    val visualStyle = LocalUiPreferences.current.visualStyle

    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
        ) { scaffoldPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = scaffoldPadding.calculateBottomPadding()),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                item {
                    ReferenceHeader(
                        name = state.user?.displayName ?: "Bạn",
                        photoUrl = state.user?.photoUrl,
                        unreadCount = state.unreadNotificationsCount,
                        onNotifications = onNotifications,
                        onProfile = { onNavigate(Route.Settings.value) },
                    )
                }
                item {
                    ReferenceBalanceHero(
                        amount = totalBalance,
                        net = state.summary.net,
                        style = visualStyle,
                        showBalance = showBalance,
                        onToggleBalance = viewModel::toggleBalanceVisibility,
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReferenceMetric(
                            title = "Thu tháng này",
                            value = if (showBalance) state.summary.income.value.toShortVnd() else "••••",
                            change = "▲ Dòng tiền vào",
                            accent = IncomeGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Route.Income.value) },
                        )
                        ReferenceMetric(
                            title = "Chi tháng này",
                            value = if (showBalance) state.summary.expense.value.toShortVnd() else "••••",
                            change = "▼ Đang theo dõi",
                            accent = ExpenseRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Route.Expense.value) },
                        )
                        ReferenceMetric(
                            title = "Ngân sách còn lại",
                            value = if (showBalance) state.budgetRemaining.toShortVnd() else "••••",
                            change = "${state.budgetRemainingPercent}% hạn mức",
                            accent = FinluxBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Route.Budget.value) },
                        )
                    }
                }
                if (savingSpinState.config.enabled && savingSpinState.config.showOnHome && savingSpinState.session != null) {
                    item {
                        SavingSpinHomeCard(
                            state = savingSpinState,
                            onOpen = { onSavingSpinAction(SavingSpinAction.OpenGame) },
                        )
                    }
                }
                item { ExpenseAnalytics(state.transactions, categories) }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Giao dịch gần nhất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Xem tất cả",
                            color = FinluxBlue,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { onNavigate(Route.Transactions.value) }.padding(vertical = 6.dp),
                        )
                    }
                }
                if (state.transactions.isEmpty()) {
                    item {
                        WaterGlassCard(Modifier.fillMaxWidth(), tint = FinluxBlue) {
                            Text("Chưa có giao dịch nào, nhấn + để bắt đầu", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(state.transactions.take(6), key = { it.id }) { transaction ->
                        ReferenceTransactionRow(
                            transaction = transaction,
                            category = categories[transaction.categoryId],
                            showBalance = showBalance,
                            onClick = { onSelectTransaction?.invoke(transaction) },
                            onLongClick = { onActionTransaction?.invoke(transaction) ?: onEditTransaction?.invoke(transaction) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceHeader(
    name: String,
    photoUrl: String?,
    unreadCount: Int,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FinluxBrandMark(size = 46.dp, framed = false)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text("Xin chào 👋", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onNotifications) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(Icons.Default.NotificationsNone, "Thông báo", tint = MaterialTheme.colorScheme.onSurface)
                if (unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = ExpenseRed,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(if (unreadCount > 9) 16.dp else 14.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        FinluxUserAvatar(photoUrl, name, 38.dp, onClick = onProfile)
    }
}

@Composable
private fun ExpenseAnalytics(transactions: List<FinanceTransaction>, categories: Map<String, Category>) {
    val groups = transactions.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (categoryId, rows) -> (categories[categoryId]?.name ?: "Khác") to rows.sumOf { it.amount.value } }
        .sortedByDescending { it.second }
    val total = groups.sumOf { it.second }
    val colors = listOf(FinluxPurple, Color(0xFFFF7A45), FinluxCyan, FinluxBlue, WarningAmber, Color(0xFF9B5CFF))
    WaterGlassCard(Modifier.fillMaxWidth(), tint = FinluxPurple, cornerRadius = 20) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chi tiêu theo danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (groups.isEmpty()) {
                Text("Chưa có dữ liệu chi tiêu", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(142.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stroke = 20.dp.toPx()
                            var start = -90f
                            groups.take(6).forEachIndexed { index, item ->
                                val sweep = item.second.toFloat() / total.coerceAtLeast(1L) * 360f
                                drawArc(
                                    color = colors[index % colors.size],
                                    startAngle = start,
                                    sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = Offset(stroke / 2, stroke / 2),
                                    size = Size(size.width - stroke, size.height - stroke),
                                    style = Stroke(stroke, cap = StrokeCap.Butt),
                                )
                                start += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(total.toShortVnd(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Tổng chi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        groups.take(6).forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(colors[index % colors.size], CircleShape))
                                Text(item.first, Modifier.weight(1f).padding(horizontal = 7.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text("${item.second * 100 / total.coerceAtLeast(1L)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceBalanceHero(
    amount: Long,
    net: Long,
    style: VisualStyle,
    showBalance: Boolean,
    onToggleBalance: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val gradient = listOf(Color(0xFF5B38FD), Color(0xFF387BFA), Color(0xFF06B6D4))
    val rimBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = .95f),
            FinluxCyan.copy(alpha = .60f),
            FinluxPurple.copy(alpha = .45f),
            Color.White.copy(alpha = .30f),
        ),
    )
    Box(
        Modifier.fillMaxWidth().height(152.dp)
            .shadow(20.dp, shape, ambientColor = Color(0xFF3B82F6).copy(alpha = .42f), spotColor = Color(0xFF6366F1).copy(alpha = .48f))
            .clip(shape)
            .background(Brush.linearGradient(gradient))
            .border(1.4.dp, rimBrush, shape),
    ) {
        HeroWaterDetails()
        Column(
            Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Tổng tài sản",
                    color = Color.White.copy(alpha = .88f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = .22f))
                        .border(1.dp, Color.White.copy(alpha = .55f), CircleShape)
                        .clickable(onClick = onToggleBalance),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        "Ẩn/hiện số dư",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            val balanceText = if (showBalance) amount.toVnd() else "•••••••• ₫"
            val balanceFontSize = when {
                balanceText.length >= 17 -> 20.sp
                balanceText.length >= 14 -> 23.sp
                else -> 28.sp
            }
            Text(
                text = balanceText,
                color = Color.White,
                fontSize = balanceFontSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Text(
                if (showBalance) "${if (net >= 0) "▲" else "▼"} ${net.toSignedVnd()} trong tháng" else "Dòng tiền trong tháng",
                color = Color.White.copy(alpha = .92f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun HeroWaterDetails() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color.White.copy(alpha = .10f), radius = size.height * .45f, center = Offset(size.width * .92f, size.height * .30f))
        drawCircle(Color.White.copy(alpha = .06f), radius = size.height * .25f, center = Offset(size.width * .80f, size.height * .80f))
    }
}

@Composable
private fun ReferenceMetric(
    title: String,
    value: String,
    change: String,
    accent: Color,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    WaterGlassCard(
        modifier = modifier.height(94.dp),
        tint = accent,
        onClick = onClick,
        padding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
        cornerRadius = 20,
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, fontWeight = FontWeight.Medium)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(change, color = accent, style = MaterialTheme.typography.labelSmall, maxLines = 1, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReferenceTransactionRow(
    transaction: FinanceTransaction,
    category: Category?,
    showBalance: Boolean,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val income = transaction.type == TransactionType.INCOME
    val accent = category?.let { colorFromHex(it.colorHex) } ?: if (income) IncomeGreen else ExpenseRed
    val zoneId = ZoneId.systemDefault()
    val txLocalDate = transaction.date.atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)
    val yesterday = today.minusDays(1)
    val timeStr = transaction.date.atZone(zoneId).format(DateTimeFormatter.ofPattern("HH:mm"))
    val dateLabel = when (txLocalDate) {
        today     -> "Hôm nay, $timeStr"
        yesterday -> "Hôm qua, $timeStr"
        else      -> transaction.date.atZone(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"))
    }
    val rowHeight = if (transaction.note.isNotBlank()) 68.dp else 58.dp
    val clickableModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = { onLongClick?.invoke() },
        )
    } else Modifier

    Row(
        Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(referenceTransactionIcon(transaction, category), null, Modifier.size(20.dp), tint = accent)
        }
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                category?.name ?: "Chuyển khoản",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                maxLines = 1,
            )
        }
        Text(
            if (!showBalance) "••••" else (if (income) "+" else "-") + transaction.amount.value.toVnd(),
            color = if (income) IncomeGreen else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .42f))
}

private fun referenceTransactionIcon(transaction: FinanceTransaction, category: Category?): ImageVector = when {
    transaction.type == TransactionType.TRANSFER_IN || transaction.type == TransactionType.TRANSFER_OUT -> Icons.Default.SwapHoriz
    category != null -> categoryIcon(category.icon)
    transaction.type == TransactionType.INCOME -> Icons.Default.Payments
    else -> when (category?.id) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        else -> Icons.Default.AccountBalanceWallet
    }
}

internal fun Long.toVnd(): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(this)

internal fun Long.toShortVnd(): String = when {
    kotlin.math.abs(this) >= 1_000_000 -> String.format(Locale.forLanguageTag("vi-VN"), "%.1f tr", this / 1_000_000.0)
    kotlin.math.abs(this) >= 1_000 -> String.format(Locale.forLanguageTag("vi-VN"), "%.0fK", this / 1_000.0)
    else -> "$this đ"
}

private fun Long.toSignedVnd(): String = (if (this >= 0) "+" else "") + toVnd()
