package com.finlux.app.presentation.settings.prism

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.BuildConfig
import com.finlux.app.core.designsystem.FinluxBrandMark
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.component.FinluxScreenHeader
import com.finlux.app.core.designsystem.component.FinluxSectionHeader
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.settings.SettingsViewModel
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun PrismSettingsScreen(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    selectedUiStyle: AppUiStyle = AppUiStyle.PRISM,
    onUiStyleSelected: (AppUiStyle) -> Unit = {},
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit,
    onSignedOut: () -> Unit,
    onCheckUpdate: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val user = viewModel.user.collectAsStateWithLifecycle().value
    val wallets = viewModel.wallets.collectAsStateWithLifecycle().value
    val totalAssets = wallets.sumOf { it.balance.value }
    val avatarState = viewModel.avatarState.collectAsStateWithLifecycle().value
    val nameState = viewModel.nameState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    var showAvatarSource by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showNameEditor by remember { mutableStateOf(false) }
    var nameDraft by remember(user?.uid) { mutableStateOf(user?.displayName.orEmpty()) }

    fun openNameEditor() {
        nameDraft = user?.displayName.orEmpty()
        viewModel.clearNameMessage()
        showNameEditor = true
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::updateAvatar)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) pendingCameraUri?.let(viewModel::updateAvatar)
    }
    fun openCamera() {
        createCameraUri(context).also {
            pendingCameraUri = it
            cameraLauncher.launch(it)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera()
    }

    if (showAvatarSource) {
        AlertDialog(
            onDismissRequest = { showAvatarSource = false },
            title = { Text("Đổi ảnh đại diện", fontWeight = FontWeight.Bold, style = FinluxTextStyles.SectionTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrismAvatarSourceButton(Icons.Default.PhotoLibrary, "Chọn từ thư viện") {
                        showAvatarSource = false
                        galleryLauncher.launch("image/*")
                    }
                    PrismAvatarSourceButton(Icons.Default.CameraAlt, "Chụp ảnh mới") {
                        showAvatarSource = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAvatarSource = false }) { Text("Hủy") } },
            shape = RoundedCornerShape(tokens.radius.dialog),
            containerColor = tokens.surfaceSoft,
        )
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { if (!nameState.isLoading) showNameEditor = false },
            title = { Text("Đổi tên hiển thị", fontWeight = FontWeight.Bold, style = FinluxTextStyles.SectionTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        singleLine = true,
                        label = { Text("Tên người dùng") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.radius.input),
                    )
                    nameState.message?.let { message ->
                        Text(
                            message,
                            color = if (nameState.isError) FinluxColors.ExpenseRed else tokens.primary,
                            style = FinluxTextStyles.Caption,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.updateDisplayName(nameDraft) },
                    enabled = nameDraft.isNotBlank() && !nameState.isLoading,
                    shape = RoundedCornerShape(tokens.radius.input),
                ) {
                    if (nameState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Lưu tên", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showNameEditor = false }, enabled = !nameState.isLoading) { Text("Hủy") } },
            shape = RoundedCornerShape(tokens.radius.dialog),
            containerColor = tokens.surfaceSoft,
        )
    }

    LaunchedEffect(nameState.message, nameState.isError) {
        if (nameState.message != null && !nameState.isError) {
            delay(650)
            showNameEditor = false
        }
    }

    Scaffold(
        topBar = {
            FinluxScreenHeader(
                title = "Hồ sơ & Cài đặt",
                subtitle = "Tài khoản & Cá nhân hóa",
                onBack = { onNavigate("home") },
            )
        },
        bottomBar = {
            MainBottomBar(
                selectedRoute = "settings",
                onNavigate = onNavigate,
                onAdd = onAdd,
            )
        },
        containerColor = tokens.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Profile Hero Card
            item {
                PrismProfileHero(
                    name = user?.displayName ?: "Người dùng",
                    email = user?.email.orEmpty(),
                    photoUrl = user?.photoUrl,
                    loading = avatarState.isLoading,
                    totalAssets = totalAssets,
                    onAvatar = { showAvatarSource = true },
                    onEditName = ::openNameEditor,
                )
            }

            // Status message feedback
            avatarState.message?.let { message ->
                item {
                    Text(
                        message,
                        color = if (avatarState.isError) FinluxColors.ExpenseRed else tokens.primary,
                        style = FinluxTextStyles.Caption,
                    )
                    LaunchedEffect(message) { delay(2_500); viewModel.clearAvatarMessage() }
                }
            }
            nameState.message?.takeIf { !showNameEditor }?.let { message ->
                item {
                    Text(
                        message,
                        color = if (nameState.isError) FinluxColors.ExpenseRed else tokens.primary,
                        style = FinluxTextStyles.Caption,
                    )
                    LaunchedEffect(message) { delay(2_500); viewModel.clearNameMessage() }
                }
            }

            // Quick Feature Grid (Bento style)
            item {
                FinluxSectionHeader(title = "Quản lý nhanh")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PrismQuickTile(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Ví của tôi",
                        subtitle = "${wallets.size} ví",
                        color = tokens.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("wallets") },
                    )
                    PrismQuickTile(
                        icon = Icons.Default.Savings,
                        title = "Ngân sách",
                        subtitle = "Theo dõi",
                        color = FinluxColors.PrimaryViolet,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("budget") },
                    )
                    PrismQuickTile(
                        icon = Icons.Default.Category,
                        title = "Danh mục",
                        subtitle = "Tùy chỉnh",
                        color = FinluxColors.IncomeGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("categories") },
                    )
                    PrismQuickTile(
                        icon = Icons.Default.Alarm,
                        title = "Nhắc nhở",
                        subtitle = "Định kỳ",
                        color = FinluxColors.WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("reminders") },
                    )
                }
            }

            // UI Style / Theme Selector (Bento Hero Selector)
            item {
                FinluxSectionHeader(title = "Phong cách giao diện")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrismUiStyleOptionCard(
                        title = "FinLux Prism",
                        badge = "Khuyên dùng 2026",
                        badgeGradient = listOf(Color(0xFF3A7BFF), Color(0xFF6F52F5), Color(0xFF23C7E8)),
                        description = "Data-first, bố cục Bento Box, Soft Surface mượt mà, tối giản hiệu ứng kính để tập trung dữ liệu.",
                        icon = "💎",
                        tags = listOf("📊 Data-First", "🍱 Bento Layout", "✨ Soft Surface", "⚡ 120 FPS"),
                        isSelected = selectedUiStyle == AppUiStyle.PRISM,
                        onClick = { onUiStyleSelected(AppUiStyle.PRISM) },
                    )

                    PrismUiStyleOptionCard(
                        title = "Modern Luxury",
                        badge = "NextGen Glass",
                        badgeGradient = listOf(Color(0xFF3478F6), Color(0xFF7758F6)),
                        description = "Kính lỏng 3D đa tầng, bo tròn sang trọng, chuyển động sống động và công nghệ hiện đại.",
                        icon = "✨",
                        tags = listOf("💎 Kính lỏng 3D", "🌌 Hiệu ứng Aurora", "🎨 Glow"),
                        isSelected = selectedUiStyle == AppUiStyle.MODERN_LUXURY,
                        onClick = { onUiStyleSelected(AppUiStyle.MODERN_LUXURY) },
                    )

                    PrismUiStyleOptionCard(
                        title = "Liquid Glass Classic",
                        badge = "Classic v1.5",
                        badgeGradient = listOf(Color(0xFF0284C7), Color(0xFF0D9488)),
                        description = "Thiết kế thanh lịch, độ tương phản cao, tối ưu trực quan và siêu nhẹ.",
                        icon = "💧",
                        tags = listOf("🎯 Trực quan", "📊 Tương phản cao", "⚡ Tối giản"),
                        isSelected = selectedUiStyle == AppUiStyle.CLASSIC_LIQUID,
                        onClick = { onUiStyleSelected(AppUiStyle.CLASSIC_LIQUID) },
                    )
                }
            }

            // Theme Preference (Light / Dark / System)
            item {
                FinluxSectionHeader(title = "Chế độ hiển thị")
                Spacer(Modifier.height(8.dp))
                FinluxSoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeOptionChip(
                            icon = Icons.Default.LightMode,
                            label = "Sáng",
                            selected = selectedTheme == ThemePreference.LIGHT,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeSelected(ThemePreference.LIGHT) },
                        )
                        ThemeOptionChip(
                            icon = Icons.Default.DarkMode,
                            label = "Tối",
                            selected = selectedTheme == ThemePreference.DARK,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeSelected(ThemePreference.DARK) },
                        )
                        ThemeOptionChip(
                            icon = Icons.Default.SettingsBrightness,
                            label = "Hệ thống",
                            selected = selectedTheme == ThemePreference.SYSTEM,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeSelected(ThemePreference.SYSTEM) },
                        )
                    }
                }
            }

            // Security & Settings Rows
            item {
                FinluxSectionHeader(title = "Bảo mật & Cài đặt")
                Spacer(Modifier.height(8.dp))
                FinluxSoftCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        PrismSettingsRow(
                            icon = Icons.Default.Edit,
                            label = "Thông tin tài khoản",
                            onClick = { openNameEditor() },
                        )
                        PrismSettingsRow(
                            icon = Icons.Default.NotificationsNone,
                            label = "Cài đặt thông báo",
                            onClick = { onNavigate("notifications") },
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(tokens.radius.smallChip),
                                    color = tokens.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = tokens.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "Khóa ứng dụng Sinh trắc học",
                                        style = FinluxTextStyles.CardTitle,
                                        color = tokens.onSurface,
                                    )
                                    Text(
                                        "Vân tay / Khuôn mặt khi mở app",
                                        style = FinluxTextStyles.Caption,
                                        color = tokens.onSurfaceVariant,
                                    )
                                }
                            }
                            Switch(
                                checked = uiPreferences.biometricEnabled,
                                onCheckedChange = { onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tokens.primary,
                                    uncheckedTrackColor = tokens.surfaceSoft,
                                ),
                            )
                        }
                    }
                }
            }

            // About FinLux
            item {
                FinluxSectionHeader(title = "Về FinLux")
                Spacer(Modifier.height(8.dp))
                FinluxSoftCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FinluxBrandMark(size = 56.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "FinLux Finance",
                                    style = FinluxTextStyles.SectionTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.onSurface,
                                )
                                Text(
                                    "Phiên bản ${BuildConfig.VERSION_NAME}",
                                    style = FinluxTextStyles.Caption,
                                    color = tokens.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "Tài chính rõ ràng, cuộc sống nhẹ nhàng",
                                    style = FinluxTextStyles.MicroLabel,
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            "FinLux giúp bạn quản lý thu chi, ví, ngân sách, mục tiêu và báo cáo trong một trải nghiệm Bento Data-First thống nhất.",
                            style = FinluxTextStyles.Body,
                            color = tokens.onSurfaceVariant,
                        )

                        OutlinedButton(
                            onClick = onCheckUpdate,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.radius.input),
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = tokens.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Kiểm tra bản cập nhật mới", fontWeight = FontWeight.SemiBold, color = tokens.primary)
                        }
                    }
                }
            }

            // Sign out button
            item {
                OutlinedButton(
                    onClick = { viewModel.signOut(onSignedOut) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
                    border = BorderStroke(1.dp, FinluxColors.ExpenseRed.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = FinluxColors.ExpenseRed,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất tài khoản", color = FinluxColors.ExpenseRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrismProfileHero(
    name: String,
    email: String,
    photoUrl: String?,
    loading: Boolean,
    totalAssets: Long,
    onAvatar: () -> Unit,
    onEditName: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.radius.heroCard),
        color = Color.Transparent,
        shadowElevation = tokens.elevation,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            tokens.primary,
                            FinluxColors.PrimaryViolet,
                            Color(0xFF0EA5E9),
                        )
                    )
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.clickable(onClick = onAvatar)) {
                        FinluxUserAvatar(
                            photoUrl = photoUrl,
                            displayName = name,
                            size = 64.dp,
                            editable = false,
                            onClick = onAvatar,
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Đổi ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = name,
                                style = FinluxTextStyles.SectionTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                shape = RoundedCornerShape(tokens.radius.smallChip),
                                color = Color.White.copy(alpha = 0.22f),
                            ) {
                                Text(
                                    "Premium",
                                    style = FinluxTextStyles.MicroLabel,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = email.ifBlank { "Tài khoản FinLux" },
                            style = FinluxTextStyles.Caption,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Chạm để chỉnh sửa thông tin",
                            style = FinluxTextStyles.MicroLabel,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }

                    IconButton(onClick = onEditName) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa tên",
                            tint = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }

                // Total assets sub-card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.radius.input),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "Tổng tài sản",
                                style = FinluxTextStyles.MicroLabel,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                            Text(
                                formatVndAmount(totalAssets),
                                style = FinluxTextStyles.DisplayAmount.copy(fontSize = 20.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Quản lý tập trung & an toàn",
                                style = FinluxTextStyles.MicroLabel,
                                color = Color.White.copy(alpha = 0.65f),
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrismQuickTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.radius.input))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(tokens.radius.input),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.surfaceSoft),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(tokens.radius.smallChip),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = title,
                style = FinluxTextStyles.Caption,
                color = tokens.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = FinluxTextStyles.MicroLabel,
                color = tokens.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrismUiStyleOptionCard(
    title: String,
    badge: String,
    badgeGradient: List<Color>,
    description: String,
    icon: String,
    tags: List<String>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.radius.standardCard))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(tokens.radius.standardCard),
        color = if (isSelected) tokens.primary.copy(alpha = 0.08f) else tokens.surfaceSoft,
        border = if (isSelected) BorderStroke(1.8.dp, tokens.primary) else BorderStroke(1.dp, tokens.surfaceSoft),
        shadowElevation = if (isSelected) tokens.elevation else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) tokens.primary.copy(alpha = 0.18f) else tokens.surfaceSoft,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(icon, fontSize = 20.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = title,
                            style = FinluxTextStyles.CardTitle,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.radius.smallChip))
                                .background(Brush.horizontalGradient(badgeGradient))
                                .padding(horizontal = 8.dp, vertical = 2.5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = badge,
                                style = FinluxTextStyles.MicroLabel,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = FinluxTextStyles.Caption,
                        color = tokens.onSurfaceVariant,
                    )
                }

                // Radio Indicator
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) tokens.primary else Color.Transparent,
                    border = BorderStroke(2.dp, if (isSelected) tokens.primary else tokens.surfaceSoft),
                    modifier = Modifier.size(22.dp),
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(tokens.radius.smallChip),
                        color = if (isSelected) tokens.primary.copy(alpha = 0.12f) else tokens.background,
                        border = BorderStroke(
                            0.5.dp,
                            if (isSelected) tokens.primary.copy(alpha = 0.35f) else tokens.onSurface.copy(alpha = 0.08f)
                        ),
                    ) {
                        Text(
                            text = tag,
                            style = FinluxTextStyles.MicroLabel.copy(fontWeight = FontWeight.Medium),
                            color = if (isSelected) tokens.primary else tokens.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.radius.input))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(tokens.radius.input),
        color = if (selected) tokens.primary else tokens.surfaceSoft,
        border = BorderStroke(1.dp, if (selected) tokens.primary else Color.Transparent),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else tokens.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = FinluxTextStyles.Caption,
                color = if (selected) Color.White else tokens.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PrismSettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(tokens.radius.smallChip),
                color = tokens.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = label,
                style = FinluxTextStyles.CardTitle,
                color = tokens.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = tokens.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PrismAvatarSourceButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(tokens.radius.input),
        color = tokens.surfaceSoft,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tokens.primary)
            Text(label, fontWeight = FontWeight.SemiBold, style = FinluxTextStyles.Body)
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "avatar-capture").apply { mkdirs() }
    val file = File.createTempFile("finlux-avatar-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun Brush.copyAlpha(alpha: Float): Brush {
    return this
}
