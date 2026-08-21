package com.finlux.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route

data class FinluxNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val DefaultFinluxNavTabs = listOf(
    FinluxNavTab(Route.Home.value, "Trang chủ", Icons.Default.Home),
    FinluxNavTab(Route.Transactions.value, "Lịch sử", Icons.AutoMirrored.Filled.ReceiptLong),
    FinluxNavTab(Route.Reports.value, "Báo cáo", Icons.Default.BarChart),
    FinluxNavTab(Route.Settings.value, "Hồ sơ", Icons.Default.Person),
)

/**
 * Standard FinLux Bottom Dock (FinLux Prism Spec 26, UI-FIX-05 & 24)
 */
@Composable
fun FinluxBottomDock(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<FinluxNavTab> = DefaultFinluxNavTabs,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(
        topStart = tokens.radius.bottomDock,
        topEnd = tokens.radius.bottomDock,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.14f),
            )
            .border(
                BorderStroke(1.dp, tokens.onSurface.copy(alpha = tokens.borderAlpha)),
                shape,
            ),
        shape = shape,
        color = tokens.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(74.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // First 2 tabs
            tabs.take(2).forEach { tab ->
                FinluxNavTabItem(
                    tab = tab,
                    isSelected = currentRoute == tab.route,
                    onClick = { onNavigate(tab.route) },
                )
            }

            // Center FAB (Quick Add)
            FinluxCenterFab(
                onClick = onQuickAdd,
            )

            // Last 2 tabs
            tabs.drop(2).forEach { tab ->
                FinluxNavTabItem(
                    tab = tab,
                    isSelected = currentRoute == tab.route,
                    onClick = { onNavigate(tab.route) },
                )
            }
        }
    }
}

@Composable
private fun FinluxNavTabItem(
    tab: FinluxNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) tokens.primary else tokens.onSurfaceVariant,
        label = "nav-tab-color",
    )

    val bgModifier = if (isSelected) {
        Modifier.background(
            color = tokens.primary.copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
            shape = RoundedCornerShape(16.dp),
        )
    } else Modifier

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(bgModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = tab.label,
            style = FinluxTextStyles.MicroLabel,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun FinluxCenterFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.65f),
        label = "fab-scale",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(54.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = tokens.primary.copy(alpha = 0.35f),
                spotColor = tokens.primary.copy(alpha = 0.45f),
            )
            .clip(CircleShape)
            .background(tokens.primaryBrush)
            .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Tạo nhanh",
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}
