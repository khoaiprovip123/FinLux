package com.finlux.app.presentation.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.home.classic.ClassicHomeScreen
import com.finlux.app.presentation.home.modern.ModernHomeScreen
import com.finlux.app.presentation.home.prism.PrismHomeScreen
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinViewModel
import com.finlux.app.presentation.savingspin.components.SavingSpinGameSheet

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    onSelectTransaction: ((FinanceTransaction) -> Unit)? = null,
    onActionTransaction: ((FinanceTransaction) -> Unit)? = null,
    onEditTransaction: ((FinanceTransaction) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    savingSpinViewModel: SavingSpinViewModel = hiltViewModel(),
) {
    val savingSpinState = savingSpinViewModel.uiState.collectAsStateWithLifecycle().value
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            onSelectTransaction = onSelectTransaction,
            onActionTransaction = onActionTransaction,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
            savingSpinState = savingSpinState,
            onSavingSpinAction = savingSpinViewModel::onAction,
        )
        AppUiStyle.MODERN_LUXURY -> ModernHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            onSelectTransaction = onSelectTransaction,
            onActionTransaction = onActionTransaction,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
            savingSpinState = savingSpinState,
            onSavingSpinAction = savingSpinViewModel::onAction,
        )
        AppUiStyle.PRISM -> PrismHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            onSelectTransaction = onSelectTransaction,
            onActionTransaction = onActionTransaction,
            onEditTransaction = onEditTransaction,
            viewModel = viewModel,
            savingSpinState = savingSpinState,
            onSavingSpinAction = savingSpinViewModel::onAction,
        )
    }
    SavingSpinGameSheet(state = savingSpinState, onAction = savingSpinViewModel::onAction)
}
