package com.finlux.app.presentation.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.home.classic.ClassicHomeScreen
import com.finlux.app.presentation.home.modern.ModernHomeScreen

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernHomeScreen(
            onNavigate = onNavigate,
            onAdd = onAdd,
            onNotifications = onNotifications,
            viewModel = viewModel,
        )
    }
}
