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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import com.finlux.app.presentation.settings.salary.SalaryCycleSettingsSheet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
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
import com.finlux.app.core.designsystem.GlassCard
import com.finlux.app.core.designsystem.component.FinluxHeroCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.settings.SettingsViewModel
import java.io.File
import com.finlux.app.domain.model.BiometricLockTimeout
import kotlinx.coroutines.delay

internal enum class PrismSettingsAction(val route: String? = null) {
    ACCOUNT,
    WALLETS("wallets"),
    BUDGET("budget"),
    DEBT("debt"),
    DEALS("deals"),
    APPEARANCE,
    CATEGORIES("categories"),
    REMINDERS("reminders"),
    NOTIFICATIONS("notifications"),
    BACKUP,
    SECURITY,
    SUPPORT,
    ABOUT,
    UPDATE,
}

internal val prismSettingsActions = PrismSettingsAction.entries

private data class InfoDialogContent(val title: String, val message: String)

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
    val avatarState = viewModel.avatarState.collectAsStateWithLifecycle().value
    val nameState = viewModel.nameState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    var showAvatarSource by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showNameEditor by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var showBiometricTimeoutDialog by remember { mutableStateOf(false) }
    var showSalaryCycleSheet by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<InfoDialogContent?>(null) }
    var amountVisible by remember { mutableStateOf(true) }
    var nameDraft by remember(user?.uid) { mutableStateOf(user?.displayName.orEmpty()) }

    fun openNameEditor() {
        nameDraft = user?.displayName.orEmpty()
        viewModel.clearNameMessage()
        showNameEditor = true
    }

    fun navigateTo(action: PrismSettingsAction) {
        action.route?.let(onNavigate)
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
            title = { Text("Đổi ảnh đại diện", style = FinluxTextStyles.SectionTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsDialogAction(Icons.Default.PhotoLibrary, "Chọn từ thư viện") {
                        showAvatarSource = false
                        galleryLauncher.launch("image/*")
                    }
                    SettingsDialogAction(Icons.Default.CameraAlt, "Chụp ảnh mới") {
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
            containerColor = tokens.surface,
        )
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { if (!nameState.isLoading) showNameEditor = false },
            title = { Text("Thông tin tài khoản", style = FinluxTextStyles.SectionTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.take(40) },
                        label = { Text("Tên người dùng") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.radius.input),
                    )
                    Text(user?.email.orEmpty(), style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    nameState.message?.let { message ->
                        Text(
                            message,
                            style = FinluxTextStyles.Caption,
                            color = if (nameState.isError) FinluxColors.ExpenseRed else tokens.primary,
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
                    if (nameState.isLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Lưu tên", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditor = false }, enabled = !nameState.isLoading) { Text("Hủy") }
            },
            shape = RoundedCornerShape(tokens.radius.dialog),
            containerColor = tokens.surface,
        )
    }

    if (showAppearance) {
        AppearanceDialog(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
            selectedUiStyle = selectedUiStyle,
            onUiStyleSelected = onUiStyleSelected,
            uiPreferences = uiPreferences,
            onUiPreferencesChanged = onUiPreferencesChanged,
            onDismiss = { showAppearance = false },
        )
    }

    infoDialog?.let { content ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            icon = { FinluxBrandMark(size = 42.dp) },
            title = { Text(content.title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = { Text(content.message, color = tokens.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { infoDialog = null }) { Text("Đã hiểu") } },
            shape = RoundedCornerShape(tokens.radius.dialog),
            containerColor = tokens.surface,
        )
    }

    if (showBiometricTimeoutDialog) {
        BiometricTimeoutDialog(
            currentTimeout = uiPreferences.biometricTimeout,
            onSelect = { timeout ->
                onUiPreferencesChanged(uiPreferences.copy(biometricTimeout = timeout))
                showBiometricTimeoutDialog = false
            },
            onDismiss = { showBiometricTimeoutDialog = false },
        )
    }

    if (showSalaryCycleSheet) {
        SalaryCycleSettingsSheet(
            onDismiss = { showSalaryCycleSheet = false },
        )
    }

    LaunchedEffect(nameState.message, nameState.isError) {
        if (nameState.message != null && !nameState.isError) {
            delay(650)
            showNameEditor = false
        }
    }

    Scaffold(
        topBar = { SettingsTitle() },
        containerColor = tokens.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ProfileCard(
                    name = user?.displayName?.ifBlank { "Người dùng FinLux" } ?: "Người dùng FinLux",
                    email = user?.email.orEmpty(),
                    photoUrl = user?.photoUrl,
                    isAvatarLoading = avatarState.isLoading,
                    onAvatarClick = { showAvatarSource = true },
                    onProfileClick = ::openNameEditor,
                )
            }

            item {
                FinluxHeroCard(
                    title = "Tổng tài sản",
                    amountText = formatVndAmount(wallets.sumOf { it.balance.value }),
                    isAmountVisible = amountVisible,
                    onToggleVisibility = { amountVisible = !amountVisible },
                    extraContent = {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "${wallets.size} ví và tài khoản đang được tổng hợp",
                            style = FinluxTextStyles.Caption,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    },
                )
            }

            item {
                SettingsGroupCard(
                    listOf(
                        SettingsMenuItem(Icons.Default.PersonOutline, "Tài khoản", Color(0xFF2563EB), onClick = ::openNameEditor),
                        SettingsMenuItem(Icons.Default.AccountBalanceWallet, "Ví & tài khoản", Color(0xFF7C3AED), onClick = { navigateTo(PrismSettingsAction.WALLETS) }),
                        SettingsMenuItem(Icons.Default.Savings, "Ngân sách", Color(0xFF059669), onClick = { navigateTo(PrismSettingsAction.BUDGET) }),
                        SettingsMenuItem(Icons.Default.Palette, "Giao diện", Color(0xFFF97316), onClick = { showAppearance = true }),
                    ),
                )
            }

            item {
                SettingsSectionLabel("QUẢN LÝ TÀI CHÍNH")
                Spacer(Modifier.height(8.dp))
                SettingsGroupCard(
                    listOf(
                        SettingsMenuItem(
                            Icons.Default.CreditCard,
                            "Quản lý nợ & Tín dụng",
                            Color(0xFFE11D48),
                            subtitle = "Kế hoạch thoát nợ Snowball & Avalanche",
                            onClick = { navigateTo(PrismSettingsAction.DEBT) },
                        ),
                        SettingsMenuItem(
                            Icons.Default.TrendingUp,
                            "Thương vụ & Đầu tư sinh lời",
                            Color(0xFF10B981),
                            subtitle = "Theo dõi vốn xuất, hoàn vốn & lợi nhuận ROI",
                            badge = "Mới",
                            onClick = { navigateTo(PrismSettingsAction.DEALS) },
                        ),
                        SettingsMenuItem(
                            Icons.Default.CalendarMonth,
                            "Tháng tài chính & Chu kỳ lương",
                            Color(0xFF059669),
                            subtitle = "Tính toán thu chi theo ngày nhận lương",
                            onClick = { showSalaryCycleSheet = true },
                        ),
                        SettingsMenuItem(Icons.Default.Category, "Danh mục thu chi", Color(0xFF8B5CF6), onClick = { navigateTo(PrismSettingsAction.CATEGORIES) }),
                        SettingsMenuItem(Icons.Default.Alarm, "Nhắc nhở thanh toán", Color(0xFFF59E0B), onClick = { navigateTo(PrismSettingsAction.REMINDERS) }),
                    ),
                )
            }

            item {
                SettingsSectionLabel("CÀI ĐẶT ỨNG DỤNG")
                Spacer(Modifier.height(8.dp))
                SettingsGroupCard(
                    listOf(
                        SettingsMenuItem(Icons.Default.NotificationsNone, "Thông báo", Color(0xFF4F46E5), onClick = { navigateTo(PrismSettingsAction.NOTIFICATIONS) }),
                        SettingsMenuItem(
                            Icons.Default.Backup,
                            "Sao lưu dữ liệu",
                            Color(0xFF2563EB),
                            subtitle = "Đồng bộ tự động qua tài khoản FinLux",
                            onClick = {
                                infoDialog = InfoDialogContent(
                                    "Sao lưu & đồng bộ",
                                    "Dữ liệu tài chính được Firestore lưu và đồng bộ tự động giữa các thiết bị đăng nhập cùng tài khoản. Khi có mạng, thay đổi sẽ được cập nhật theo thời gian thực.",
                                )
                            },
                        ),
                        SettingsMenuItem(
                            Icons.Default.Fingerprint,
                            "Bảo mật",
                            Color(0xFF4F46E5),
                            subtitle = "Khóa ứng dụng bằng sinh trắc học",
                            trailing = {
                                Switch(
                                    checked = uiPreferences.biometricEnabled,
                                    onCheckedChange = { isEnabled ->
                                        if (isEnabled) {
                                            if (com.finlux.app.core.security.BiometricHelper.canAuthenticate(context)) {
                                                onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = true))
                                            } else {
                                                android.widget.Toast.makeText(context, "Thiết bị chưa thiết lập hoặc không hỗ trợ sinh trắc học", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = false))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = tokens.primary,
                                    ),
                                )
                            },
                            onClick = {
                                if (!uiPreferences.biometricEnabled) {
                                    if (com.finlux.app.core.security.BiometricHelper.canAuthenticate(context)) {
                                        onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = true))
                                    } else {
                                        android.widget.Toast.makeText(context, "Thiết bị chưa thiết lập hoặc không hỗ trợ sinh trắc học", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    onUiPreferencesChanged(uiPreferences.copy(biometricEnabled = false))
                                }
                            },
                        ),
                    ).let { items ->
                        if (uiPreferences.biometricEnabled) {
                            items + SettingsMenuItem(
                                Icons.Default.Alarm,
                                "Thời gian tự động khóa",
                                Color(0xFF0284C7),
                                subtitle = uiPreferences.biometricTimeout.label,
                                onClick = { showBiometricTimeoutDialog = true },
                            )
                        } else {
                            items
                        }
                    },
                )
            }

            item {
                SettingsSectionLabel("HỖ TRỢ")
                Spacer(Modifier.height(8.dp))
                SettingsGroupCard(
                    listOf(
                        SettingsMenuItem(Icons.Default.HeadsetMic, "Hỗ trợ", Color(0xFF4F46E5)) {
                            infoDialog = InfoDialogContent(
                                "Trung tâm hỗ trợ",
                                "Nếu dữ liệu chưa cập nhật, hãy kiểm tra kết nối mạng và đăng nhập đúng tài khoản. Với thông báo, hãy kiểm tra quyền thông báo của FinLux trong Cài đặt Android.",
                            )
                        },
                    ),
                )
            }

            item {
                SettingsSectionLabel("THÔNG TIN")
                Spacer(Modifier.height(8.dp))
                SettingsGroupCard(
                    listOf(
                        SettingsMenuItem(Icons.Default.Info, "Giới thiệu FinLux", Color(0xFF4F46E5)) {
                            infoDialog = InfoDialogContent(
                                "FinLux",
                                "Tài chính rõ ràng, cuộc sống nhẹ nhàng. FinLux giúp quản lý thu chi, ví, ngân sách, mục tiêu và báo cáo trong một trải nghiệm thống nhất.\n\nPhiên bản ${BuildConfig.VERSION_NAME}",
                            )
                        },
                        SettingsMenuItem(
                            Icons.Default.SystemUpdate,
                            "Kiểm tra cập nhật",
                            Color(0xFF4F46E5),
                            subtitle = "Phiên bản ${BuildConfig.VERSION_NAME}",
                            badge = "Kiểm tra",
                            onClick = onCheckUpdate,
                        ),
                    ),
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.radius.standardCard))
                        .clickable { viewModel.signOut(onSignedOut) },
                    shape = RoundedCornerShape(tokens.radius.standardCard),
                    color = FinluxColors.ExpenseRed.copy(alpha = if (tokens.isDark) 0.12f else 0.06f),
                    border = BorderStroke(1.dp, FinluxColors.ExpenseRed.copy(alpha = 0.42f)),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 18.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = FinluxColors.ExpenseRed)
                        Spacer(Modifier.width(10.dp))
                        Text("Đăng xuất", color = FinluxColors.ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }

            avatarState.message?.let { message ->
                item {
                    Text(
                        message,
                        style = FinluxTextStyles.Caption,
                        color = if (avatarState.isError) FinluxColors.ExpenseRed else tokens.primary,
                    )
                    LaunchedEffect(message) { delay(2_500); viewModel.clearAvatarMessage() }
                }
            }
        }
    }
}

