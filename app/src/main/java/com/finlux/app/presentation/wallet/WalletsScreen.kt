package com.finlux.app.presentation.wallet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.wallet.classic.ClassicWalletsScreen
import com.finlux.app.presentation.wallet.modern.ModernWalletsScreen
import com.finlux.app.presentation.wallet.prism.PrismWalletsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: ((String) -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    transferRequestKey: Int = 0,
    viewModel: WalletsViewModel = hiltViewModel(),
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicWalletsScreen(
            onBack = onBack,
            onNavigate = onNavigate,
            onAdd = onAdd,
            transferRequestKey = transferRequestKey,
            viewModel = viewModel,
        )
        AppUiStyle.MODERN_LUXURY -> ModernWalletsScreen(
            onBack = onBack,
            onNavigate = onNavigate,
            onAdd = onAdd,
            transferRequestKey = transferRequestKey,
            viewModel = viewModel,
        )
        AppUiStyle.PRISM -> PrismWalletsScreen(
            onBack = onBack,
            onNavigate = onNavigate,
            onAdd = onAdd,
            transferRequestKey = transferRequestKey,
            viewModel = viewModel,
        )
    }
}
