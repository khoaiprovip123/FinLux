package com.finlux.app.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Unified liquid aura backdrop for FinLux (Callstack Liquid Glass standard).
 */
@Composable
fun FinluxStyleBackdrop(
    modifier: Modifier = Modifier,
    auth: Boolean = false,
) {
    LiquidAuraBackdrop(modifier = modifier, auth = auth)
}
