package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.Wallet

@Composable
fun SavingSpinCompletedContent(
    session: SavingSpinSession,
    destinations: List<SavingDestination>,
    wallets: List<Wallet>,
    sourceWalletId: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val destination = destinations.firstOrNull { it.id == session.destinationId }
    val sourceWallet = wallets.firstOrNull { it.id == sourceWalletId }
    val amountFormatted = formatVndAmount(session.selectedAmount?.value ?: 0L)

    val message = when (session.method) {
        SavingMethod.CASH -> "Đã ghi nhận $amountFormatted vào ${destination?.name ?: "Heo đất"}."
        SavingMethod.BANK_TRANSFER -> {
            val sourceName = sourceWallet?.name ?: "Ví nguồn"
            val destName = destination?.name ?: "Ví tiết kiệm"
            "Đã chuyển $amountFormatted từ $sourceName sang $destName."
        }
        null -> "Đã hoàn tất lượt tiết kiệm $amountFormatted thành công."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.primary.copy(alpha = 0.14f),
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = tokens.primary,
                modifier = Modifier.padding(12.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tiết kiệm thành công!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = tokens.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.primary,
                contentColor = tokens.onHero,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text("Đóng", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
