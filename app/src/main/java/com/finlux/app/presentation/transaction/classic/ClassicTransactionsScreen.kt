package com.finlux.app.presentation.transaction.classic

import com.finlux.app.presentation.transaction.*

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
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
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
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
fun ClassicTransactionsScreen(
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
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TransactionFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { viewModel.filter.value = option },
                        label = { Text(option.label) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .13f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    GradientHeroCard(Modifier.fillMaxWidth()) {
                        Column {
                            Text("${filter.heading} trong kỳ", color = Color.White.copy(alpha = .8f))
                            Text(total.toVnd(), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            Text("${transactions.size} giao dịch", color = Color.White.copy(alpha = .78f))
                        }
                    }
                }
                items(transactions, key = { it.id }) { transaction ->
                    val isIncome = transaction.type == TransactionType.INCOME
                    val rowAccent = if (isIncome) IncomeGreen else ExpenseRed
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = rowAccent.copy(alpha = .12f)) {
                                Icon(Icons.Default.Payments, null, Modifier.padding(9.dp).size(20.dp), tint = rowAccent)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                                Text(transaction.note.ifBlank { if (isIncome) "Thu nhập" else "Chi tiêu" }, fontWeight = FontWeight.Bold)
                                Text(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy").format(transaction.date.atZone(ZoneId.systemDefault())),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FinluxTextSecondary,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text((if (isIncome) "+" else "-") + transaction.amount.value.toVnd(), color = rowAccent, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { pendingDelete = transaction }) { Icon(Icons.Default.DeleteOutline, "Xóa giao dịch", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { transaction ->
        AlertDialog(
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
