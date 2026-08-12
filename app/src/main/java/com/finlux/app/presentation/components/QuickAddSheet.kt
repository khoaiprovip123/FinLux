package com.finlux.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.designsystem.ExpenseRed
import com.finlux.app.core.designsystem.FinluxBlue
import com.finlux.app.core.designsystem.FinluxPurple
import com.finlux.app.core.designsystem.GlassBottomSheet
import com.finlux.app.core.designsystem.IncomeGreen
import com.finlux.app.core.designsystem.WaterGlassCard

/** Shared action launcher used by the center navigation button on every main screen. */
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onIncome: () -> Unit,
    onExpense: () -> Unit,
    onTransfer: () -> Unit,
    onReceipt: () -> Unit,
) {
    GlassBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Tạo nhanh", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Chọn nghiệp vụ tài chính cần thực hiện", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Thêm thu", "Dòng tiền vào", Icons.Default.CallReceived, IncomeGreen, Modifier.weight(1f), onIncome)
                QuickAction("Thêm chi", "Dòng tiền ra", Icons.Default.CallMade, ExpenseRed, Modifier.weight(1f), onExpense)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Chuyển tiền", "Giữa các ví", Icons.Default.SwapHoriz, FinluxBlue, Modifier.weight(1f), onTransfer)
                QuickAction("Scan hóa đơn", "Nhập khoản chi", Icons.Default.DocumentScanner, FinluxPurple, Modifier.weight(1f), onReceipt)
            }
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    WaterGlassCard(
        modifier = modifier.height(106.dp),
        tint = accent,
        onClick = onClick,
        padding = PaddingValues(13.dp),
        cornerRadius = 20,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(34.dp).background(accent.copy(alpha = .16f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(19.dp), tint = accent)
            }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
