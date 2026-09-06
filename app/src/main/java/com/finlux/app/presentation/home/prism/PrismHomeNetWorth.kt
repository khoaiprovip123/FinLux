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
internal fun PrismHeroNetWorthCard(
    netWorth: Long,
    grossAssets: Long,
    totalDebt: Long,
    showBalance: Boolean,
    onToggleShowBalance: () -> Unit,
    onDebtsClick: () -> Unit = {},
    onWalletsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val shape = RoundedCornerShape(24.dp)
    val pageCount = if (totalDebt > 0L) 2 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = FinluxPalette.CFF4C68FF.copy(alpha = 0.32f),
                spotColor = FinluxPalette.CFF865BF9.copy(alpha = 0.42f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        FinluxPalette.CFF3A5FFF,
                        FinluxPalette.CFF5E50F8,
                        FinluxPalette.CFF7C5AF9,
                        FinluxPalette.CFF9B6EFB,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .border(BorderStroke(1.dp, tokens.onHero.copy(alpha = 0.22f)), shape),
    ) {
        // ── Decorative Background Layer ──────────────────────────────
        Canvas(modifier = Modifier.matchParentSize()) {
            // Large top-right glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.onHero.copy(alpha = 0.18f), FinluxPalette.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.0f),
                    radius = size.width * 0.52f,
                ),
                center = Offset(size.width * 0.82f, 0f),
                radius = size.width * 0.52f,
            )

            // Bottom-left secondary orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FinluxPalette.CFF38BDF8.copy(alpha = 0.22f), FinluxPalette.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 1.1f),
                    radius = size.width * 0.45f,
                ),
                center = Offset(size.width * 0.15f, size.height * 1.1f),
                radius = size.width * 0.45f,
            )

            // Decorative large arc line (top-right)
            drawArc(
                color = tokens.onHero.copy(alpha = 0.08f),
                startAngle = 160f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(size.width * 0.45f, -size.width * 0.42f),
                size = Size(size.width * 0.85f, size.width * 0.85f),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Decorative smaller arc
            drawArc(
                color = tokens.onHero.copy(alpha = 0.12f),
                startAngle = 165f,
                sweepAngle = 80f,
                useCenter = false,
                topLeft = Offset(size.width * 0.52f, -size.width * 0.28f),
                size = Size(size.width * 0.62f, size.width * 0.62f),
                style = Stroke(width = 1.dp.toPx()),
            )

            // Dots grid pattern (bottom-right area)
            val dotRadius = 1.8.dp.toPx()
            val dotSpacing = 14.dp.toPx()
            val gridStartX = size.width * 0.60f
            val gridStartY = size.height * 0.55f
            for (row in 0..3) {
                for (col in 0..4) {
                    val cx = gridStartX + col * dotSpacing
                    val cy = gridStartY + row * dotSpacing
                    if (cx < size.width - 8.dp.toPx()) {
                        drawCircle(
                            color = tokens.onHero.copy(alpha = 0.18f),
                            radius = dotRadius,
                            center = Offset(cx, cy),
                        )
                    }
                }
            }
        }

        // ── Pager Content ────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 22.dp,
                        top = 22.dp,
                        end = 22.dp,
                        bottom = if (pageCount > 1) 28.dp else 22.dp,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (page == 0) {
                        // ── Page 0: Số dư hiện có (Tổng tiền các ví) ─────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleShowBalance,
                            ),
                        ) {
                            Text(
                                text = "Số dư hiện có",
                                style = FinluxTextStyles.Caption.copy(fontSize = 13.sp),
                                color = tokens.onHero.copy(alpha = 0.94f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = tokens.onHero.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Display Amount (Gross Assets)
                        Text(
                            text = if (showBalance) formatVndAmount(grossAssets) else "••••••••",
                            style = FinluxTextStyles.DisplayAmount.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = tokens.onHero,
                        )

                        Text(
                            text = "Tổng số dư từ tất cả các ví",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = tokens.onHeroMuted,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Breakdown pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Gross Assets Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.heroGlassSurface,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onWalletsClick() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Ví: " + if (showBalance) formatVndAmount(grossAssets) else "•••",
                                        style = FinluxTextStyles.MicroLabel.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                        ),
                                        color = tokens.onHero,
                                    )
                                }
                            }

                            // Total Debt Chip
                            if (totalDebt > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinluxPalette.CFFE11D48.copy(alpha = 0.35f),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDebtsClick() },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "Nợ: " + if (showBalance) formatVndAmount(totalDebt) else "•••",
                                            style = FinluxTextStyles.MicroLabel.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                            ),
                                            color = tokens.onHero,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ── Page 1: Tài sản ròng (Net Worth = Ví - Nợ) ───────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleShowBalance,
                            ),
                        ) {
                            Text(
                                text = "Tài sản ròng (Net Worth)",
                                style = FinluxTextStyles.Caption.copy(fontSize = 13.sp),
                                color = tokens.onHero.copy(alpha = 0.94f),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Ẩn/Hiện số dư",
                                tint = tokens.onHero.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Display Amount (Net Worth)
                        Text(
                            text = if (showBalance) formatVndAmount(netWorth) else "••••••••",
                            style = FinluxTextStyles.DisplayAmount.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = tokens.onHero,
                        )

                        Text(
                            text = "Tổng ví trừ tổng dư nợ",
                            style = FinluxTextStyles.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = tokens.onHeroMuted,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Breakdown pills
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.heroGlassSurface,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onWalletsClick() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "Ví: " + if (showBalance) formatVndAmount(grossAssets) else "•••",
                                        style = FinluxTextStyles.MicroLabel.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                        ),
                                        color = tokens.onHero,
                                    )
                                }
                            }

                            if (totalDebt > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinluxPalette.CFFE11D48.copy(alpha = 0.35f),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDebtsClick() },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "Nợ: " + if (showBalance) formatVndAmount(totalDebt) else "•••",
                                            style = FinluxTextStyles.MicroLabel.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                            ),
                                            color = tokens.onHero,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3D Glowing Wallet Graphic Composition
                PrismWallet3DIllustration(
                    modifier = Modifier
                        .size(105.dp)
                        .padding(start = 6.dp),
                )
            }
        }

        // ── Page Indicator Dots ──────────────────────────────────────
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isSelected) 14.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) tokens.onHero else tokens.onHero.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

/**
 * Visual 3D Spatial Holographic Cards & Golden Coin Graphic
 */
