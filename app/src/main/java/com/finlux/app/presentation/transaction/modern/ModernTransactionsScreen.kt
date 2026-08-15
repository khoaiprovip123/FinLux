package com.finlux.app.presentation.transaction.modern

import com.finlux.app.presentation.transaction.*
import com.finlux.app.core.designsystem.modern.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.modern.GlassCard
import com.finlux.app.core.designsystem.modern.GlassAlertDialog
import com.finlux.app.core.designsystem.modern.GlassTopBar
import com.finlux.app.core.designsystem.GradientHeroCard
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.home.toVnd
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.finlux.app.core.navigation.Route

@Composable
fun ModernTransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val transactions = viewModel.transactions.collectAsStateWithLifecycle().value
    val filter = viewModel.filter.collectAsStateWithLifecycle().value
    val total = transactions.sumOf { it.amount.value }
    val snackbar = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
    LaunchedEffect(Unit) { viewModel.messages.collect { snackbar.showSnackbar(it) } }

    Box(Modifier.fillMaxSize()) {
        com.finlux.app.core.designsystem.modern.FinluxStyleBackdrop(Modifier.fillMaxSize())
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Giao dịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onBack?.invoke() ?: onNavigate?.invoke(Route.Home.value) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                        }
                    },
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TransactionFilter.entries.forEach { option ->
                        com.finlux.app.core.designsystem.modern.LiquidGlassCapsule(
                            selected = filter == option,
                            onClick = { viewModel.filter.value = option },
                            modifier = Modifier.weight(1f),
                            accentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                option.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (filter == option) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    item {
                        GlassCard(
                            Modifier.fillMaxWidth(),
                            mode = com.finlux.app.core.designsystem.modern.LiquidGlassMode.CLEAR,
                            tint = MaterialTheme.colorScheme.primary,
                            padding = PaddingValues(18.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${filter.heading} trong kỳ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(total.toVnd(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("${transactions.size} giao dịch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        val isIncome = transaction.type == TransactionType.INCOME
                        val rowAccent = if (isIncome) IncomeGreen else ExpenseRed
                        GlassCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.background(rowAccent.copy(alpha = .14f), RoundedCornerShape(14.dp))) {
                                    Icon(Icons.Default.Payments, null, Modifier.padding(10.dp).size(22.dp), tint = rowAccent)
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(transaction.note.ifBlank { if (isIncome) "Thu nhập" else "Chi tiêu" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy").format(transaction.date.atZone(ZoneId.systemDefault())),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FinluxTextSecondary,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text((if (isIncome) "+" else "-") + transaction.amount.value.toVnd(), color = rowAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    IconButton(onClick = { pendingDelete = transaction }) { Icon(Icons.Default.DeleteOutline, "Xóa giao dịch", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { transaction ->
        GlassAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa giao dịch?") },
            text = { Text("Số dư ví sẽ được hoàn lại tự động. Thao tác này không thể hoàn tác.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(transaction); pendingDelete = null }) { Text("Xóa", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Hủy") } },
        )
    }
}

private val TransactionFilter.label: String
    get() = when (this) {
        TransactionFilter.ALL -> "Tất cả"
        TransactionFilter.INCOME -> "Thu"
        TransactionFilter.EXPENSE -> "Chi"
    }

private val TransactionFilter.heading: String
    get() = when (this) {
        TransactionFilter.ALL -> "Tổng giao dịch"
        TransactionFilter.INCOME -> "Tổng thu"
        TransactionFilter.EXPENSE -> "Tổng chi"
    }
