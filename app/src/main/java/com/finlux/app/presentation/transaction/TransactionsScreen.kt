package com.finlux.app.presentation.transaction

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.transaction.classic.ClassicTransactionsScreen
import com.finlux.app.presentation.transaction.modern.ModernTransactionsScreen
import com.finlux.app.presentation.transaction.prism.PrismTransactionsScreen

@Composable
fun TransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicTransactionsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernTransactionsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
        )
        AppUiStyle.PRISM -> PrismTransactionsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
        )
    }
}
