package com.finlux.app.presentation.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.Wallet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterBottomSheet(
    currentPeriod: TimePeriodFilter,
    selectedWalletId: String?,
    selectedCategoryId: String?,
    wallets: List<Wallet>,
    categories: List<Category>,
    onApply: (TimePeriodFilter, String?, String?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tempPeriod by remember { mutableStateOf(currentPeriod) }
    var tempWalletId by remember { mutableStateOf(selectedWalletId) }
    var tempCategoryId by remember { mutableStateOf(selectedCategoryId) }

    val hasActiveFilters = tempPeriod != TimePeriodFilter.ALL || tempWalletId != null || tempCategoryId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // 1. Header Bar: Title + Reset Button + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Text(
                        text = "Bộ lọc nâng cao",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                tempPeriod = TimePeriodFilter.ALL
                                tempWalletId = null
                                tempCategoryId = null
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tokens.primary,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Đặt lại",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                ),
                                color = tokens.primary,
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(tokens.surfaceSoft, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = tokens.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // 2. Section: KỲ THỜI GIAN (BÁO CÁO)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "KỲ THỜI GIAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = Color(0xFF6B7280),
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TimePeriodFilter.entries.forEach { period ->
                        val isSelected = tempPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { tempPeriod = period },
                            label = {
                                Text(
                                    text = period.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6366F1).copy(alpha = 0.16f),
                                selectedLabelColor = Color(0xFF6366F1),
                                selectedLeadingIconColor = Color(0xFF6366F1),
                                containerColor = tokens.surfaceSoft,
                                labelColor = tokens.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF6366F1) else tokens.border,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }

            // 3. Section: VÍ THANH TOÁN
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "VÍ THANH TOÁN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = Color(0xFF6B7280),
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        val isAllSelected = tempWalletId == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { tempWalletId = null },
                            label = { Text("Tất cả ví") },
                            leadingIcon = if (isAllSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.16f),
                                selectedLabelColor = Color(0xFF3B82F6),
                                selectedLeadingIconColor = Color(0xFF3B82F6),
                                containerColor = tokens.surfaceSoft,
                                labelColor = tokens.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAllSelected,
                                borderColor = if (isAllSelected) Color(0xFF3B82F6) else tokens.border,
                                borderWidth = if (isAllSelected) 1.5.dp else 1.dp,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    items(wallets) { wallet ->
                        val isSelected = tempWalletId == wallet.id
                        val walletColor = colorFromHex(wallet.colorHex, Color(0xFF3B82F6))
                        FilterChip(
                            selected = isSelected,
                            onClick = { tempWalletId = if (isSelected) null else wallet.id },
                            label = { Text(wallet.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = walletIcon(wallet.type),
                                    contentDescription = null,
                                    tint = if (isSelected) walletColor else tokens.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = walletColor.copy(alpha = 0.16f),
                                selectedLabelColor = walletColor,
                                selectedLeadingIconColor = walletColor,
                                containerColor = tokens.surfaceSoft,
                                labelColor = tokens.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) walletColor else tokens.border,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }

            // 4. Section: DANH MỤC
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "DANH MỤC GIAO DỊCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = Color(0xFF6B7280),
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        val isAllSelected = tempCategoryId == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { tempCategoryId = null },
                            label = { Text("Tất cả danh mục") },
                            leadingIcon = if (isAllSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF43F5E).copy(alpha = 0.16f),
                                selectedLabelColor = Color(0xFFF43F5E),
                                selectedLeadingIconColor = Color(0xFFF43F5E),
                                containerColor = tokens.surfaceSoft,
                                labelColor = tokens.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAllSelected,
                                borderColor = if (isAllSelected) Color(0xFFF43F5E) else tokens.border,
                                borderWidth = if (isAllSelected) 1.5.dp else 1.dp,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    items(categories) { cat ->
                        val isSelected = tempCategoryId == cat.id
                        val catColor = colorFromHex(cat.colorHex, Color(0xFFF43F5E))
                        FilterChip(
                            selected = isSelected,
                            onClick = { tempCategoryId = if (isSelected) null else cat.id },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(cat.icon),
                                    contentDescription = null,
                                    tint = if (isSelected) catColor else tokens.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.16f),
                                selectedLabelColor = catColor,
                                selectedLeadingIconColor = catColor,
                                containerColor = tokens.surfaceSoft,
                                labelColor = tokens.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) catColor else tokens.border,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 5. Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        onReset()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.border),
                ) {
                    Text(
                        text = "Đặt lại",
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.onSurfaceVariant,
                    )
                }

                Button(
                    onClick = {
                        onApply(tempPeriod, tempWalletId, tempCategoryId)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                ) {
                    Text(
                        text = "Áp dụng bộ lọc",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
