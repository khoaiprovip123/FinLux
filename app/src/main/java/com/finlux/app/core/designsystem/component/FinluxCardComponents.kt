package com.finlux.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

/**
 * Standard Soft Surface Card (FinLux Prism Spec 6 & 24)
 * Used for 80% data clarity, minimal glass overhead.
 */
@Composable
fun FinluxSoftCard(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(radius)
    val border = borderColor ?: tokens.onSurface.copy(alpha = tokens.borderAlpha)

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onClick,
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = tokens.elevation / 2,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .clip(shape)
            .background(tokens.surfaceSoft)
            .border(BorderStroke(1.dp, border), shape)
            .then(clickableMod)
            .padding(padding),
        content = content,
    )
}

/**
 * Standard Hero Card for Net Worth / Main Assets (FinLux Prism Spec 8.2 & 24)
 */
@Composable
fun FinluxHeroCard(
    title: String,
    amountText: String,
    modifier: Modifier = Modifier,
    deltaText: String? = null,
    isPositiveDelta: Boolean = true,
    isAmountVisible: Boolean = true,
    onToggleVisibility: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(tokens.radius.heroCard)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = tokens.elevation,
                shape = shape,
                ambientColor = tokens.primary.copy(alpha = 0.22f),
                spotColor = tokens.primary.copy(alpha = 0.28f),
            )
            .clip(shape)
            .background(tokens.heroBrush)
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                shape,
            )
            .padding(22.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title.uppercase(),
                    style = FinluxTextStyles.MicroLabel,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )

                if (onToggleVisibility != null) {
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isAmountVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isAmountVisible) "Ẩn số dư" else "Hiện số dư",
                            tint = Color.White.copy(alpha = 0.90f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isAmountVisible) amountText else "••••••••",
                style = FinluxTextStyles.DisplayAmount,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            if (deltaText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.radius.smallChip))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = if (isPositiveDelta) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = deltaText,
                        style = FinluxTextStyles.MicroLabel,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            extraContent?.invoke()
        }
    }
}

/**
 * Metric Card for Income/Expense/Cashflow (FinLux Prism Spec 8.3 & 24)
 */
@Composable
fun FinluxMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    FinluxSoftCard(
        modifier = modifier,
        radius = 20.dp,
        padding = 14.dp,
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (icon != null) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(24.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
                Text(
                    text = title,
                    style = FinluxTextStyles.Caption,
                    color = LocalFinluxTokens.current.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Text(
                text = value,
                style = FinluxTextStyles.SectionTitle.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = accentColor,
                maxLines = 1,
            )

            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = FinluxTextStyles.MicroLabel,
                    color = accentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Rule-Based Insight Card (FinLux Prism Spec 10 & 24)
 */
@Composable
fun FinluxInsightCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.AutoAwesome,
    accentColor: Color = FinluxColors.PrimaryViolet,
) {
    val tokens = LocalFinluxTokens.current

    FinluxSoftCard(
        modifier = modifier.fillMaxWidth(),
        radius = 20.dp,
        padding = 16.dp,
        borderColor = accentColor.copy(alpha = 0.20f),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = FinluxTextStyles.CardTitle,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = FinluxTextStyles.Body,
                    color = tokens.onSurfaceVariant,
                )
            }
        }
    }
}
