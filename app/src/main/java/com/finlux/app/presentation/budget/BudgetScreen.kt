package com.finlux.app.presentation.budget

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.budget.classic.ClassicBudgetScreen
import com.finlux.app.presentation.budget.modern.ModernBudgetScreen
import com.finlux.app.presentation.budget.prism.PrismBudgetScreen

@Composable
fun BudgetScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicBudgetScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernBudgetScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
        AppUiStyle.PRISM -> PrismBudgetScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
    }
}
