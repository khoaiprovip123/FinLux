package com.finlux.app.core.designsystem.modern

import com.finlux.app.core.designsystem.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val FinluxBrandGradient = Brush.linearGradient(
    colors = listOf(FinluxPurple, FinluxBlue, FinluxCyan),
)

@Composable
fun GradientHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    LiquidGlassSurface(
        modifier = modifier,
        mode = LiquidGlassMode.CLEAR,
        tint = MaterialTheme.colorScheme.primary,
        shape = shape,
        elevation = 14.dp,
        padding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            FinluxPurple.copy(alpha = .82f),
                            FinluxBlue.copy(alpha = .78f),
                            FinluxCyan.copy(alpha = .72f),
                        ),
                    ),
                )
                .padding(20.dp),
            content = content,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        action?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color,
    supporting: String? = null,
) {
    GlassCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = accent.copy(alpha = .82f)) }
        }
    }
}
