package com.finlux.app.presentation.savingspin.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.SavingSpinStatus
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingSpinGameSheet(
    state: SavingSpinUiState,
    onAction: (SavingSpinAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isGameOpen) return
    val session = state.session ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onAction(SavingSpinAction.CloseGame) },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1)),
            )
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (session.status) {
                SavingSpinStatus.READY -> {
                    // Header có icon Sparkles và nút Đóng (X) tròn bên phải như mockup
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("✨", fontSize = 16.sp)
                            Text(
                                "Vòng quay tiết kiệm",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                            )
                            Text("✨", fontSize = 16.sp)
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .clickable { onAction(SavingSpinAction.CloseGame) },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Đóng",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.padding(6.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badge "1 lượt quay hôm nay"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                "1 lượt quay hôm nay",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vòng quay Wheel Canvas
                    SavingSpinWheel(
                        values = session.wheelValues,
                        selectedIndex = session.selectedIndex,
                        isSpinning = state.isSpinning,
                        modifier = Modifier.size(270.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nút QUAY NGAY lớn full-width bo tròn
                    Button(
                        onClick = { onAction(SavingSpinAction.Spin) },
                        enabled = !state.isSpinning,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB), // Xanh dương đậm theo mockup
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("saving_spin_button"),
                    ) {
                        if (state.isSpinning) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            Text("QUAY NGAY", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Thẻ info: Khoảng tiền và bước mệnh giá
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Khoảng tiền: ${formatVndAmount(state.config.minAmount.value)} - ${formatVndAmount(state.config.maxAmount.value)}  |  Bước ${formatVndAmount(state.config.step.amount)}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hai nút Nhắc tôi sau & Đóng ở dưới cùng
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onAction(SavingSpinAction.Snooze(Instant.now().plusSeconds(30L * 60L))) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nhắc tôi sau", fontSize = 13.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                            }

                            Box(modifier = Modifier.size(width = 1.dp, height = 18.dp).background(Color(0xFFE2E8F0)))

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onAction(SavingSpinAction.CloseGame) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đóng", fontSize = 13.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                SavingSpinStatus.SPUN_PENDING,
                SavingSpinStatus.SNOOZED -> {
                    if (state.isWheelAnimating) {
                        // Vẫn đang quay hoạt ảnh bánh xe: tiếp tục render bánh xe quay 3.2s
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Đang quay tìm mệnh giá...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SavingSpinWheel(
                                values = session.wheelValues,
                                selectedIndex = session.selectedIndex,
                                isSpinning = true,
                                onAnimationFinished = { onAction(SavingSpinAction.WheelAnimationFinished) },
                                modifier = Modifier.size(270.dp),
                            )
                        }
                    } else {
                        SavingSpinResultContent(
                            session = session,
                            wallets = state.wallets,
                            sourceWalletId = state.sourceWalletId,
                            selectedWalletId = state.selectedWalletId,
                            streakCount = state.streakCount,
                            allowSkip = state.config.allowSkip,
                            onSelectSourceWallet = { onAction(SavingSpinAction.SelectSourceWallet(it)) },
                            onSelectWallet = { onAction(SavingSpinAction.SelectWallet(it)) },
                            onConfirm = { onAction(SavingSpinAction.ConfirmDeposit) },
                            onSnooze = { onAction(SavingSpinAction.Snooze(Instant.now().plusSeconds(30L * 60L))) },
                            onSkip = { onAction(SavingSpinAction.Skip) },
                        )
                    }
                }
                SavingSpinStatus.COMPLETED -> {
                    Text("🎉 Đã hoàn thành!", style = FinluxTextStyles.SectionTitle, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        formatVndAmount(session.selectedAmount?.value ?: 0L),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Khoản này đã được ghi nhận vào báo cáo & số dư ví.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onAction(SavingSpinAction.CloseGame) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text("Xong", fontWeight = FontWeight.Bold)
                    }
                }
                SavingSpinStatus.SKIPPED -> {
                    Text("Đã bỏ qua hôm nay", style = FinluxTextStyles.SectionTitle, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onAction(SavingSpinAction.CloseGame) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text("Đóng")
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
