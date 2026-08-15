package com.finlux.app.presentation.transaction

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.transaction.classic.ClassicTransactionsScreen
import com.finlux.app.presentation.transaction.modern.ModernTransactionsScreen

@Composable
fun TransactionsScreen(
    onNavigate: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicTransactionsScreen(
            onNavigate = onNavigate,
            onBack = onBack,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernTransactionsScreen(
            onNavigate = onNavigate,
            onBack = onBack,
            viewModel = viewModel,
        )
    }
}
