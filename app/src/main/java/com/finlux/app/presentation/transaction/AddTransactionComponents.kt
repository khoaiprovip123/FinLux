@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.finlux.app.presentation.transaction

import com.finlux.app.core.designsystem.theme.FinluxPalette
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.finlux.app.core.designsystem.component.ErgonomicCompactAmountCard
import com.finlux.app.core.designsystem.component.ErgonomicFormRow
import com.finlux.app.core.designsystem.component.ErgonomicInputRow
import com.finlux.app.core.designsystem.component.FinluxCategoryPickerBottomSheet
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.FinluxWalletPickerBottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.finlux.app.domain.model.DealFlowType
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinanceCategoryIcons
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxDialog
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.designsystem.walletIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.WalletType
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun TransactionTypePill(
    label: String,
    isSelected: Boolean,
    activeBg: Color,
    activeText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeBg else if (tokens.isDark) FinluxPalette.CFF1E1E2D else FinluxPalette.CFFF3F4F6,
        border = if (isSelected) BorderStroke(1.dp, activeText.copy(alpha = 0.3f)) else null,
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isSelected) activeText else FinluxPalette.CFF6B7280,
                textAlign = TextAlign.Center,
            )
        }
    }
}







/**
 * Category Editor Dialog for adding/editing a category
 */
@Composable
internal fun CategoryEditorDialog(
    category: Category? = null,
    initialType: CategoryType = CategoryType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (String, String, String, CategoryType) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val tokens = LocalFinluxTokens.current
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: FinanceCategoryIcons.first().key) }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: FinanceAccentHexes.first()) }
    var selectedType by remember { mutableStateOf(category?.type ?: initialType) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (category == null) "Tạo danh mục mới" else "Chỉnh sửa danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Loại danh mục", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionTypePill(
                        label = "Chi tiêu",
                        isSelected = selectedType == CategoryType.EXPENSE,
                        activeBg = FinluxPalette.CFFFFE4E6,
                        activeText = FinluxPalette.CFFE11D48,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = CategoryType.EXPENSE },
                    )
                    TransactionTypePill(
                        label = "Thu nhập",
                        isSelected = selectedType == CategoryType.INCOME,
                        activeBg = FinluxPalette.CFFDCFCE7,
                        activeText = FinluxPalette.CFF16A34A,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = CategoryType.INCOME },
                    )
                }

                Text("Chọn biểu tượng", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FinanceCategoryIcons) { iconOption ->
                        val isSelected = selectedIcon == iconOption.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) tokens.primary.copy(alpha = 0.15f) else tokens.surfaceSoft,
                            border = if (isSelected) BorderStroke(1.5.dp, tokens.primary) else null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedIcon = iconOption.key },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(iconOption.icon, null, tint = if (isSelected) tokens.primary else tokens.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Text("Chọn màu sắc", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FinanceAccentHexes) { colorHex: String ->
                        val isSelected = selectedColor == colorHex
                        val color = colorFromHex(colorHex)
                        Surface(
                            shape = CircleShape,
                            color = color,
                            border = if (isSelected) BorderStroke(2.5.dp, tokens.onSurface) else null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { selectedColor = colorHex },
                        ) {}
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text("Xóa", color = FinluxPalette.CFFEF4444)
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), selectedIcon, selectedColor, selectedType)
                            }
                        },
                        enabled = name.isNotBlank(),
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
