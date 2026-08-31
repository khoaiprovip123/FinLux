package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod

@Composable
fun SavingDestinationCard(
    destination: SavingDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        modifier = modifier.clickable(role = Role.RadioButton, enabled = destination.enabled, onClick = onClick),
        shape = RoundedCornerShape(tokens.radius.standardCard),
        color = if (selected) tokens.primary.copy(alpha = .12f) else tokens.surfaceSoft,
        contentColor = tokens.onSurface,
        border = BorderStroke(1.dp, if (selected) tokens.primary else tokens.border),
    ) {
        Row(
            modifier = Modifier.padding(tokens.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = tokens.primary.copy(alpha = .12f), modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = if (destination.method == SavingMethod.CASH) Icons.Filled.Savings else Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(destination.name, style = FinluxTextStyles.Body, color = tokens.onSurface)
                Text(
                    if (destination.method == SavingMethod.CASH) "Tiền mặt" else "Chuyển khoản ngân hàng",
                    style = FinluxTextStyles.Caption,
                    color = tokens.onSurfaceVariant,
                )
            }
            RadioButton(selected = selected, onClick = null, enabled = destination.enabled)
        }
    }
}
