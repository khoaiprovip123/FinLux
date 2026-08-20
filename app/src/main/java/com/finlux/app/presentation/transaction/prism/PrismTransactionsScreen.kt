package com.finlux.app.presentation.transaction.prism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.transaction.DeleteTransactionConfirmDialog
import com.finlux.app.presentation.transaction.TransactionActionDialog
import com.finlux.app.presentation.transaction.TransactionDetailSheet
import com.finlux.app.presentation.transaction.TransactionFilter
import com.finlux.app.presentation.transaction.TransactionsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PrismTransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val transactions = viewModel.transactions.collectAsStateWithLifecycle().value
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val filter = viewModel.filter.collectAsStateWithLifecycle().value
    val total = transactions.sumOf { it.amount.value }
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current

    var viewingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var actionTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }

    LaunchedEffect(Unit) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

    val isRootTab = onNavigate != null && onAdd != null

    Scaffold(
        topBar = {
            // Fixed Top Header matching mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.background)
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isRootTab) "Lịch sử thu chi" else "Giao dịch",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tokens.onSurface,
                )

                IconButton(
                    onClick = { /* Filter menu */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Bộ lọc",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        bottomBar = {
            if (isRootTab) {
                MainBottomBar(Route.Transactions.value, onNavigate!!, onAdd!!)
            }
        },
        containerColor = tokens.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 1. Segmented Filter Pills Bar (Tất cả, Thu nhập, Chi tiêu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrismFilterPill(
                    label = "Tất cả",
                    icon = Icons.Default.GridView,
                    isSelected = filter == TransactionFilter.ALL,
                    activeColor = Color(0xFF3B5DF8),
                    onClick = { viewModel.filter.value = TransactionFilter.ALL },
                    modifier = Modifier.weight(1f),
                )

                PrismFilterPill(
                    label = "Thu nhập",
                    icon = Icons.Default.ArrowDownward,
                    isSelected = filter == TransactionFilter.INCOME,
                    activeColor = Color(0xFF16A34A),
                    onClick = { viewModel.filter.value = TransactionFilter.INCOME },
                    modifier = Modifier.weight(1f),
                )

                PrismFilterPill(
                    label = "Chi tiêu",
                    icon = Icons.Default.ArrowUpward,
                    isSelected = filter == TransactionFilter.EXPENSE,
                    activeColor = Color(0xFFDC2626),
                    onClick = { viewModel.filter.value = TransactionFilter.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(6.dp))

            // 2. Transaction List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = if (isRootTab) 140.dp else 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Summary Bento Banner Card
                item {
                    PrismSummaryBentoBanner(
                        filter = filter,
                        totalAmount = total,
                        itemCount = transactions.size,
                    )
                }

                // Transaction Items or Empty State
                if (transactions.isEmpty()) {
                    item {
                        FinluxEmptyState(
                            title = "Không có giao dịch nào",
                            description = "Không tìm thấy giao dịch phù hợp với bộ lọc hiện tại.",
                        )
                    }
                } else {
                    items(
                        items = transactions,
                        key = { it.id },
                    ) { tx ->
                        val category = tx.categoryId?.let { categories[it] }
                        val wallet = tx.walletId.let { wallets[it] }

                        PrismTransactionCardItem(
                            transaction = tx,
                            category = category,
                            wallet = wallet,
                            onClick = { viewingTransaction = tx },
                            onLongClick = { actionTransaction = tx },
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    viewingTransaction?.let { tx ->
        val category = tx.categoryId?.let { categories[it] }
        val wallet = tx.walletId.let { wallets[it] }

        TransactionDetailSheet(
            transaction = tx,
            category = category,
            wallet = wallet,
            onDismiss = { viewingTransaction = null },
            onEdit = {
                viewingTransaction = null
                onEditTransaction?.invoke(tx)
            },
            onDelete = {
                viewingTransaction = null
                pendingDelete = tx
            },
        )
    }

    // Action Menu Dialog (Edit / Delete)
    actionTransaction?.let { tx ->
        val category = tx.categoryId?.let { categories[it] }

        TransactionActionDialog(
            transaction = tx,
            category = category,
            onDismiss = { actionTransaction = null },
            onEdit = {
                actionTransaction = null
                onEditTransaction?.invoke(tx)
            },
            onDelete = {
                actionTransaction = null
                pendingDelete = tx
            },
        )
    }

    // Delete Confirmation Dialog
    pendingDelete?.let { tx ->
        DeleteTransactionConfirmDialog(
            transaction = tx,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(tx)
                pendingDelete = null
            },
        )
    }
}

/**
 * Filter Pill Button matching the mockup
 */
@Composable
private fun PrismFilterPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) activeColor else tokens.surface,
        border = if (isSelected) null else BorderStroke(1.dp, tokens.onSurface.copy(alpha = 0.08f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else activeColor,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isSelected) Color.White else tokens.onSurface,
            )
        }
    }
}

