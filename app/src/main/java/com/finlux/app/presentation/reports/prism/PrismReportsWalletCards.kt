package com.finlux.app.presentation.reports.prism

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import com.finlux.app.core.time.FinanceTime
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import com.finlux.app.presentation.reports.BudgetReportItem
import com.finlux.app.presentation.reports.CategoryExpense
import com.finlux.app.presentation.reports.DebtReportItem
import com.finlux.app.presentation.reports.ExportReportDialog
import com.finlux.app.presentation.reports.GoalReportItem
import com.finlux.app.presentation.reports.ReportPeriod
import com.finlux.app.presentation.reports.ReportsUiState
import com.finlux.app.presentation.reports.ReportsViewModel
import com.finlux.app.presentation.reports.WalletReportItem
import com.finlux.app.presentation.reports.WalletSpendingDetail
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
internal fun PrismWalletsHeroCard(state: ReportsUiState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinluxPalette.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = FinluxPalette.CFF0EA5E9.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FinluxPalette.CFF0284C7,
                            FinluxPalette.CFF0EA5E9,
                            FinluxPalette.CFF0369A1,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BÁO CÁO TÀI SẢN & VÍ", style = MaterialTheme.typography.labelSmall, color = FinluxPalette.White.copy(alpha = 0.8f))
                Text(
                    text = formatVndAmount(state.totalNetWorth),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = FinluxPalette.White,
                )
                Text("Tài sản ròng (Tổng số dư ví - Tổng dư nợ)", style = MaterialTheme.typography.bodySmall, color = FinluxPalette.White.copy(alpha = 0.9f))

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tổng tài sản ví", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalAssets), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFF4ADE80)
                    }
                    Column {
                        Text("Tổng dư nợ", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text(formatVndAmount(state.totalDebtRemaining), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.CFFF87171)
                    }
                    Column {
                        Text("Số lượng ví", fontSize = 11.5.sp, color = FinluxPalette.White.copy(alpha = 0.75f))
                        Text("${state.walletReportItems.size} ví", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinluxPalette.White)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismWalletFilterSelector(
    wallets: List<Wallet>,
    selectedWalletId: String?,
    onSelectWallet: (String?) -> Unit,
) {
    if (wallets.isEmpty()) return
    val tokens = LocalFinluxTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Lọc theo ví:",
                style = MaterialTheme.typography.labelSmall,
                color = FinluxPalette.CFF6B7280,
                fontWeight = FontWeight.SemiBold,
            )
            if (selectedWalletId != null) {
                Text(
                    "Xóa lọc ví",
                    fontSize = 11.5.sp,
                    color = tokens.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSelectWallet(null) },
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            // "Tất cả ví" chip
            item {
                val isSelected = selectedWalletId == null
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) tokens.primary.copy(alpha = 0.2f) else if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) tokens.primary else if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB,
                    ),
                    modifier = Modifier.clickable { onSelectWallet(null) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isSelected) tokens.primary else tokens.onSurface.copy(alpha = 0.7f),
                        )
                        Text(
                            "Tất cả ví",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) tokens.primary else tokens.onSurface,
                        )
                    }
                }
            }

            // Từng ví
            items(wallets, key = { it.id }) { wallet ->
                val isSelected = selectedWalletId == wallet.id
                val accent = colorFromHex(wallet.colorHex)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) accent.copy(alpha = 0.2f) else if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) accent else if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB,
                    ),
                    modifier = Modifier.clickable { onSelectWallet(wallet.id) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accent, CircleShape),
                        )
                        Text(
                            wallet.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) accent else tokens.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismWalletSpendingDistributionCard(
    state: ReportsUiState,
    onSelectWallet: (String?) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val totalExpense = state.walletSpendingDetails.sumOf { it.expenseInPeriod }
    if (totalExpense <= 0) return

    val spendingWallets = state.walletSpendingDetails.filter { it.expenseInPeriod > 0 }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Cơ cấu chi tiêu các ví trong kỳ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Text(
                    formatVndAmount(totalExpense),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = FinluxPalette.CFFEF4444,
                )
            }

            // Thanh đa phân đoạn thể hiện tỷ trọng chi giữa các ví
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            ) {
                spendingWallets.forEach { detail ->
                    val weight = (detail.expenseInPeriod.toFloat() / totalExpense.toFloat()).coerceAtLeast(0.01f)
                    val color = colorFromHex(detail.wallet.colorHex)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(color),
                    )
                }
            }

            // Legend pills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                spendingWallets.forEach { detail ->
                    val isSelected = state.selectedWalletId == detail.wallet.id
                    val accent = colorFromHex(detail.wallet.colorHex)
                    val percent = (detail.expenseShareOfTotal * 100).roundToInt()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accent.copy(alpha = 0.2f) else if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.05f) else FinluxPalette.CFFF3F4F6,
                        border = BorderStroke(1.dp, if (isSelected) accent else FinluxPalette.Transparent),
                        modifier = Modifier.clickable {
                            if (isSelected) onSelectWallet(null) else onSelectWallet(detail.wallet.id)
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(accent, CircleShape))
                            Text(
                                "${detail.wallet.name}: $percent%",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = tokens.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrismWalletReportCard(
    item: WalletReportItem,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
