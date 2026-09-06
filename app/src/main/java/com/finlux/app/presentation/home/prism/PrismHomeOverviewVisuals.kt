package com.finlux.app.presentation.home.prism

import com.finlux.app.core.designsystem.theme.FinluxPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.finlux.app.core.designsystem.component.FinluxLazyColumn
import com.finlux.app.core.designsystem.component.FinluxListType
import com.finlux.app.core.designsystem.component.FinluxScreenScaffold
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.core.designsystem.FinluxTextStyles
import com.finlux.app.core.designsystem.FinluxUserAvatar
import com.finlux.app.core.designsystem.LocalUiPreferences
import com.finlux.app.core.designsystem.NotificationPermissionHandler
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.core.designsystem.colorFromHex
import com.finlux.app.core.designsystem.component.FinluxEmptyState
import com.finlux.app.core.designsystem.component.FinluxTransactionGroup
import com.finlux.app.core.designsystem.component.FinluxSoftCard
import com.finlux.app.core.designsystem.component.formatVndAmount
import com.finlux.app.core.designsystem.theme.FinluxColors
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.navigation.Route
import com.finlux.app.domain.model.Budget
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.Wallet
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.HomeViewModel
import com.finlux.app.presentation.savingspin.SavingSpinAction
import com.finlux.app.presentation.savingspin.SavingSpinUiState
import com.finlux.app.presentation.savingspin.components.SavingSpinHomeCard
import com.finlux.app.presentation.transaction.TransactionActionDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun PrismMiniBarChart(
    values: List<Long>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    val barCount = 5
    val paddedValues = if (values.size >= barCount) values.take(barCount) else values + List(barCount - values.size) { 0L }
    val maxVal = paddedValues.maxOfOrNull { kotlin.math.abs(it) }?.coerceAtLeast(1L) ?: 1L
    val hasData = paddedValues.any { it != 0L }

    Row(
        modifier = modifier
            .width(56.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        paddedValues.forEach { rawVal ->
            val absVal = kotlin.math.abs(rawVal)
            val heightFraction = if (hasData && absVal > 0L) {
                (absVal.toFloat() / maxVal).coerceIn(0.18f, 1.0f)
            } else {
                0.08f
            }
            val isMax = hasData && absVal == maxVal && absVal > 0L

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp))
                    .background(
                        if (isMax) barColor.copy(alpha = 0.95f)
                        else if (absVal > 0L) barColor.copy(alpha = 0.45f)
                        else barColor.copy(alpha = 0.14f)
                    ),
            )
        }
    }
}

