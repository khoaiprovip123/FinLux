package com.finlux.app.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle

/**
 * Universal Adaptive Top Bar for Component-driven Architecture.
 * Automatically adapts typography, translucency, and action styling to [LocalAppUiStyle]:
 * - [AppUiStyle.CLASSIC_LIQUID]: Classic translucent glass top bar.
 * - [AppUiStyle.MODERN_LUXURY]: Modern optical blur glass top bar.
 * - [AppUiStyle.PRISM]: Minimalist, transparent status bar aligned with Prism typography.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxAdaptiveTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val uiStyle = LocalAppUiStyle.current
    val tokens = LocalFinluxTokens.current

    when (uiStyle) {
        AppUiStyle.CLASSIC_LIQUID -> {
            GlassTopBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
            )
        }
        AppUiStyle.MODERN_LUXURY -> {
            com.finlux.app.core.designsystem.modern.GlassTopBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
            )
        }
        AppUiStyle.PRISM -> {
            TopAppBar(
                modifier = modifier.statusBarsPadding(),
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = tokens.onSurface,
                    navigationIconContentColor = tokens.onSurface,
                    actionIconContentColor = tokens.onSurface,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        }
    }
}
