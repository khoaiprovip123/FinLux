package com.finlux.app.presentation.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.home.classic.ClassicHomeScreen
import com.finlux.app.presentation.home.modern.ModernHomeScreen

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            onSelectTransaction = onSelectTransaction,
            onActionTransaction = onActionTransaction,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY, AppUiStyle.PRISM -> ModernHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            onSelectTransaction = onSelectTransaction,
            onActionTransaction = onActionTransaction,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
        )
    }
}
