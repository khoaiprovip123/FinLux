package com.finlux.app.presentation.reports

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.reports.classic.ClassicReportsScreen
import com.finlux.app.presentation.reports.modern.ModernReportsScreen
import com.finlux.app.presentation.reports.prism.PrismReportsScreen

@Composable
fun ReportsScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicReportsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernReportsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
        AppUiStyle.PRISM -> PrismReportsScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onBack = onBack,
            viewModel = viewModel,
        )
    }
}