/**
 * Summary Bento Banner Card with 3D Financial Ledger Illustration
 */
@Composable
private fun PrismSummaryBentoBanner(
    filter: TransactionFilter,
    totalAmount: Long,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val summaryTitle = when (filter) {
        TransactionFilter.ALL -> "Tổng giá trị giao dịch"
        TransactionFilter.INCOME -> "Tổng thu trong kỳ"
        TransactionFilter.EXPENSE -> "Tổng chi trong kỳ"
    }

    val amountColor = when (filter) {
        TransactionFilter.ALL -> Color(0xFF2563EB)
        TransactionFilter.INCOME -> Color(0xFF16A34A)
        TransactionFilter.EXPENSE -> Color(0xFFDC2626)
    }

    val bannerBg = when (filter) {
        TransactionFilter.ALL -> listOf(Color(0xFFEFF4FE), Color(0xFFE9F0FD), Color(0xFFF5F2FE))
        TransactionFilter.INCOME -> listOf(Color(0xFFECFDF5), Color(0xFFE6F9F0), Color(0xFFF0FDF4))
        TransactionFilter.EXPENSE -> listOf(Color(0xFFFEF2F2), Color(0xFFFEECEB), Color(0xFFFFF1F2))
    }

    val accentGlowColor = when (filter) {
        TransactionFilter.ALL -> Color(0xFF6366F1)
        TransactionFilter.INCOME -> Color(0xFF10B981)
        TransactionFilter.EXPENSE -> Color(0xFFF43F5E)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, accentGlowColor.copy(alpha = if (tokens.isDark) 0.28f else 0.18f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = accentGlowColor.copy(alpha = 0.08f),
                spotColor = accentGlowColor.copy(alpha = 0.14f),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (tokens.isDark) {
                            listOf(Color(0xFF1E1E38), Color(0xFF151528), Color(0xFF201B3E))
                        } else {
                            bannerBg
                        },
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Column: Text & Amount
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = summaryTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                        ),
                    )

                    // Extra Bold Amount
                    Text(
                        text = formatVndAmount(totalAmount),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.8).sp,
                        ),
                        color = amountColor,
                    )

                    // Count of items pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "$itemCount giao dịch hiển thị",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color(0xFF6B7280),
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Right Column: 3D Spatial Transaction Ledger Graphic
                Prism3DTransactionIllustration(
                    filter = filter,
                    modifier = Modifier.size(width = 112.dp, height = 88.dp),
                )
            }
        }
    }
}

/**
 * 3D Spatial Illustration for Transaction History
 */
