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
internal fun PrismWallet3DIllustration(modifier: Modifier = Modifier) {
    val tokens = LocalFinluxTokens.current
    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Radial Aura Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FinluxPalette.CFF38BDF8.copy(alpha = 0.35f),
                        FinluxPalette.CFF818CF8.copy(alpha = 0.18f),
                        FinluxPalette.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * 0.70f,
                ),
            )
        }

        // 2. Back Card: Cyber Blue Hologram Card
        Surface(
            modifier = Modifier
                .size(width = 68.dp, height = 44.dp)
                .graphicsLayer {
                    rotationZ = -13f
                    translationX = -6f
                    translationY = -12f
                }
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(10.dp),
                    ambientColor = FinluxPalette.CFF0284C7.copy(alpha = 0.4f),
                    spotColor = FinluxPalette.CFF0284C7.copy(alpha = 0.6f),
                ),
            shape = RoundedCornerShape(10.dp),
            color = FinluxPalette.Transparent,
            border = BorderStroke(1.dp, FinluxPalette.White.copy(alpha = 0.65f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FinluxPalette.CFF00C6FF,
                                FinluxPalette.CFF0072FF,
                                FinluxPalette.CFF4F46E5,
                            ),
                        ),
                    )
                    .padding(5.dp),
            ) {
                // Micro EMV Chip
                Surface(
                    shape = RoundedCornerShape(2.5.dp),
                    color = FinluxPalette.CFFFDE047,
                    modifier = Modifier.size(width = 11.dp, height = 8.dp),
                ) {}

                // Contactless Signal Waves
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                ) {
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 5.dp).background(FinluxPalette.White.copy(alpha = 0.7f), CircleShape))
                    Box(modifier = Modifier.size(width = 1.5.dp, height = 8.dp).background(FinluxPalette.White.copy(alpha = 0.7f), CircleShape))
                }
            }
        }

        // 3. Front Card: Frosted Platinum Liquid Glass Card
        Surface(
            modifier = Modifier
                .size(width = 82.dp, height = 54.dp)
                .graphicsLayer {
                    rotationZ = 6f
                    translationX = 4f
                    translationY = 6f
                }
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = FinluxPalette.CFF4338CA.copy(alpha = 0.35f),
                    spotColor = FinluxPalette.CFF4338CA.copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(12.dp),
            color = FinluxPalette.Transparent,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        FinluxPalette.White.copy(alpha = 0.95f),
                        FinluxPalette.White.copy(alpha = 0.35f),
                    ),
                ),
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                FinluxPalette.White.copy(alpha = 0.40f),
                                FinluxPalette.White.copy(alpha = 0.15f),
                            ),
                        ),
                    )
                    .padding(6.dp),
            ) {
                // Gold Chip
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = FinluxPalette.CFFF59E0B,
                    border = BorderStroke(0.5.dp, FinluxPalette.CFFFEF08A),
                    modifier = Modifier
                        .size(width = 14.dp, height = 10.dp)
                        .align(Alignment.TopStart),
                ) {}

                // Dual VIP Intersecting Circles
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy((-5).dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(FinluxPalette.CFFEC4899.copy(alpha = 0.80f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(FinluxPalette.CFFFBBF24.copy(alpha = 0.80f), CircleShape),
                    )
                }

                // Embossed card numbers placeholder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 2.dp)
                        .size(width = 22.dp, height = 3.dp)
                        .background(FinluxPalette.White.copy(alpha = 0.75f), CircleShape),
                )
            }
        }

        // 4. Floating 3D Gold Coin with ₫ symbol
        Surface(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 10f
                    translationY = 10f
                }
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = FinluxPalette.CFFF59E0B.copy(alpha = 0.6f),
                    spotColor = FinluxPalette.CFFF59E0B.copy(alpha = 0.8f),
                ),
            shape = CircleShape,
            color = FinluxPalette.Transparent,
            border = BorderStroke(1.5.dp, FinluxPalette.White.copy(alpha = 0.95f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FinluxPalette.CFFFFFBEB,
                                FinluxPalette.CFFFBBF24,
                                FinluxPalette.CFFD97706,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "₫",
                    color = FinluxPalette.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                )
            }
        }

        // 5. Sparkle Accent (Top Left)
        Canvas(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopStart),
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val rayLength = size.width * 0.40f
            drawLine(
                color = tokens.onHero.copy(alpha = 0.58f),
                start = Offset(center.x, center.y - rayLength),
                end = Offset(center.x, center.y + rayLength),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = tokens.onHero.copy(alpha = 0.58f),
                start = Offset(center.x - rayLength, center.y),
                end = Offset(center.x + rayLength, center.y),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = tokens.onHero.copy(alpha = 0.72f),
                radius = 1.2.dp.toPx(),
                center = center,
            )
        }
    }
}
