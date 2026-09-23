package com.finlux.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.GlassBottomNav
import com.finlux.app.core.designsystem.GlassFab
import com.finlux.app.core.designsystem.LocalAppUiStyle
import com.finlux.app.core.designsystem.component.FinluxBottomDock
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.AppUiStyle

/**
 * Universal Adaptive Bottom Navigation Bar (Phase 3 Consolidation).
 * - [AppUiStyle.PRISM]: Renders [FinluxBottomDock]
 * - [AppUiStyle.CLASSIC_LIQUID] / [AppUiStyle.MODERN_LUXURY]: Renders [FinluxAdaptiveLiquidBottomNav]
 */
@Composable
fun MainBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
) {
    when (LocalAppUiStyle.current) {
        AppUiStyle.PRISM -> {
            FinluxBottomDock(
                currentRoute = selectedRoute,
                onNavigate = onNavigate,
                onQuickAdd = onAdd,
            )
        }
        AppUiStyle.CLASSIC_LIQUID,
        AppUiStyle.MODERN_LUXURY -> {
            FinluxAdaptiveLiquidBottomNav(
                selectedRoute = selectedRoute,
                onNavigate = onNavigate,
                onAdd = onAdd,
            )
        }
    }
}

@Composable
fun FinluxAdaptiveLiquidBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    GlassBottomNav(modifier.fillMaxWidth()) {
        BottomDestinationItem(
            route = Route.Home,
            label = "Trang chủ",
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        )
        BottomDestinationItem(
            route = Route.Transactions,
            label = "Giao dịch",
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
            unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
        )
        Box(
            modifier = Modifier.weight(1.1f),
            contentAlignment = Alignment.Center,
        ) {
            GlassFab(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm giao dịch",
                    modifier = Modifier.size(28.dp),
                    tint = tokens.onHero,
                )
            }
        }
        BottomDestinationItem(
            route = Route.Reports,
            label = "Báo cáo",
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
        )
        BottomDestinationItem(
            route = Route.Settings,
            label = "Hồ sơ",
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        )
    }
}

@Composable
private fun RowScope.BottomDestinationItem(
    route: Route,
    label: String,
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
) {
    val tokens = LocalFinluxTokens.current
    val selected = selectedRoute == route.value
    val interactionSource = remember { MutableInteractionSource() }

    val selectedColor = tokens.primary
    val unselectedColor = tokens.onSurfaceVariant.copy(alpha = if (tokens.isDark) 0.70f else 0.85f)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_item_scale",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onNavigate(route.value) },
            )
            .padding(vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) selectedColor else unselectedColor,
            modifier = Modifier.size(22.dp),
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) selectedColor else unselectedColor,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (selected) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(tokens.primary),
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
