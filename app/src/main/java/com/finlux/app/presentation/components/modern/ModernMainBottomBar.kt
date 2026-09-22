package com.finlux.app.presentation.components.modern

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.modern.GlassBottomNav
import com.finlux.app.core.designsystem.modern.GlassFab
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.VisualStyle

@Composable
fun ModernMainBottomBar(selectedRoute: String, onNavigate: (String) -> Unit, onAdd: () -> Unit) {
    val tokens = LocalFinluxTokens.current
    GlassBottomNav(Modifier.fillMaxWidth()) {
        DestinationItem(Route.Home, "Trang chủ", selectedRoute, onNavigate, Icons.Filled.Home, Icons.Outlined.Home)
        DestinationItem(Route.Transactions, "Giao dịch", selectedRoute, onNavigate, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong)
        Box(
            modifier = Modifier.weight(1.1f),
            contentAlignment = Alignment.Center,
        ) {
            GlassFab(onClick = onAdd) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm giao dịch",
                    modifier = Modifier.size(28.dp),
                    tint = tokens.onHero,
                )
            }
        }
        DestinationItem(
            Route.Reports,
            "Báo cáo",
            selectedRoute,
            onNavigate,
            Icons.Filled.BarChart,
            Icons.Outlined.BarChart,
        )
        DestinationItem(Route.Settings, "Hồ sơ", selectedRoute, onNavigate, Icons.Filled.Person, Icons.Outlined.Person)
    }
}

@Composable
private fun RowScope.DestinationItem(
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
    val unselectedColor = tokens.onSurfaceVariant.copy(alpha = if (tokens.isDark) 0.7f else 0.85f)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.65f),
        label = "nav-tab-scale",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onNavigate(route.value) },
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ThemedNavigationIcon(
            icon = if (selected) selectedIcon else unselectedIcon,
            selected = selected,
            selectedColor = selectedColor,
            unselectedColor = unselectedColor,
            contentDescription = label,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedColor else unselectedColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun ThemedNavigationIcon(
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    contentDescription: String,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.4f
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 26.dp)
            .then(
                if (selected) {
                    Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            if (dark) selectedColor.copy(alpha = 0.18f) else selectedColor.copy(alpha = 0.12f),
                        )
                        .border(
                            1.dp,
                            if (dark) selectedColor.copy(alpha = 0.35f) else selectedColor.copy(alpha = 0.20f),
                            RoundedCornerShape(13.dp),
                        )
                } else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) selectedColor else unselectedColor,
            modifier = Modifier.size(20.dp),
        )
    }
}
