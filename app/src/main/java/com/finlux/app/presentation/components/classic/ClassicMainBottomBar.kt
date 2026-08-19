package com.finlux.app.presentation.components.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.FinluxTextSecondary
import com.finlux.app.core.designsystem.GlassBottomNav
import com.finlux.app.core.designsystem.GlassFab
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.VisualStyle

@Composable
fun ClassicMainBottomBar(selectedRoute: String, onNavigate: (String) -> Unit, onAdd: () -> Unit) {
    // BottomAppBar owns navigation-bar insets. Do not force a total height here:
    // on 3-button devices that would squeeze the 48dp system inset into the content area.
    GlassBottomNav(Modifier.fillMaxWidth()) {
        DestinationItem(Route.Home, "Trang chủ", selectedRoute, onNavigate, Icons.Filled.Home, Icons.Outlined.Home)
        DestinationItem(Route.Transactions, "Lịch sử", selectedRoute, onNavigate, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong)
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            GlassFab(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch") }
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
    val style = LocalUiPreferences.current.visualStyle
    val selected = selectedRoute == route.value
    val selectedColor = when (style) {
        VisualStyle.MODERN_DARK -> FinluxBlue
        VisualStyle.GLASSMORPHISM -> Color.White
        VisualStyle.DYNAMIC_GRADIENT -> FinluxBlue
    }
    val unselectedColor = when (style) {
        VisualStyle.MODERN_DARK -> Color(0xFF7890AA)
        VisualStyle.GLASSMORPHISM -> Color.White.copy(alpha = .58f)
        VisualStyle.DYNAMIC_GRADIENT -> FinluxTextSecondary
    }
    NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(route.value) },
        icon = {
            ThemedNavigationIcon(
                icon = if (selected) selectedIcon else unselectedIcon,
                selected = selected,
                style = style,
                tint = if (selected) selectedColor else unselectedColor,
                contentDescription = label,
            )
        },
        label = { Text(label, maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun ThemedNavigationIcon(
    icon: ImageVector,
    selected: Boolean,
    style: VisualStyle,
    tint: Color,
    contentDescription: String,
) {
    val shape = when (style) {
        VisualStyle.MODERN_DARK -> RoundedCornerShape(10.dp)
        VisualStyle.GLASSMORPHISM -> CircleShape
        VisualStyle.DYNAMIC_GRADIENT -> RoundedCornerShape(11.dp)
    }
    val fill = when (style) {
        VisualStyle.MODERN_DARK -> Brush.linearGradient(
            listOf(FinluxBlue.copy(alpha = if (selected) .22f else .04f), Color(0xFF08182B)),
        )
        VisualStyle.GLASSMORPHISM -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = if (selected) .28f else .06f), Color(0xFF8B5CFF).copy(alpha = if (selected) .30f else .04f)),
        )
        VisualStyle.DYNAMIC_GRADIENT -> if (selected) {
            Brush.linearGradient(listOf(Color(0xFF7C3CFF), Color(0xFF356DFF), Color(0xFF37C7F4)))
        } else {
            Brush.linearGradient(listOf(FinluxPurple.copy(alpha = .07f), Color.Transparent))
        }
    }
    val borderColor = when (style) {
        VisualStyle.MODERN_DARK -> FinluxBlue.copy(alpha = if (selected) .58f else .12f)
        VisualStyle.GLASSMORPHISM -> Color.White.copy(alpha = if (selected) .62f else .12f)
        VisualStyle.DYNAMIC_GRADIENT -> FinluxPurple.copy(alpha = if (selected) .35f else .10f)
    }
    Box(
        modifier = Modifier.size(34.dp)
            .then(if (selected && style == VisualStyle.GLASSMORPHISM) Modifier.shadow(8.dp, shape, ambientColor = Color.White) else Modifier)
            .clip(shape)
            .background(fill)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp), tint = if (selected && style == VisualStyle.DYNAMIC_GRADIENT) Color.White else tint)
    }
}
