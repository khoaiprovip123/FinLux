package com.finlux.app.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinanceAccentHexes
import com.finlux.app.core.designsystem.FinanceCategoryIcons
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.GlassDialogSurface
import com.finlux.app.core.designsystem.GlassTopBar
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import java.time.Instant

@Composable
fun CategoriesScreen(onBack: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories = viewModel.categories.collectAsStateWithLifecycle().value
    val actionState = viewModel.actionState.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    LaunchedEffect(actionState.message) {
        actionState.message?.let { snackbar.showSnackbar(it); viewModel.consumeMessage() }
    }
    Box(Modifier.fillMaxSize()) {
    FinluxStyleBackdrop(Modifier.fillMaxSize())
    Scaffold(
        topBar = {
            GlassTopBar(
                title = { Text("Quản lý danh mục") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") } },
                actions = {
                    IconButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "Thêm danh mục") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(if (type == CategoryType.EXPENSE) "Danh mục Chi" else "Danh mục Thu") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            items(categories.filter { it.type == selectedType }, key = { it.id }) { category ->
                val accent = colorFromHex(category.colorHex)
                GlassCard(Modifier.fillMaxWidth(), onClick = { editing = category; showEditor = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(15.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(categoryIcon(category.icon), null, tint = accent) }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(category.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (category.isDefault) "Mặc định · đang được bảo vệ" else "Tùy chỉnh · có thể sửa/xóa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { editing = category; showEditor = true }) { Icon(Icons.Default.Edit, "Sửa") }
                        if (!category.isDefault) {
                            IconButton(onClick = { viewModel.delete(category) }) { Icon(Icons.Default.DeleteOutline, "Xóa") }
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

@Composable
private fun CategoryEditor(
    initial: Category?,
    defaultType: CategoryType,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: defaultType) }
    var iconKey by remember(initial) { mutableStateOf(initial?.icon ?: FinanceCategoryIcons.first().key) }
    var colorHex by remember(initial) { mutableStateOf(initial?.colorHex ?: FinanceAccentHexes.first()) }
    Dialog(onDismissRequest = onDismiss) {
        GlassDialogSurface {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (initial == null) "Thêm danh mục" else "Sửa danh mục", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it.take(32) }, Modifier.fillMaxWidth(), label = { Text("Tên danh mục") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryType.entries.forEach { option ->
                        FilterChip(type == option, { type = option }, { Text(if (option == CategoryType.EXPENSE) "Chi" else "Thu") })
                    }
                }
                Text("Chọn biểu tượng", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(FinanceCategoryIcons, key = { it.key }) { option ->
                        val selected = iconKey == option.key
                        Box(
                            Modifier.size(44.dp)
                                .background(if (selected) colorFromHex(colorHex).copy(alpha = .18f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(13.dp))
                                .clickable { iconKey = option.key },
                            contentAlignment = Alignment.Center,
                        ) { Icon(option.icon, option.label, tint = if (selected) colorFromHex(colorHex) else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                Text("Màu nhận diện", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(FinanceAccentHexes) { hex ->
                        Box(
                            Modifier.size(if (hex == colorHex) 34.dp else 30.dp)
                                .background(colorFromHex(hex), CircleShape)
                                .clickable { colorHex = hex },
                        )
                    }
                }
                Button(
                    onClick = {
                        onSave(
                            Category(
                                id = initial?.id.orEmpty(), name = name.trim(), type = type, icon = iconKey,
                                colorHex = colorHex, isDefault = initial?.isDefault ?: false,
                                createdAt = initial?.createdAt ?: Instant.now(),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && !isSaving,
                ) { Text(if (isSaving) "Đang lưu…" else "Lưu danh mục") }
            }
        }
    }
}
