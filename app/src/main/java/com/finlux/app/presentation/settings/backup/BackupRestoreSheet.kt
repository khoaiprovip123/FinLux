package com.finlux.app.presentation.settings.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finlux.app.core.designsystem.LiquidGlassSurface
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.backup.BackupPreviewSummary
import com.finlux.app.domain.model.backup.RestoreReport
import com.finlux.app.domain.model.backup.RestoreStrategy
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupRestoreViewModel = hiltViewModel(),
) {
    val tokens = LocalFinluxTokens.current
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    fun shareBackup(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "FinLux Data Backup")
            putExtra(Intent.EXTRA_TEXT, "Bản sao lưu dữ liệu tài chính FinLux")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Chia sẻ bản sao lưu FinLux"))
    }

    // Confirmation dialog for FULL_OVERWRITE
    if (state.showFullOverwriteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = tokens.error)
                    Text(
                        "Cảnh báo: Ghi đè toàn bộ",
                        fontWeight = FontWeight.Bold,
                        color = tokens.textPrimary,
                        fontSize = 18.sp,
                    )
                }
            },
            text = {
                Text(
                    "Toàn bộ dữ liệu hiện tại trên thiết bị (ví, giao dịch, danh mục, ngân sách, mục tiêu, nợ) sẽ bị XÓA VĨNH VIỄN và thay thế hoàn toàn bằng tệp sao lưu này.\n\nHành động này KHÔNG THỂ HOÀN TÁC. Bạn có chắc chắn muốn tiếp tục?",
                    color = tokens.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAndRestore() },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.error),
                ) {
                    Text("Xóa sạch & Khôi phục", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Hủy", color = tokens.textSecondary)
                }
            },
            containerColor = tokens.surface,
        )
    }

    // Restore completion report dialog
    state.restoreReport?.let { report ->
        RestoreReportDialog(
            report = report,
            onDismiss = { viewModel.dismissReportDialog() },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tokens.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Sao lưu & Khôi phục",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.textPrimary,
                        )
                        Text(
                            text = "Bảo vệ toàn vẹn lịch sử tài chính",
                            fontSize = 13.sp,
                            color = tokens.textSecondary,
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = tokens.textSecondary)
                }
            }

            // Progress bar when busy
            AnimatedVisibility(visible = state.isExporting || state.isValidating || state.isRestoring) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = tokens.primary,
                        trackColor = tokens.surfaceSoft,
                    )
                    Text(
                        text = when {
                            state.isExporting -> "Đang tạo bản sao lưu dữ liệu..."
                            state.isValidating -> "Đang kiểm tra tính toàn vẹn và chữ ký SHA-256..."
                            state.isRestoring -> "Đang nạp và đối soát số dư dữ liệu..."
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = tokens.textSecondary,
                    )
                }
            }

            // ── KHU VỰC 1: ĐỒNG BỘ ĐÁM MÂY (FIRESTORE) ─────────────────────────
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isCloudSyncing) FinluxColors.IncomeGreen.copy(alpha = 0.15f)
                                else tokens.surfaceSoft
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (state.isCloudSyncing) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (state.isCloudSyncing) FinluxColors.IncomeGreen else tokens.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isCloudSyncing) FinluxColors.IncomeGreen else tokens.border)
                            )
                            Text(
                                text = if (state.isCloudSyncing) "Đồng bộ thời gian thực" else "Ngoại tuyến (Offline)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = tokens.textPrimary,
                            )
                        }
                        Text(
                            text = state.currentUserEmail ?: "Chưa liên kết tài khoản FinLux",
                            fontSize = 12.sp,
                            color = tokens.textSecondary,
                        )
                    }
                }
            }

            // ── KHU VỰC 2: SAO LƯU THỦ CÔNG (LOCAL SNAPSHOT EXPORT) ───────────
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sao lưu cục bộ (.finlux)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = tokens.textPrimary,
                    )
                    Text(
                        text = "Tạo snapshot dữ liệu độc lập kèm mã băm SHA-256 chống chỉnh sửa giả mạo.",
                        fontSize = 13.sp,
                        color = tokens.textSecondary,
                    )

                    // Stats box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceSoft)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Bản sao lưu sẽ bao gồm:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = tokens.textPrimary,
                            )
                            Text(
                                text = "• ${state.dataStats.walletCount} ví · ${state.dataStats.transactionCount} giao dịch · ${state.dataStats.categoryCount} danh mục",
                                fontSize = 12.sp,
                                color = tokens.textSecondary,
                            )
                            Text(
                                text = "• ${state.dataStats.budgetCount} ngân sách · ${state.dataStats.goalCount} mục tiêu · ${state.dataStats.debtCount} khoản nợ",
                                fontSize = 12.sp,
                                color = tokens.textSecondary,
                            )
                            Text(
                                text = "• Ước tính dung lượng: ${state.dataStats.estimatedSizeLabel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = tokens.primary,
                            )
                        }
                    }

                    // Success or Error message
                    state.exportSuccessMessage?.let { msg ->
                        Text(text = "✅ $msg", fontSize = 12.sp, color = FinluxColors.IncomeGreen)
                    }
                    state.exportError?.let { err ->
                        Text(text = "❌ $err", fontSize = 12.sp, color = tokens.error)
                    }

                    Button(
                        onClick = { viewModel.exportBackup { uri -> shareBackup(uri) } },
                        enabled = !state.isExporting,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Đang tạo bản sao lưu...", color = Color.White)
                        } else {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Tạo bản sao lưu & Chia sẻ", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            // ── KHU VỰC 3: KHÔI PHỤC DỮ LIỆU (DATA RESTORE) ───────────────────
            LiquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Khôi phục dữ liệu từ tệp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = tokens.textPrimary,
                    )
                    Text(
                        text = "Nạp lại dữ liệu từ tệp .finlux đã lưu. Hệ thống tự động xác thực chữ ký SHA-256.",
                        fontSize = 13.sp,
                        color = tokens.textSecondary,
                    )

                    // File picker button
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        enabled = !state.isValidating && !state.isRestoring,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border),
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp), tint = tokens.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (state.selectedFileName != null) "Đổi tệp sao lưu khác" else "Chọn file sao lưu (.finlux)",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Validation error
                    state.validationError?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.error.copy(alpha = 0.1f))
                                .border(1.dp, tokens.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "❌ $err", color = tokens.error, fontSize = 12.sp)
                        }
                    }

                    // Preview Card
                    state.previewSummary?.let { preview ->
                        RestorePreviewCard(
                            preview = preview,
                            fileName = state.selectedFileName ?: "FinLux_Backup.finlux",
                            selectedStrategy = state.selectedStrategy,
                            onStrategySelected = { viewModel.selectStrategy(it) },
                            onRestoreClicked = { viewModel.requestRestore() },
                            isRestoring = state.isRestoring,
                            onClearFile = { viewModel.clearSelectedFile() },
                        )
                    }

                    // Restore error
                    state.restoreError?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.error.copy(alpha = 0.1f))
                                .border(1.dp, tokens.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "❌ $err", color = tokens.error, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestorePreviewCard(
    preview: BackupPreviewSummary,
    fileName: String,
    selectedStrategy: RestoreStrategy,
    onStrategySelected: (RestoreStrategy) -> Unit,
    onRestoreClicked: () -> Unit,
    isRestoring: Boolean,
    onClearFile: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.surfaceSoft)
            .border(1.dp, tokens.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = fileName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = tokens.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClearFile, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Bỏ chọn", tint = tokens.textSecondary, modifier = Modifier.size(16.dp))
                }
            }

            // Info rows
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "• Xuất ngày: ${dateFormatter.format(preview.exportedAt)}",
                    fontSize = 12.sp,
                    color = tokens.textSecondary,
                )
                Text(
                    text = "• Phiên bản: v${preview.appVersion} (code ${preview.appVersionCode}, schema v${preview.schemaVersion})",
                    fontSize = 12.sp,
                    color = tokens.textSecondary,
                )
                Text(
                    text = "• Số lượng: ${preview.walletCount} ví · ${preview.transactionCount} giao dịch · ${preview.categoryCount} danh mục",
                    fontSize = 12.sp,
                    color = tokens.textSecondary,
                )
                Text(
                    text = "• Bổ sung: ${preview.budgetCount} ngân sách · ${preview.debtCount} khoản nợ · ${preview.goalCount} mục tiêu",
                    fontSize = 12.sp,
                    color = tokens.textSecondary,
                )
            }

            // Integrity badge
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinluxColors.IncomeGreen, modifier = Modifier.size(16.dp))
                Text("Chữ ký SHA-256 hợp lệ", fontSize = 12.sp, color = FinluxColors.IncomeGreen, fontWeight = FontWeight.Medium)
            }

            // Cross-account warning
            if (preview.isCrossAccount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FinluxColors.WarningAmber.copy(alpha = 0.12f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "⚠️ Dữ liệu từ tài khoản khác (${preview.exportedByUid.take(8)}...): ID thực thể sẽ được ánh xạ lại tự động để bảo toàn quan hệ.",
                        fontSize = 11.sp,
                        color = FinluxColors.WarningAmber,
                    )
                }
            }

            // Strategy selection
            Text(
                text = "Phương thức khôi phục:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = tokens.textPrimary,
            )

            // Option 1: SMART_MERGE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onStrategySelected(RestoreStrategy.SMART_MERGE) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RadioButton(
                    selected = selectedStrategy == RestoreStrategy.SMART_MERGE,
                    onClick = { onStrategySelected(RestoreStrategy.SMART_MERGE) },
                    colors = RadioButtonDefaults.colors(selectedColor = tokens.primary),
                )
                Column {
                    Text("Hợp nhất thông minh (Khuyến nghị)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = tokens.textPrimary)
                    Text("Giữ bản ghi mới hơn; bảo toàn dữ liệu hiện tại, đổi tên danh mục trùng.", fontSize = 11.sp, color = tokens.textSecondary)
                }
            }

            // Option 2: FULL_OVERWRITE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onStrategySelected(RestoreStrategy.FULL_OVERWRITE) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RadioButton(
                    selected = selectedStrategy == RestoreStrategy.FULL_OVERWRITE,
                    onClick = { onStrategySelected(RestoreStrategy.FULL_OVERWRITE) },
                    colors = RadioButtonDefaults.colors(selectedColor = tokens.error),
                )
                Column {
                    Text("Ghi đè toàn bộ (Wipe & Replace)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = tokens.error)
                    Text("Xóa sạch toàn bộ dữ liệu hiện tại trước khi nạp tệp này.", fontSize = 11.sp, color = tokens.textSecondary)
                }
            }

            // Start restore button
            Button(
                onClick = onRestoreClicked,
                enabled = !isRestoring,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedStrategy == RestoreStrategy.FULL_OVERWRITE) tokens.error else tokens.primary
                ),
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Đang khôi phục...", color = Color.White)
                } else {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (selectedStrategy == RestoreStrategy.FULL_OVERWRITE) "Xóa & Khôi phục toàn bộ" else "Bắt đầu hợp nhất dữ liệu",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun RestoreReportDialog(
    report: RestoreReport,
    onDismiss: () -> Unit,
) {
    val tokens = LocalFinluxTokens.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinluxColors.IncomeGreen)
                Text(
                    text = "Khôi phục hoàn tất!",
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                    fontSize = 18.sp,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Chiến lược: ${if (report.strategy == RestoreStrategy.FULL_OVERWRITE) "Ghi đè toàn bộ" else "Hợp nhất thông minh"}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = tokens.textPrimary,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(tokens.surfaceSoft)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("• Ví đã phục hồi: ${report.walletsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Giao dịch: ${report.transactionsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Danh mục: ${report.categoriesRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Ngân sách: ${report.budgetsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Khoản nợ: ${report.debtsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Mục tiêu: ${report.goalsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Nhắc nhở: ${report.remindersRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        Text("• Deal đầu tư: ${report.dealsRestored}", fontSize = 12.sp, color = tokens.textSecondary)
                        if (report.strategy == RestoreStrategy.SMART_MERGE) {
                            Text("• Xung đột đã giải quyết: ${report.conflictsResolved}", fontSize = 12.sp, color = tokens.textSecondary)
                            Text("• Bản ghi bỏ qua (cũ hơn): ${report.skippedCount}", fontSize = 12.sp, color = tokens.textSecondary)
                        }
                    }
                }

                // Balance audit result
                if (report.balanceAuditPassed) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinluxColors.IncomeGreen, modifier = Modifier.size(16.dp))
                        Text("Kiểm toán số dư: 100% khớp", fontSize = 12.sp, color = FinluxColors.IncomeGreen, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = tokens.error, modifier = Modifier.size(16.dp))
                            Text("Cảnh báo: Kiểm toán số dư phát hiện lệch", fontSize = 12.sp, color = tokens.error, fontWeight = FontWeight.Bold)
                        }
                        report.balanceDiscrepancies.forEach { disc ->
                            Text(text = "• $disc", fontSize = 11.sp, color = tokens.error)
                        }
                    }
                }

                Text(
                    text = "Thời gian thực hiện: ${report.durationMs} ms",
                    fontSize = 11.sp,
                    color = tokens.textSecondary,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
            ) {
                Text("Hoàn tất", color = Color.White)
            }
        },
        containerColor = tokens.surface,
    )
}
