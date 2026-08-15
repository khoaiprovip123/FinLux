package com.finlux.app.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finlux.app.R
import com.finlux.app.domain.model.VisualStyle

/**
 * Master FinLux brand mark. Keeping it in the design system guarantees that Splash, Auth and
 * About render the same logo proportions, safe area and theme-aware frame.
 */
@Composable
fun FinluxBrandMark(
    size: Dp,
    modifier: Modifier = Modifier,
    framed: Boolean = true,
) {
    val style = LocalUiPreferences.current.visualStyle
    val image: @Composable () -> Unit = {
        Image(
            painter = painterResource(R.drawable.finlux_logo),
            contentDescription = "Logo FinLux",
            modifier = Modifier.size(size).padding(size * .04f),
            contentScale = ContentScale.Fit,
        )
    }
    if (!framed) {
        Box(modifier.size(size), contentAlignment = Alignment.Center) { image() }
        return
    }
    val frameColor = when (style) {
        VisualStyle.MODERN_DARK -> Color(0xD9091C33)
        VisualStyle.GLASSMORPHISM -> Color.White.copy(alpha = .16f)
        VisualStyle.DYNAMIC_GRADIENT -> Color.White.copy(alpha = .94f)
    }
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * .28f),
        color = frameColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .22f)),
        shadowElevation = 10.dp,
    ) {
        Box(contentAlignment = Alignment.Center) { image() }
    }
}
