package com.finlux.app.presentation.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinanceCategoryIcons
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import java.time.Instant

@Composable
fun CategoriesScreen(onBack: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val actionState = viewModel.actionState.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    val tokens = LocalFinluxTokens.current
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(actionState.message) {
        actionState.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("Quản lý danh mục", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
                    actions = {
                        IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "Thêm danh mục") }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color.Transparent,
        ) { padding ->
            val filteredCategories = categories.filter { it.type == selectedType }

            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryType.entries.forEach { type ->
                            val isSelected = selectedType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                label = {
                                    Text(
                                        text = if (type == CategoryType.EXPENSE) "Danh mục Chi" else "Danh mục Thu",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tokens.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = tokens.primary,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = tokens.border,
                                    selectedBorderColor = tokens.primary,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                if (filteredCategories.isEmpty()) {
                    item {
                        FinluxEmptyState(
                            title = if (selectedType == CategoryType.EXPENSE) "Chưa có danh mục Chi tiêu" else "Chưa có danh mục Thu nhập",
                            description = "Tạo danh mục mới để phân loại thu chi một cách khoa học.",
                            icon = Icons.Default.Add,
                            actionLabel = "+ Thêm danh mục",
                            onActionClick = { editing = null; showEditor = true },
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                } else {
                    items(filteredCategories, key = { it.id }) { category ->
                        val accent = colorFromHex(category.colorHex, tokens.primary)
                        GlassCard(Modifier.fillMaxWidth(), onClick = { editing = category; showEditor = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(46.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(15.dp)),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(categoryIcon(category.icon), null, tint = accent) }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = tokens.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        if (category.isDefault) "Mặc định · đang được bảo vệ" else "Tùy chỉnh · có thể sửa/xóa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { editing = category; showEditor = true }) { Icon(Icons.Default.Edit, "Sửa", tint = tokens.onSurfaceVariant) }
                                if (!category.isDefault) {
                                    IconButton(onClick = { viewModel.delete(category) }) { Icon(Icons.Default.DeleteOutline, "Xóa", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        CategoryEditor(
            initial = editing,
            defaultType = selectedType,
            isSaving = actionState.isSaving,
            onDismiss = { showEditor = false },
            onSave = { viewModel.save(it) { showEditor = false } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditor(
    initial: Category?,
    defaultType: CategoryType,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: defaultType) }
    var iconKey by remember(initial) { mutableStateOf(initial?.icon ?: FinanceCategoryIcons.first().key) }
    var colorHex by remember(initial) { mutableStateOf(initial?.colorHex ?: FinanceAccentHexes.first()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (initial == null) "Thêm danh mục mới" else "Chỉnh sửa danh mục",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = tokens.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = tokens.onSurfaceVariant)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(32) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên danh mục") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.border,
                ),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryType.entries.forEach { option ->
                    val isSelected = type == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { type = option },
                        label = {
                            Text(
                                if (option == CategoryType.EXPENSE) "Chi tiêu" else "Thu nhập",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tokens.primary.copy(alpha = 0.15f),
                            selectedLabelColor = tokens.primary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = tokens.border,
                            selectedBorderColor = tokens.primary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text("Chọn biểu tượng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tokens.onSurface)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(FinanceCategoryIcons, key = { it.key }) { option ->
                    val selected = iconKey == option.key
                    val accent = colorFromHex(colorHex, tokens.primary)
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accent.copy(alpha = .18f) else tokens.surfaceSoft)
                            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent else tokens.border), RoundedCornerShape(14.dp))
                            .clickable { iconKey = option.key },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(option.icon, option.label, tint = if (selected) accent else tokens.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Text("Màu nhận diện", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tokens.onSurface)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(FinanceAccentHexes) { hex ->
                    val selected = hex == colorHex
                    Box(
                        Modifier
                            .size(if (selected) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(colorFromHex(hex, tokens.primary))
                            .border(BorderStroke(if (selected) 2.5.dp else 0.dp, tokens.onSurface), CircleShape)
                            .clickable { colorHex = hex },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        Category(
                            id = initial?.id.orEmpty(),
                            name = name.trim(),
                            type = type,
                            icon = iconKey,
                            colorHex = colorHex,
                            isDefault = initial?.isDefault ?: false,
                            createdAt = initial?.createdAt ?: Instant.now(),
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
            ) {
                Text(if (isSaving) "Đang lưu…" else "Lưu danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
