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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinSession
import com.finlux.app.domain.model.Wallet

@Composable
fun SavingSpinResultContent(
    session: SavingSpinSession,
    destinations: List<SavingDestination>,
    selectedDestinationId: String?,
    wallets: List<Wallet>,
    sourceWalletId: String?,
    streakCount: Int,
    allowSkip: Boolean,
    isConfirming: Boolean,
    onSelectDestination: (String) -> Unit,
    onSelectSourceWallet: (String) -> Unit,
    onConfirm: () -> Unit,
    onOpenSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val amount = session.selectedAmount?.value ?: 0L

    val effectiveDestination = destinations.firstOrNull { it.id == selectedDestinationId }
        ?: destinations.firstOrNull { it.enabled }

    val effectiveMethod = effectiveDestination?.method ?: SavingMethod.CASH

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Badge chúc mừng kết quả
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.primary.copy(alpha = 0.12f),
            contentColor = tokens.primary,
        ) {
            Text(
                text = "🎉 Kết quả hôm nay!",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Mệnh giá trúng lớn
        Text(
            text = formatVndAmount(amount),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tokens.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Badge Streak
        if (streakCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
            ) {
                Text(
                    text = "🔥 Chuỗi $streakCount ngày liên tiếp",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Chọn nơi tiết kiệm (Saving Destination)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "NƠI TIẾT KIỆM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurfaceVariant,
                )
                Text(
                    text = if (effectiveMethod == SavingMethod.CASH) "Nuôi heo (Tiền mặt)" else "Chuyển khoản liên ví",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (destinations.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Mặc định: Heo đất (Tiền mặt)",
                        fontSize = 13.sp,
                        color = tokens.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(destinations.filter { it.enabled }) { dest ->
                        DestinationItemCard(
                            destination = dest,
                            isSelected = (dest.id == effectiveDestination?.id),
                            onClick = { onSelectDestination(dest.id) },
                            modifier = Modifier.width(130.dp),
                        )
                    }
                }
            }
        }

        // 4. Nếu là BANK_TRANSFER: Chọn ví nguồn (Source Wallet Picker)
        if (effectiveMethod == SavingMethod.BANK_TRANSFER) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TRÍCH TỪ VÍ NGUỒN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(6.dp))

                val availableWallets = wallets.filter { it.id != effectiveDestination?.linkedWalletId }
                var isExpanded by remember { mutableStateOf(false) }
                val currentSrcWallet = availableWallets.firstOrNull { it.id == sourceWalletId } ?: availableWallets.firstOrNull()

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = currentSrcWallet?.name ?: "Chọn ví thanh toán",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.onSurface,
                            )
                            currentSrcWallet?.let { w ->
                                Text(
                                    text = "Số dư: ${formatVndAmount(w.balance.value)}",
                                    fontSize = 11.5.sp,
                                    color = if (w.balance.value < amount) FinluxColors.ExpenseRed else tokens.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            fontSize = 12.sp,
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceSoft)
                            .padding(4.dp),
                    ) {
                        availableWallets.forEach { w ->
                            val isSrc = (w.id == currentSrcWallet?.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSrc) tokens.primary.copy(alpha = 0.1f) else tokens.surfaceSoft)
                                    .clickable {
                                        onSelectSourceWallet(w.id)
                                        isExpanded = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = w.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSrc) FontWeight.Bold else FontWeight.Normal,
                                    color = tokens.onSurface,
                                )
                                Text(
                                    text = formatVndAmount(w.balance.value),
                                    fontSize = 12.sp,
                                    color = tokens.onSurfaceVariant,
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
            enabled = !isConfirming,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("saving_spin_confirm"),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.primary,
                contentColor = tokens.onHero,
                disabledContainerColor = tokens.primary.copy(alpha = 0.5f),
                disabledContentColor = tokens.onHero.copy(alpha = 0.7f),
            ),
        ) {
            if (isConfirming) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = tokens.onHero)
            } else {
                Text("XÁC NHẬN ĐÃ NẠP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Nút phụ Nhắc tôi sau
        OutlinedButton(
            onClick = onOpenSnooze,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, tokens.border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.onSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
        ) {
            Text("Nhắc tôi sau", fontSize = 14.sp)
        }

        // 7. Bỏ qua hôm nay (text link)
        if (allowSkip) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bỏ qua hôm nay",
                fontSize = 13.sp,
                color = tokens.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(8.dp)
                    .testTag("saving_spin_skip"),
            )
        }
    }
}

@Composable
private fun DestinationItemCard(
    destination: SavingDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) tokens.primary.copy(alpha = 0.12f) else tokens.surfaceSoft,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) tokens.primary else tokens.border),
        modifier = modifier
            .height(108.dp)
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = tokens.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = tokens.onHero,
                        modifier = Modifier.padding(2.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (destination.method == SavingMethod.CASH) Icons.Filled.Savings else Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = destination.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = if (destination.method == SavingMethod.CASH) "Tiền mặt" else "Chuyển khoản",
                    fontSize = 10.sp,
                    color = tokens.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