@Composable
private fun Prism3DTransactionIllustration(
    filter: TransactionFilter,
    modifier: Modifier = Modifier,
) {
    val cardGradient = when (filter) {
        TransactionFilter.ALL -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF4338CA))
        TransactionFilter.INCOME -> listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
        TransactionFilter.EXPENSE -> listOf(Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFFBE123C))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // 1. Ambient Luminous Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cardGradient.first().copy(alpha = 0.35f),
                        Color.Transparent,
                    ),
                ),
                radius = size.minDimension * 0.65f,
            )
        }

        // 2. Back 3D Layer: Frosted Glass Invoice / Receipt Sheet (tilted left)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(width = 68.dp, height = 72.dp)
                .graphicsLayer(
                    rotationZ = -14f,
                    translationX = -12f,
                    translationY = -2f,
                    cameraDistance = 12f,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Receipt header line
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 4.dp)
                        .background(cardGradient.first().copy(alpha = 0.45f), RoundedCornerShape(2.dp)),
                )
                // Receipt item lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .background(Color.LightGray.copy(alpha = 0.50f), RoundedCornerShape(2.dp)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(3.dp)
                        .background(Color.LightGray.copy(alpha = 0.40f), RoundedCornerShape(2.dp)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(3.dp)
                        .background(Color.LightGray.copy(alpha = 0.40f), RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.weight(1f))
                // Stamp pill
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 5.dp)
                        .background(cardGradient.first().copy(alpha = 0.60f), RoundedCornerShape(3.dp)),
                )
            }
        }

        // 3. Front 3D Layer: Metallic Holographic Card (tilted right)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.85f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(width = 76.dp, height = 54.dp)
                .graphicsLayer(
                    rotationZ = 10f,
                    translationX = 10f,
                    translationY = 6f,
                    cameraDistance = 12f,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = cardGradient,
                            start = Offset(0f, 0f),
                            end = Offset(200f, 200f),
                        ),
                    )
                    .padding(6.dp),
            ) {
                // Gold EMV Chip
                Box(
                    modifier = Modifier
                        .size(12.dp, 9.dp)
                        .align(Alignment.TopStart)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFDF00), Color(0xFFD4AF37))),
                            RoundedCornerShape(3.dp),
                        )
                        .border(0.5.dp, Color(0xFFB8860B), RoundedCornerShape(3.dp)),
                )

                // Contactless Wave lines
                Canvas(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd),
                ) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.75f),
                        startAngle = -45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.45f),
                        startAngle = -45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(3.dp.toPx(), 0f),
                    )
                }

                // Dual VIP Rings
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy((-4).dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(Color.White.copy(alpha = 0.40f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(Color.White.copy(alpha = 0.65f), CircleShape),
                    )
                }
            }
        }

        // 4. Foreground: Floating Golden Coin (₫)
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.2.dp, Color(0xFFFFF3B0)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer(
                    translationX = (-4).dp.value,
                    translationY = 2.dp.value,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFEA79),
                                Color(0xFFF59E0B),
                                Color(0xFFB45309),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "₫",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5A2A00),
                )
            }
        }

        // 5. Sparkle Accent (Top-Right)
        Text(
            text = "✦",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer(translationY = (-6).dp.value, translationX = 4.dp.value),
        )
    }
}

/**
 * Transaction Card Item matching the mockup
 */
@Composable
private fun PrismTransactionCardItem(
    transaction: FinanceTransaction,
    category: Category?,
    wallet: Wallet?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = category?.let { colorFromHex(it.colorHex) } ?: if (isIncome) IncomeGreen else ExpenseRed

    val title = transaction.note.ifBlank {
        category?.name ?: if (isIncome) "Thu nhập" else "Chi tiêu"
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm") }
    val dateText = remember(transaction.date) {
        dateFormatter.format(transaction.date.atZone(ZoneId.systemDefault()))
    }
    val walletName = wallet?.name ?: "Ví chính"

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (tokens.isDark) Color(0xFF1E1E2D) else Color.White,
        border = BorderStroke(1.dp, if (tokens.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF3F4F6)),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category Icon (rounded square with soft tint)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = if (tokens.isDark) 0.18f else 0.12f),
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category?.let { categoryIcon(it.icon) } ?: Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Center Column: Title + Date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = tokens.onSurface,
                    maxLines = 1,
                )

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                    ),
                )
            }

            Spacer(Modifier.width(8.dp))

            // Right Column: Amount + Wallet
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = (if (isIncome) "+" else "-") + formatVndAmount(transaction.amount.value),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFEF4444),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = walletName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