@Composable
private fun SettingsTitle() {
    val tokens = LocalFinluxTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 14.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Cài đặt",
            style = FinluxTextStyles.ScreenTitle.copy(fontSize = 27.sp),
            color = tokens.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProfileCard(
    name: String,
    email: String,
    photoUrl: String?,
    isAvatarLoading: Boolean,
    onAvatarClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onProfileClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.clickable(onClick = onAvatarClick)) {
                FinluxUserAvatar(
                    photoUrl = photoUrl,
                    displayName = name,
                    size = 76.dp,
                    editable = false,
                    onClick = onAvatarClick,
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp),
                    shape = CircleShape,
                    color = tokens.primary,
                    border = BorderStroke(2.dp, tokens.surface),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAvatarLoading) {
                            CircularProgressIndicator(Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Đổi ảnh đại diện", tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    name,
                    style = FinluxTextStyles.SectionTitle,
                    color = tokens.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    email.ifBlank { "Tài khoản FinLux" },
                    style = FinluxTextStyles.Caption,
                    color = tokens.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF5B32F4), Color(0xFF1749D9))))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text("◆  FinLux Premium", color = Color.White, style = FinluxTextStyles.Caption, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Mở hồ sơ", tint = tokens.onSurfaceVariant, modifier = Modifier.size(26.dp))
        }
    }
}

