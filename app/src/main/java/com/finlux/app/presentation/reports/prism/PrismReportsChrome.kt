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
internal fun PrismReportsHeader(
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Báo cáo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = tokens.onSurface,
            )
            Text(
                text = "Tình hình tài chính của bạn",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = FinluxPalette.CFF6B7280,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // "Bộ lọc" Pill Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onFilterClick,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Bộ lọc",
                        tint = FinluxPalette.CFF5B4DFF,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Kỳ báo cáo",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = tokens.onSurface,
                    )
                }
            }

            // Export Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onExportClick,
                    ),
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Xuất báo cáo",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrismReportPrimaryTabsRow(
    selectedTab: ReportPrimaryTab,
    onTabSelected: (ReportPrimaryTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.surfaceSoft)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ReportPrimaryTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) tokens.primary else FinluxPalette.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onTabSelected(tab) },
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) tokens.onHero else tokens.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) tokens.onHero else tokens.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrismDeepDiveSubTabsRow(
    selectedSubTab: DeepDiveSubTab,
    onSubTabSelected: (DeepDiveSubTab) -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DeepDiveSubTab.entries) { subTab ->
            val isSelected = selectedSubTab == subTab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) tokens.primary.copy(alpha = if (tokens.isDark) 0.28f else 0.15f) else tokens.surfaceSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) tokens.primary.copy(alpha = 0.5f) else tokens.border.copy(alpha = 0.3f),
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onSubTabSelected(subTab) },
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = subTab.icon,
                        contentDescription = null,
                        tint = if (isSelected) tokens.primary else tokens.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = subTab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (isSelected) tokens.primary else tokens.onSurface,
                    )
                }
            }
        }
    }
}
