package com.finlux.app.presentation.components

import androidx.compose.runtime.Composable
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.presentation.components.classic.ClassicMainBottomBar
import com.finlux.app.presentation.components.modern.ModernMainBottomBar

@Composable
fun MainBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.CLASSIC_LIQUID -> ClassicMainBottomBar(
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            onAdd = onAdd,
        )
        AppUiStyle.MODERN_LUXURY, AppUiStyle.PRISM -> ModernMainBottomBar(
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            onAdd = onAdd,
        )
    }
}