@Composable
internal fun PrismCardBackdropTexture(
    theme: PrismCardTheme,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Common soft ambient bloom & specular rim
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tintColor.copy(alpha = 0.14f),
                    tintColor.copy(alpha = 0.03f),
                    FinluxPalette.Transparent,
                ),
                center = Offset(w * 0.85f, h * 0.35f),
                radius = w * 0.45f,
            ),
            center = Offset(w * 0.85f, h * 0.35f),
            radius = w * 0.45f,
        )

        // Top edge specular accent line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    FinluxPalette.Transparent,
                    tintColor.copy(alpha = 0.35f),
                    tintColor.copy(alpha = 0.10f),
                    FinluxPalette.Transparent,
                ),
                startX = w * 0.15f,
                endX = w * 0.85f,
            ),
            start = Offset(w * 0.15f, 1.dp.toPx()),
            end = Offset(w * 0.85f, 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )

        // 2. Distinctive bank-grade security watermark patterns per card theme
        when (theme) {
            PrismCardTheme.WALLET -> {
                // --- THẺ VÍ: HỌA TIẾT KHO BẢO MẬT & VÒNG ĐỒNG TÂM KỸ THUẬT SỐ ---
                val vaultCenter = Offset(w * 0.88f, h * 0.50f)

                // Concentric dashed security rings
                drawCircle(
                    color = tintColor.copy(alpha = 0.10f),
                    radius = 48.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                    ),
                )
                drawCircle(
                    color = tintColor.copy(alpha = 0.07f),
                    radius = 72.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    ),
                )
                drawCircle(
                    color = tintColor.copy(alpha = 0.05f),
                    radius = 96.dp.toPx(),
                    center = vaultCenter,
                    style = Stroke(width = 0.8.dp.toPx()),
                )

                // Geometric security lattice lines across bottom
                val latticePath = Path().apply {
                    moveTo(w * 0.40f, h)
                    cubicTo(w * 0.60f, h * 0.70f, w * 0.80f, h * 0.95f, w, h * 0.65f)
                }
                drawPath(
                    path = latticePath,
                    color = tintColor.copy(alpha = 0.12f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                )
                val latticePath2 = Path().apply {
                    moveTo(w * 0.50f, h)
                    cubicTo(w * 0.70f, h * 0.78f, w * 0.85f, h * 0.98f, w, h * 0.80f)
                }
                drawPath(
                    path = latticePath2,
                    color = tintColor.copy(alpha = 0.08f),
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
                )

                // Subtle security dots
                drawCircle(color = tintColor.copy(alpha = 0.18f), radius = 2.dp.toPx(), center = Offset(w * 0.72f, h * 0.28f))
                drawCircle(color = tintColor.copy(alpha = 0.12f), radius = 1.5.dp.toPx(), center = Offset(w * 0.60f, h * 0.80f))
            }

            PrismCardTheme.INCOME -> {
                // --- THẺ THU: HỌA TIẾT CỰC QUANG TĂNG TRƯỞNG & DẢI SÓNG THỊNH VƯỢNG ---
                val auroraPath1 = Path().apply {
                    moveTo(w * 0.35f, h * 0.95f)
                    cubicTo(w * 0.55f, h * 0.75f, w * 0.75f, h * 0.45f, w * 0.98f, h * 0.20f)
                }
                drawPath(
                    path = auroraPath1,
                    brush = Brush.linearGradient(
                        colors = listOf(tintColor.copy(alpha = 0.05f), tintColor.copy(alpha = 0.22f)),
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )

                val auroraPath2 = Path().apply {
                    moveTo(w * 0.45f, h)
                    cubicTo(w * 0.65f, h * 0.82f, w * 0.82f, h * 0.55f, w, h * 0.32f)
                }
                drawPath(
                    path = auroraPath2,
                    color = tintColor.copy(alpha = 0.10f),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                        cap = StrokeCap.Round,
                    ),
                )

                // Subtle ascending chevron markers in top-right
                for (i in 0..2) {
                    val cx = w * 0.86f + i * 14.dp.toPx()
                    val cy = h * 0.22f - i * 6.dp.toPx()
                    val chevron = Path().apply {
                        moveTo(cx - 5.dp.toPx(), cy + 4.dp.toPx())
                        lineTo(cx, cy)
                        lineTo(cx + 5.dp.toPx(), cy + 4.dp.toPx())
                    }
                    drawPath(
                        path = chevron,
                        color = tintColor.copy(alpha = 0.12f + i * 0.04f),
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
                    )
                }

                // Prosperity sparkle accents
                drawCircle(color = tintColor.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(w * 0.94f, h * 0.18f))
                drawCircle(color = tintColor.copy(alpha = 0.15f), radius = 1.5.dp.toPx(), center = Offset(w * 0.68f, h * 0.32f))
            }

            PrismCardTheme.EXPENSE -> {
                // --- THẺ CHI: HỌA TIẾT CUNG ĐO NGÂN SÁCH & QUỸ ĐẠO CHUẨN XÁC ---
                val radarCenter = Offset(w * 0.88f, h * 0.48f)

                // Radar budget arc with graduation marks
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            FinluxPalette.Transparent,
                            tintColor.copy(alpha = 0.08f),
                            tintColor.copy(alpha = 0.22f),
                        ),
                        center = radarCenter,
                    ),
                    startAngle = 135f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(radarCenter.x - 56.dp.toPx(), radarCenter.y - 56.dp.toPx()),
                    size = Size(112.dp.toPx(), 112.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )

                drawArc(
                    color = tintColor.copy(alpha = 0.09f),
                    startAngle = 110f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(radarCenter.x - 76.dp.toPx(), radarCenter.y - 76.dp.toPx()),
                    size = Size(152.dp.toPx(), 152.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
                    ),
                )

                // Precision calibration ticks
                for (deg in listOf(150, 180, 210, 240, 270)) {
                    val rad = Math.toRadians(deg.toDouble())
                    val r1 = 70.dp.toPx()
                    val r2 = 75.dp.toPx()
                    drawLine(
                        color = tintColor.copy(alpha = 0.15f),
                        start = Offset(radarCenter.x + (r1 * Math.cos(rad)).toFloat(), radarCenter.y + (r1 * Math.sin(rad)).toFloat()),
                        end = Offset(radarCenter.x + (r2 * Math.cos(rad)).toFloat(), radarCenter.y + (r2 * Math.sin(rad)).toFloat()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // Smooth expenditure guideline curve
                val expCurve = Path().apply {
                    moveTo(w * 0.42f, h * 0.38f)
                    cubicTo(w * 0.62f, h * 0.65f, w * 0.74f, h * 0.88f, w * 0.95f, h * 0.82f)
                }
                drawPath(
                    path = expCurve,
                    color = tintColor.copy(alpha = 0.10f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            PrismCardTheme.CASH_FLOW -> {
                // --- THẺ DÒNG TIỀN: HỌA TIẾT SÓNG ĐIỀU HÒA ĐÔI & MA TRẬN VECTOR ---
                val gridX = w * 0.62f
                val gridY = h * 0.18f
                val spacing = 13.dp.toPx()
                for (col in 0..3) {
                    for (row in 0..2) {
                        val alpha = (0.05f + col * 0.03f + row * 0.02f).coerceAtMost(0.18f)
                        drawCircle(
                            color = tintColor.copy(alpha = alpha),
                            radius = 1.3.dp.toPx(),
                            center = Offset(gridX + col * spacing, gridY + row * spacing),
                        )
                    }
                }

                // Harmonic Wave 1 (Upper Inflow Crest)
                val harmonicWave1 = Path().apply {
                    moveTo(w * 0.38f, h * 0.68f)
                    cubicTo(w * 0.58f, h * 0.32f, w * 0.75f, h * 0.78f, w * 0.98f, h * 0.40f)
                }
                drawPath(
                    path = harmonicWave1,
                    brush = Brush.linearGradient(
                        colors = listOf(tintColor.copy(alpha = 0.06f), tintColor.copy(alpha = 0.20f)),
                    ),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
                )

                // Harmonic Wave 2 (Lower Counter Valley)
                val harmonicWave2 = Path().apply {
                    moveTo(w * 0.44f, h * 0.48f)
                    cubicTo(w * 0.64f, h * 0.82f, w * 0.80f, h * 0.42f, w, h * 0.74f)
                }
                drawPath(
                    path = harmonicWave2,
                    color = tintColor.copy(alpha = 0.11f),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                        cap = StrokeCap.Round,
                    ),
                )

                // Crossing equilibrium node
                drawCircle(color = tintColor.copy(alpha = 0.22f), radius = 3.dp.toPx(), center = Offset(w * 0.70f, h * 0.56f))
                drawCircle(color = tintColor.copy(alpha = 0.08f), radius = 7.dp.toPx(), center = Offset(w * 0.70f, h * 0.56f))
            }
        }
    }
}
