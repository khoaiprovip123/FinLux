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
internal fun PrismWalletDetailBottomSheet(
    detail: WalletSpendingDetail,
    isFilterActive: Boolean,
    onFilterWallet: () -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val wallet = detail.wallet
    val accent = colorFromHex(wallet.colorHex)
    val typeName = when (wallet.type) {
        WalletType.CASH -> "Tiền mặt"
        WalletType.BANK -> "Ngân hàng"
        WalletType.EWALLET -> "Ví điện tử"
        WalletType.CARD -> "Thẻ tín dụng"
        WalletType.INVESTMENT -> "Đầu tư / Tiết kiệm"
        WalletType.OTHER -> "Khác"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.White,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = tokens.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(2.dp),
            ) {
                Box(Modifier.size(width = 36.dp, height = 4.dp))
            }
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Nhận diện ví
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accent.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = accent,
                        )
                    }
                    Column {
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = tokens.onSurface,
                        )
                        Text(
                            text = "$typeName • Số dư: ${formatVndAmount(detail.balance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinluxPalette.CFF6B7280,
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurfaceVariant,
                    )
                }
            }

            // 4 KPI cards (Chi tiêu, Thu nhập, Luân chuyển tiền, Biến động ví)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Thu nhập
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.05f) else FinluxPalette.CFFF0FDF4,
                        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFDCFCE7),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tổng thu nhập", fontSize = 11.sp, color = FinluxPalette.CFF10B981)
                            Text(
                                "+${formatVndAmount(detail.incomeInPeriod)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinluxPalette.CFF10B981,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Chi tiêu
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.05f) else FinluxPalette.CFFFEF2F2,
                        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFFEE2E2),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tổng chi tiêu", fontSize = 11.sp, color = FinluxPalette.CFFEF4444)
                            Text(
                                "-${formatVndAmount(detail.expenseInPeriod)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinluxPalette.CFFEF4444,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Luân chuyển tiền (Chuyển đi / Nhận chuyển)
                    val hasTransfer = detail.transferOutInPeriod > 0 || detail.transferInInPeriod > 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.05f) else FinluxPalette.CFFF0F9FF,
                        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE0F2FE),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Chuyển tiền", fontSize = 11.sp, color = FinluxPalette.CFF0284C7)
                            if (hasTransfer) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (detail.transferOutInPeriod > 0) {
                                        Text(
                                            "-${formatVndAmount(detail.transferOutInPeriod)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FinluxPalette.CFFF97316,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (detail.transferInInPeriod > 0) {
                                        Text(
                                            "+${formatVndAmount(detail.transferInInPeriod)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FinluxPalette.CFF0EA5E9,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "0 ₫",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }

                    // Biến động số dư ví
                    val isPositiveChange = detail.netWalletChange >= 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.05f) else if (isPositiveChange) FinluxPalette.CFFF0FDF4 else FinluxPalette.CFFFEF2F2,
                        border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else if (isPositiveChange) FinluxPalette.CFFDCFCE7 else FinluxPalette.CFFFEE2E2),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Biến động ví", fontSize = 11.sp, color = if (isPositiveChange) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444)
                            Text(
                                "${if (isPositiveChange) "+" else ""}${formatVndAmount(detail.netWalletChange)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositiveChange) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Thẻ tóm tắt biến động số dư thực tế
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.04f) else FinluxPalette.CFFF9FAFB,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tổng kết dòng tiền thực tế của ví",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.onSurface,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiền vào (Thu nhập + Nhận chuyển)", fontSize = 12.sp, color = FinluxPalette.CFF6B7280)
                        Text(
                            "+${formatVndAmount(detail.totalMoneyIn)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinluxPalette.CFF10B981,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiền ra (Chi tiêu + Chuyển đi)", fontSize = 12.sp, color = FinluxPalette.CFF6B7280)
                        Text(
                            "-${formatVndAmount(detail.totalMoneyOut)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinluxPalette.CFFEF4444,
                        )
                    }
                    HorizontalDivider(color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Biến động số dư ví trong kỳ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tokens.onSurface)
                        Text(
                            "${if (detail.netWalletChange >= 0) "+" else ""}${formatVndAmount(detail.netWalletChange)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (detail.netWalletChange >= 0) FinluxPalette.CFF10B981 else FinluxPalette.CFFEF4444,
                        )
                    }
                }
            }

            // Tỷ trọng chi tiêu card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.04f) else FinluxPalette.CFFF9FAFB,
                border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tỷ trọng trên tổng chi toàn app", fontSize = 11.5.sp, color = FinluxPalette.CFF6B7280)
                        Text(
                            "${(detail.expenseShareOfTotal * 100).roundToInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.primary,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { detail.expenseShareOfTotal.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = tokens.primary,
                        trackColor = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.08f) else FinluxPalette.CFFE5E7EB,
                    )
                }
            }

            // Phân bổ chi tiêu theo danh mục
            Text(
                "Phân bổ chi tiêu theo danh mục (${detail.expensesByCategory.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.onSurface,
            )

            if (detail.expensesByCategory.isEmpty()) {
                Text(
                    "Ví này chưa có khoản chi tiêu nào trong kỳ đã chọn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinluxPalette.CFF6B7280,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.expensesByCategory.forEach { catExp ->
                        val cat = catExp.category
                        val catColor = colorFromHex(cat?.colorHex ?: "#6B7280")
                        val catPercent = (catExp.percentage * 100).roundToInt()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.03f) else FinluxPalette.CFFFAFAFA,
                            border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.06f) else FinluxPalette.CFFEEEEEE),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(catColor.copy(alpha = 0.16f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                categoryIcon(cat?.icon.orEmpty()),
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp),
                                                tint = catColor,
                                            )
                                        }
                                        Text(
                                            cat?.name ?: "Khác",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = tokens.onSurface,
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatVndAmount(catExp.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = FinluxPalette.CFFEF4444,
                                        )
                                        Text(
                                            "$catPercent% của ví",
                                            fontSize = 10.5.sp,
                                            color = FinluxPalette.CFF6B7280,
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { catExp.percentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp)),
                                    color = catColor,
                                    trackColor = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.06f) else FinluxPalette.CFFE5E7EB,
                                )
                            }
                        }
                    }
                }
            }

            // Nguồn thu nạp vào ví nếu có
            if (detail.incomeByCategory.isNotEmpty()) {
                Text(
                    "Nguồn thu nạp vào ví (${detail.incomeByCategory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.onSurface,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    detail.incomeByCategory.forEach { catInc ->
                        val cat = catInc.category
                        val catColor = colorFromHex(cat?.colorHex ?: "#10B981")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.03f) else FinluxPalette.CFFFAFAFA,
                            border = BorderStroke(1.dp, if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.06f) else FinluxPalette.CFFEEEEEE),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(catColor.copy(alpha = 0.16f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            categoryIcon(cat?.icon.orEmpty()),
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = catColor,
                                        )
                                    }
                                    Text(
                                        cat?.name ?: "Khác",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = tokens.onSurface,
                                    )
                                }
                                Text(
                                    "+${formatVndAmount(catInc.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = FinluxPalette.CFF10B981,
                                )
                            }
                        }
                    }
                }
            }

            // Lịch sử giao dịch ví trong kỳ
            Text(
                "Lịch sử giao dịch ví trong kỳ (${detail.transactions.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.onSurface,
            )

            if (detail.transactions.isEmpty()) {
                Text(
                    "Không có giao dịch phát sinh từ ví này trong kỳ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinluxPalette.CFF6B7280,
                )
            } else {
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    detail.transactions.take(20).forEach { tx ->
                        val isIncome = tx.type == TransactionType.INCOME
                        val isTransferOut = tx.type == TransactionType.TRANSFER_OUT
                        val isTransferIn = tx.type == TransactionType.TRANSFER_IN
                        val isTransfer = isTransferOut || isTransferIn
                        val txDateStr = tx.date.atZone(FinanceTime.VIETNAM_ZONE).format(dateFormatter)

                        val amountColor = when {
                            isIncome -> FinluxPalette.CFF10B981
                            isTransferIn -> FinluxPalette.CFF0EA5E9
                            isTransferOut -> FinluxPalette.CFFF97316
                            else -> FinluxPalette.CFFEF4444
                        }
                        val prefix = when {
                            isIncome || isTransferIn -> "+"
                            else -> "-"
                        }
                        val defaultLabel = when {
                            isTransferOut -> "Chuyển tiền sang ví khác"
                            isTransferIn -> "Nhận tiền chuyển từ ví khác"
                            isIncome -> "Thu nhập"
                            else -> "Chi tiêu"
                        }
                        val txIcon = when {
                            isTransfer -> Icons.Default.SwapHoriz
                            isIncome -> Icons.Default.ArrowDownward
                            else -> Icons.Default.ArrowUpward
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (tokens.isDark) FinluxPalette.White.copy(alpha = 0.02f) else FinluxPalette.CFFF9FAFB,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(amountColor.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = txIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = amountColor,
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            tx.note.ifBlank { defaultLabel },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = tokens.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            txDateStr,
                                            fontSize = 10.5.sp,
                                            color = FinluxPalette.CFF6B7280,
                                        )
                                    }
                                }
                                Text(
                                    "$prefix${formatVndAmount(tx.amount.value)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor,
                                )
                            }
                        }
                    }
                    if (detail.transactions.size > 15) {
                        Text(
                            "... và ${detail.transactions.size - 15} giao dịch khác",
                            fontSize = 11.sp,
                            color = FinluxPalette.CFF6B7280,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Action button: Lọc toàn bộ báo cáo theo ví này
            if (isFilterActive) {
                OutlinedButton(
                    onClick = onClearFilter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Bỏ lọc theo ví này", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = onFilterWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Lọc toàn bộ báo cáo theo ví này", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
