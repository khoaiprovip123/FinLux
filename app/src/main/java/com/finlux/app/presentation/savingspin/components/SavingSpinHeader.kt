package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

@Composable
fun SavingSpinHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Vòng quay tiết kiệm",
) {
    val tokens = LocalFinluxTokens.current

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("✨", fontSize = 16.sp)
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.onSurface,
            )
            Text("✨", fontSize = 16.sp)
        }

        Surface(
            shape = CircleShape,
            color = tokens.surfaceSoft,
            contentColor = tokens.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp)
                .clickable(onClick = onClose),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Đóng",
                tint = tokens.onSurfaceVariant,
                modifier = Modifier.padding(6.dp),
            )
        }
    }
}
