package com.finlux.app.presentation.savingspin.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.domain.model.SavingDestination
import com.finlux.app.domain.model.SavingMethod
import com.finlux.app.domain.model.SavingSpinSession

@Composable
fun SavingSpinResultContent(
    session: SavingSpinSession,
    destinations: List<SavingDestination>,
    selectedDestinationId: String?,
    allowSkip: Boolean,
    onSelectDestination: (String) -> Unit,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabledDestinations = destinations.filter { it.enabled }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Top row: 🎉 Hôm nay bạn tiết kiệm  và  🔥 Chuỗi 8 ngày
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
                        "Chuỗi 8 ngày",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB45309),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Số tiền lớn 35.000đ ở giữa với ánh sao lấp lánh xung quanh
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("✨", fontSize = 20.sp)
            Text(
                text = formatVndAmount(session.selectedAmount?.value ?: 0L),
                fontSize = 44.sp,
                color = Color(0xFF2563EB), // Xanh dương đậm chủ đạo
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("saving_spin_selected_amount"),
            )
            Text("✨", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Subtitle "Chọn nơi nạp tiền"
        Text(
            "Chọn nơi nạp tiền",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Ba thẻ chọn nơi nạp tiền dàn hàng ngang (Heo tiền mặt, MB Bank, Dự phòng)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (enabledDestinations.isEmpty()) {
                // Fallback demo cards nếu chưa có destinations
                val defaultMockDestinations = listOf(
                    Triple("piggy", "Heo tiền mặt", "🐷"),
                    Triple("bank", "Ví tiết kiệm\nMB Bank", "🏦"),
                    Triple("reserve", "Tài khoản\ndự phòng", "💳"),
                )
                defaultMockDestinations.forEach { (id, title, icon) ->
                    val isSelected = selectedDestinationId == id || (selectedDestinationId == null && id == "bank")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .clickable { onSelectDestination(id) },
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd),
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    title,
                                    fontSize = 11.5.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            } else {
                enabledDestinations.take(3).forEach { dest ->
                    val isSelected = dest.id == selectedDestinationId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .clickable { onSelectDestination(dest.id) },
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
                                    imageVector = if (dest.method == SavingMethod.CASH) Icons.Filled.Savings else Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = if (dest.method == SavingMethod.CASH) Color(0xFFF43F5E) else Color(0xFF2563EB),
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    dest.name,
                                    fontSize = 11.5.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