private data class SettingsMenuItem(
    val icon: ImageVector,
    val title: String,
    val tint: Color,
    val subtitle: String? = null,
    val badge: String? = null,
    val trailing: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit,
)

@Composable
private fun SettingsGroupCard(items: List<SettingsMenuItem>) {
    val tokens = LocalFinluxTokens.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = item.onClick)
                        .padding(vertical = 11.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = item.tint.copy(alpha = if (tokens.isDark) 0.18f else 0.10f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = item.tint, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = FinluxTextStyles.CardTitle, color = tokens.onSurface, fontWeight = FontWeight.SemiBold)
                        item.subtitle?.let {
                            Spacer(Modifier.height(2.dp))
                            Text(it, style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                        }
                    }
                    item.badge?.let { badge ->
                        Surface(shape = RoundedCornerShape(10.dp), color = item.tint.copy(alpha = 0.10f)) {
                            Text(
                                badge,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                style = FinluxTextStyles.MicroLabel,
                                color = item.tint,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (item.trailing != null) item.trailing.invoke()
                    else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.onSurfaceVariant.copy(alpha = 0.72f))
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = tokens.onSurface.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    val tokens = LocalFinluxTokens.current
    Text(
        text,
        modifier = Modifier.padding(start = 8.dp),
        style = FinluxTextStyles.Caption,
        color = tokens.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.7.sp,
    )
}

@Composable
private fun AppearanceDialog(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    selectedUiStyle: AppUiStyle,
    onUiStyleSelected: (AppUiStyle) -> Unit,
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Giao diện", style = FinluxTextStyles.SectionTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("CHỦ ĐỀ", style = FinluxTextStyles.MicroLabel, color = tokens.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChoice(Icons.Default.LightMode, "Sáng", selectedTheme == ThemePreference.LIGHT, Modifier.weight(1f)) {
                        onThemeSelected(ThemePreference.LIGHT)
                    }
                    ThemeChoice(Icons.Default.DarkMode, "Tối", selectedTheme == ThemePreference.DARK, Modifier.weight(1f)) {
                        onThemeSelected(ThemePreference.DARK)
                    }
                    ThemeChoice(Icons.Default.SettingsBrightness, "Hệ thống", selectedTheme == ThemePreference.SYSTEM, Modifier.weight(1f)) {
                        onThemeSelected(ThemePreference.SYSTEM)
                    }
                }
                Text("PHONG CÁCH", style = FinluxTextStyles.MicroLabel, color = tokens.onSurfaceVariant, fontWeight = FontWeight.Bold)
                StyleChoice("FinLux Prism", "Bố cục dữ liệu rõ ràng", selectedUiStyle == AppUiStyle.PRISM) {
                    onUiStyleSelected(AppUiStyle.PRISM)
                }
                StyleChoice("Modern Luxury", "Liquid Glass nhiều lớp", selectedUiStyle == AppUiStyle.MODERN_LUXURY) {
                    onUiStyleSelected(AppUiStyle.MODERN_LUXURY)
                }
                StyleChoice("Liquid Glass Classic", "Nhẹ và tương phản cao", selectedUiStyle == AppUiStyle.CLASSIC_LIQUID) {
                    onUiStyleSelected(AppUiStyle.CLASSIC_LIQUID)
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hiệu ứng chuyển động", color = tokens.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Spring, ripple và phản hồi khi chạm", style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiPreferences.animationsEnabled,
                        onCheckedChange = { onUiPreferencesChanged(uiPreferences.copy(animationsEnabled = it)) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Xong", fontWeight = FontWeight.Bold) } },
        shape = RoundedCornerShape(tokens.radius.dialog),
        containerColor = tokens.surface,
    )
}

@Composable
private fun ThemeChoice(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) tokens.primary.copy(alpha = 0.14f) else tokens.surfaceSoft,
        border = BorderStroke(1.dp, if (selected) tokens.primary else tokens.onSurface.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) tokens.primary else tokens.onSurfaceVariant, modifier = Modifier.size(19.dp))
            Text(label, style = FinluxTextStyles.MicroLabel, color = tokens.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun StyleChoice(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val tokens = LocalFinluxTokens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) tokens.primary.copy(alpha = 0.12f) else tokens.surfaceSoft,
        border = BorderStroke(1.dp, if (selected) tokens.primary else Color.Transparent),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) tokens.primary else tokens.onSurface.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = tokens.onSurface, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = FinluxTextStyles.Caption, color = tokens.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsDialogAction(icon: ImageVector, label: String, onClick: () -> Unit) {
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
            Text(label, color = tokens.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "avatar-capture").apply { mkdirs() }
    val file = File.createTempFile("finlux-avatar-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun BiometricTimeoutDialog(
    currentTimeout: BiometricLockTimeout,
    onSelect: (BiometricLockTimeout) -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = tokens.primary) },
        title = { Text("Thời gian tự động khóa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BiometricLockTimeout.entries.forEach { timeout ->
                    val isSelected = timeout == currentTimeout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) tokens.primary.copy(alpha = 0.12f) else tokens.surfaceSoft,
                        border = if (isSelected) BorderStroke(1.5.dp, tokens.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(timeout) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = timeout.label,
                                style = FinluxTextStyles.Body.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) tokens.primary else tokens.onSurface,
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Đóng") }
        },
        shape = RoundedCornerShape(tokens.radius.dialog),
        containerColor = tokens.surface,
    )
}

