package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType

@Composable
fun SavingSpinResultContent(
    session: SavingSpinSession,
    wallets: List<Wallet>,
    sourceWalletId: String?,
    selectedWalletId: String?,
    streakCount: Int,
    allowSkip: Boolean,
    onSelectSourceWallet: (String) -> Unit,
    onSelectWallet: (String) -> Unit,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeWallets = wallets.filter { it.status == "active" }
    val sourceWallet = activeWallets.firstOrNull { it.id == sourceWalletId }
    val destWallet = activeWallets.firstOrNull { it.id == selectedWalletId }
    var showSourcePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Top row: 🎉 Hôm nay bạn tiết kiệm  và  🔥 Chuỗi X lần nạp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🎉", fontSize = 16.sp)
                Text(
                    "Hôm nay bạn tiết kiệm",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("🔥", fontSize = 12.sp)
                    Text(
                        "Chuỗi $streakCount lần nạp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB45309),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Số tiền lớn ở giữa với ánh sao lấp lánh xung quanh
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("✨", fontSize = 20.sp)
            Text(
                text = formatVndAmount(session.selectedAmount?.value ?: 0L),
                fontSize = 42.sp,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("saving_spin_selected_amount"),
            )
            Text("✨", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Khối Nguồn tiền chuyển (Transfer Source)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSourcePicker = !showSourcePicker },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Trích từ nguồn:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text(
                        sourceWallet?.name ?: "Ví tiền mặt",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                    )
                    sourceWallet?.let {
                        Text(
                            "(${formatVndAmount(it.balance.value, isCompact = true)})",
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Text("Đổi ví ▾", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
            }
        }

        if (showSourcePicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Chọn ví nguồn trích tiền:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    activeWallets.forEach { w ->
                        val isSrc = w.id == sourceWalletId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSrc) Color(0xFFEFF6FF) else Color.Transparent)
                                .clickable {
                                    onSelectSourceWallet(w.id)
                                    showSourcePicker = false
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(w.name, fontSize = 13.sp, fontWeight = if (isSrc) FontWeight.Bold else FontWeight.Normal, color = Color(0xFF1E293B))
                            Text(formatVndAmount(w.balance.value), fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Subtitle "Cất vào ví tiết kiệm / Heo đất"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("➔ Cất vào ví tiết kiệm:", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Thẻ chọn ví từ hệ thống danh mục ví đã thiết lập
        val activeWallets = wallets.filter { it.status == "active" }
        if (activeWallets.size <= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                activeWallets.forEach { wallet ->
                    val isSelected = wallet.id == selectedWalletId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clickable { onSelectWallet(wallet.id) },
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = when (wallet.type) {
                                        WalletType.CASH -> Icons.Filled.Savings
                                        WalletType.BANK -> Icons.Filled.AccountBalance
                                        else -> Icons.Filled.CreditCard
                                    },
                                    contentDescription = null,
                                    tint = when (wallet.type) {
                                        WalletType.CASH -> Color(0xFFF43F5E)
                                        WalletType.BANK -> Color(0xFF2563EB)
                                        else -> Color(0xFF8B5CF6)
                                    },
                                    modifier = Modifier.size(26.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    wallet.name,
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 2,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    formatVndAmount(wallet.balance.value, isCompact = true),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(activeWallets.size) { index ->
                    val wallet = activeWallets[index]
                    val isSelected = wallet.id == selectedWalletId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .width(106.dp)
                            .height(112.dp)
                            .clickable { onSelectWallet(wallet.id) },
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = when (wallet.type) {
                                        WalletType.CASH -> Icons.Filled.Savings
                                        WalletType.BANK -> Icons.Filled.AccountBalance
                                        else -> Icons.Filled.CreditCard
                                    },
                                    contentDescription = null,
                                    tint = when (wallet.type) {
                                        WalletType.CASH -> Color(0xFFF43F5E)
                                        WalletType.BANK -> Color(0xFF2563EB)
                                        else -> Color(0xFF8B5CF6)
                                    },
                                    modifier = Modifier.size(26.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    wallet.name,
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 2,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    formatVndAmount(wallet.balance.value, isCompact = true),
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Nút chính XÁC NHẬN ĐÃ NẠP
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("saving_spin_confirm"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
            ),
        ) {
            Text("XÁC NHẬN ĐÃ NẠP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Nút phụ Nhắc tôi sau
        OutlinedButton(
            onClick = onSnooze,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().height(46.dp),
        ) {
            Text("Nhắc tôi sau", fontSize = 14.sp, color = Color(0xFF475569))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7. Bỏ qua hôm nay (text link)
        if (allowSkip) {
            Text(
                "Bỏ qua hôm nay",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(8.dp)
                    .testTag("saving_spin_skip"),
            )
        }
    }
}
