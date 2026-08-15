package com.finlux.app.core.designsystem

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Compact premium panel used by the 6–8 screen set from the approved visual reference. */
@Composable
fun FinluxPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .58f),
    cornerRadius: Dp = 18.dp,
    padding: PaddingValues = PaddingValues(14.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier,
        mode = LiquidGlassMode.REGULAR,
        tint = containerColor.takeUnless { it == MaterialTheme.colorScheme.surface.copy(alpha = .96f) },
        shape = RoundedCornerShape(cornerRadius),
        elevation = 5.dp,
        padding = padding,
        onClick = onClick,
        content = content,
    )
}
