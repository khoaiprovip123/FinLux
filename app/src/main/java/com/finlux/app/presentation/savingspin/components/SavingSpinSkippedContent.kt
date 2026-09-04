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
import androidx.compose.material.icons.filled.Info
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
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens

@Composable
fun SavingSpinSkippedContent(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.surfaceSoft,
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = tokens.onSurfaceVariant,
                modifier = Modifier.padding(14.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đã bỏ qua lượt này",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bạn có thể tiếp tục tham gia vào đợt tiết kiệm tiếp theo theo lịch đã cài đặt.",
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
                containerColor = tokens.surfaceSoft,
                contentColor = tokens.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text("Đóng", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
