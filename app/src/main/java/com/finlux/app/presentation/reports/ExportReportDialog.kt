package com.finlux.app.presentation.reports

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finlux.app.core.export.ReportExporter
import java.time.format.DateTimeFormatter

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    EXCEL("Excel / CSV (.csv)", ".csv", "text/csv"),
    PDF("Tài liệu PDF (.pdf)", ".pdf", "application/pdf")
}

@Composable
fun ExportReportDialog(
    state: ReportsUiState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.EXCEL) }
    var isExporting by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.FileDownload, null, tint = MaterialTheme.colorScheme.primary)
                Text("Xuất Báo Cáo Tài Chính", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Xuất dữ liệu chi tiêu & dòng tiền từ ${state.range.start.format(formatter)} đến ${state.range.end.format(formatter)} (${state.transactionCount} giao dịch).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text("Chọn định dạng xuất file:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormatOption(
                        format = ExportFormat.EXCEL,
                        selected = selectedFormat == ExportFormat.EXCEL,
                        icon = Icons.Default.TableChart,
                        description = "Bảng dữ liệu Excel đầy đủ, phân tích danh mục & chi tiết",
                        onSelect = { selectedFormat = ExportFormat.EXCEL },
                    )

                    ExportFormatOption(
                        format = ExportFormat.PDF,
                        selected = selectedFormat == ExportFormat.PDF,
                        icon = Icons.Default.PictureAsPdf,
                        description = "Tài liệu PDF chuyên nghiệp kèm biểu đồ KPI & giao dịch",
                        onSelect = { selectedFormat = ExportFormat.PDF },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    try {
                        when (selectedFormat) {
                            ExportFormat.EXCEL -> {
                                val uri = ReportExporter.exportToCsv(
                                    context = context,
                                    range = state.range,
                                    summary = state.summary,
                                    expensesByCategory = state.expensesByCategory,
                                    transactions = state.filteredTransactions,
                                    categories = state.categories,
                                    wallets = state.wallets,
                                )
                                ReportExporter.shareExportedFile(
                                    context = context,
                                    fileUri = uri,
                                    mimeType = ExportFormat.EXCEL.mimeType,
                                    title = "Báo cáo tài chính FinLux",
                                )
                            }
                            ExportFormat.PDF -> {
                                val uri = ReportExporter.exportToPdf(
                                    context = context,
                                    range = state.range,
                                    summary = state.summary,
                                    expensesByCategory = state.expensesByCategory,
                                    transactions = state.filteredTransactions,
                                    categories = state.categories,
                                    wallets = state.wallets,
                                )
                                ReportExporter.shareExportedFile(
                                    context = context,
                                    fileUri = uri,
                                    mimeType = ExportFormat.PDF.mimeType,
                                    title = "Báo cáo tài chính FinLux",
                                )
                            }
                        }
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi khi xuất file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isExporting = false
                    }
                },
                enabled = !isExporting,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(if (isExporting) "Đang xuất..." else "Xuất & Chia sẻ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Hủy")
            }
        },
    )
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    selected: Boolean,
    icon: ImageVector,
    description: String,
    onSelect: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Icon(icon, null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(format.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
