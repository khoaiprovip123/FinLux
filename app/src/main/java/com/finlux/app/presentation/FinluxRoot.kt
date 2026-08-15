package com.finlux.app.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTheme
import com.finlux.app.core.navigation.FinluxNavHost
import kotlinx.coroutines.flow.MutableStateFlow

/** Root is the only place where theme preference enters Composition (AGENTS.md rule 5). */
@Composable
fun FinluxRoot(
    viewModel: RootViewModel = hiltViewModel(),
    destinationFlow: MutableStateFlow<String?>? = null,
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
) {
    val theme = viewModel.theme.collectAsStateWithLifecycle().value
    val uiStyle = viewModel.uiStyle.collectAsStateWithLifecycle().value
    val uiPreferences = viewModel.uiPreferences.collectAsStateWithLifecycle().value
    FinluxTheme(
        preference = theme,
        uiStyle = uiStyle,
        uiPreferences = uiPreferences,
    ) {
        FinluxNavHost(
            selectedTheme = theme,
            onThemeSelected = viewModel::setTheme,
            selectedUiStyle = uiStyle,
            onUiStyleSelected = viewModel::setUiStyle,
            uiPreferences = uiPreferences,
            onUiPreferencesChanged = viewModel::setUiPreferences,
            destinationFlow = destinationFlow,
            payNotificationIdFlow = payNotificationIdFlow,
        )
    }
}
