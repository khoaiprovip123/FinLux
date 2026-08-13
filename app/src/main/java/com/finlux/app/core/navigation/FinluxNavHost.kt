package com.finlux.app.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.core.designsystem.FinluxStyleBackdrop
import com.finlux.app.presentation.auth.AuthMode
import com.finlux.app.presentation.auth.AuthScreen
import com.finlux.app.presentation.auth.SplashScreen
import com.finlux.app.presentation.budget.BudgetScreen
import com.finlux.app.presentation.category.CategoriesScreen
import com.finlux.app.presentation.home.HomeScreen
import com.finlux.app.presentation.expense.ExpenseScreen
import com.finlux.app.presentation.income.IncomeScreen
import com.finlux.app.presentation.goal.GoalsScreen
import com.finlux.app.presentation.notifications.NotificationsScreen
import com.finlux.app.presentation.reminders.RemindersScreen
import com.finlux.app.presentation.reports.ReportsScreen
import com.finlux.app.presentation.settings.SettingsScreen
import com.finlux.app.presentation.transaction.AddTransactionSheet
import com.finlux.app.presentation.transaction.TransactionsScreen
import com.finlux.app.presentation.wallet.WalletsScreen
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.presentation.components.QuickAddSheet
import com.finlux.app.presentation.receipt.ReceiptCaptureScreen
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FinluxNavHost(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    navController: NavHostController = rememberNavController(),
    destinationFlow: MutableStateFlow<String?>? = null,
) {
    var showAddTransaction by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var walletTransferRequest by remember { mutableStateOf(0) }
    var initialTransactionType by remember { mutableStateOf<TransactionType?>(null) }
    var pendingReceiptUri by remember { mutableStateOf<String?>(null) }
    var showReceiptCapture by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val destination = destinationFlow?.collectAsState()?.value
    LaunchedEffect(destination) {
        if (!destination.isNullOrBlank()) {
            val targetRoute = when (destination) {
                "notifications" -> Route.Notifications.value
                else -> destination
            }
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    launchSingleTop = true
                }
            }
            destinationFlow.value = null
        }
    }

    val navigateMain: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Route.Home.value) { saveState = true }
        }
    }
    Box(Modifier.fillMaxSize()) {
        FinluxStyleBackdrop(Modifier.fillMaxSize())
        NavHost(
            navController = navController,
            startDestination = Route.Splash.value,
            enterTransition = {
                val from = MainSwipeRoutes.indexOf(initialState.destination.route)
                val to = MainSwipeRoutes.indexOf(targetState.destination.route)
                if (from >= 0 && to >= 0) {
                    slideInHorizontally(
                        spring(dampingRatio = 0.86f, stiffness = 390f),
                    ) { width -> if (to > from) width else -width } +
                        fadeIn(tween(210)) + scaleIn(tween(250), initialScale = 0.985f)
                } else fadeIn(tween(180))
            },
            exitTransition = {
                val from = MainSwipeRoutes.indexOf(initialState.destination.route)
                val to = MainSwipeRoutes.indexOf(targetState.destination.route)
                if (from >= 0 && to >= 0) {
                    slideOutHorizontally(
                        spring(dampingRatio = 0.90f, stiffness = 430f),
                    ) { width -> if (to > from) -width else width } +
                        fadeOut(tween(180)) + scaleOut(tween(210), targetScale = 0.985f)
                } else fadeOut(tween(140))
            },
        ) {
            composable(Route.Splash.value) {
                SplashScreen(
                    onAuthenticated = { navController.replaceGraphStart(Route.Home.value) },
                    onGuest = { navController.replaceGraphStart(Route.Login.value) },
                )
            }
            composable(Route.Login.value) {
                AuthScreen(
                    mode = AuthMode.LOGIN,
                    onCompleted = { navController.replaceGraphStart(Route.Home.value) },
                    onNavigate = { mode -> navController.navigate(mode.route) },
                )
            }
            composable(Route.Register.value) {
                AuthScreen(AuthMode.REGISTER, { navController.replaceGraphStart(Route.Home.value) }, { navController.navigate(it.route) })
            }
            composable(Route.ForgotPassword.value) {
                AuthScreen(AuthMode.FORGOT, { navController.popBackStack() }, { navController.navigate(it.route) })
            }
            composable(Route.Home.value) {
                HomeScreen(
                    onNavigate = navigateMain,
                    onAdd = { showQuickAdd = true },
                    onNotifications = { navController.navigate(Route.Notifications.value) },
                )
            }
            composable(Route.Transactions.value) { TransactionsScreen(onNavigate = navigateMain, onBack = navController::popBackStack) }
            composable(Route.Reports.value) {
                ReportsScreen(onNavigate = navigateMain, onAdd = { showQuickAdd = true }, onBack = navController::popBackStack)
            }
            composable(Route.Budget.value) {
                BudgetScreen(onNavigate = navigateMain, onAdd = { showQuickAdd = true }, onBack = navController::popBackStack)
            }
            composable(Route.Settings.value) {
                SettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    uiPreferences = uiPreferences,
                    onUiPreferencesChanged = onUiPreferencesChanged,
                    onNavigate = { route ->
                        if (route in MainSwipeRoutes) navigateMain(route)
                        else navController.navigate(route)
                    },
                    onAdd = { showQuickAdd = true },
                    onSignedOut = { navController.replaceGraphStart(Route.Login.value) },
                )
            }
            composable(Route.Categories.value) { CategoriesScreen(onBack = navController::popBackStack) }
            composable(Route.Wallets.value) {
                WalletsScreen(
                    onNavigate = navigateMain,
                    onAdd = { showQuickAdd = true },
                    onBack = navController::popBackStack,
                    transferRequestKey = walletTransferRequest,
                )
            }
            composable(Route.Income.value) {
                IncomeScreen(
                    onBack = navController::popBackStack,
                    onNavigate = navigateMain,
                    onAddIncome = {
                        initialTransactionType = TransactionType.INCOME
                        showAddTransaction = true
                    },
                )
            }
            composable(Route.Expense.value) {
                ExpenseScreen(
                    onBack = navController::popBackStack,
                    onNavigate = navigateMain,
                    onAddExpense = {
                        initialTransactionType = TransactionType.EXPENSE
                        showAddTransaction = true
                    },
                )
            }
            composable(Route.Notifications.value) { NotificationsScreen(onBack = navController::popBackStack) }
            composable(Route.Reminders.value) { RemindersScreen(onBack = navController::popBackStack) }
            composable(Route.Goals.value) { GoalsScreen(onBack = navController::popBackStack) }
        }
        if (showAddTransaction) {
            AddTransactionSheet(
                initialType = initialTransactionType,
                initialReceiptUri = pendingReceiptUri,
                onDismiss = {
                    showAddTransaction = false
                    initialTransactionType = null
                    pendingReceiptUri = null
                },
            )
        }
        if (showQuickAdd) {
            QuickAddSheet(
                onDismiss = { showQuickAdd = false },
                onIncome = {
                    showQuickAdd = false
                    initialTransactionType = TransactionType.INCOME
                    showAddTransaction = true
                },
                onExpense = {
                    showQuickAdd = false
                    initialTransactionType = TransactionType.EXPENSE
                    showAddTransaction = true
                },
                onTransfer = {
                    showQuickAdd = false
                    walletTransferRequest += 1
                    navigateMain(Route.Wallets.value)
                },
                onReceipt = {
                    showQuickAdd = false
                    showReceiptCapture = true
                },
                onGoal = {
                    showQuickAdd = false
                    navController.navigate(Route.Goals.value)
                },
            )
        }
        if (showReceiptCapture) {
            ReceiptCaptureScreen(
                onDismiss = { showReceiptCapture = false },
                onCaptured = { uri ->
                    showReceiptCapture = false
                    pendingReceiptUri = uri
                    initialTransactionType = TransactionType.EXPENSE
                    showAddTransaction = true
                },
            )
        }
    }
}

@Composable
private fun BoxScope.SwipeEdgeGlow(offset: Float) {
    if (kotlin.math.abs(offset) < 1f) return
    val revealFromLeft = offset > 0f
    val strength = (kotlin.math.abs(offset) / 80f).coerceIn(0f, 1f)
    val colors = if (revealFromLeft) {
        listOf(Color(0xFF47C8FF).copy(alpha = .34f * strength), Color.Transparent)
    } else {
        listOf(Color.Transparent, Color(0xFF7758F6).copy(alpha = .34f * strength))
    }
    Box(
        Modifier.align(if (revealFromLeft) Alignment.CenterStart else Alignment.CenterEnd)
            .fillMaxHeight()
            .width(54.dp)
            .background(Brush.horizontalGradient(colors)),
    )
}

private data class SwipeGesture(
    val horizontalTravel: Float,
    val verticalTravel: Float,
    val elapsedMillis: Long,
)

private val MainSwipeRoutes = listOf(
    Route.Home.value,
    Route.Wallets.value,
    Route.Reports.value,
    Route.Settings.value,
)

internal fun mainRouteAfterSwipe(currentRoute: String?, horizontalTravel: Float, threshold: Float): String? {
    return mainRouteAfterSwipe(currentRoute, horizontalTravel, 0f, threshold)
}

internal fun mainRouteAfterSwipe(
    currentRoute: String?,
    horizontalTravel: Float,
    verticalTravel: Float,
    threshold: Float,
    elapsedMillis: Long = Long.MAX_VALUE,
): String? {
    val currentIndex = MainSwipeRoutes.indexOf(currentRoute)
    val horizontalDistance = kotlin.math.abs(horizontalTravel)
    val verticalDistance = kotlin.math.abs(verticalTravel)
    val crossedDistance = horizontalDistance >= threshold
    val quickFlick = elapsedMillis <= 360L && horizontalDistance >= threshold * 0.48f
    if (currentIndex < 0 || (!crossedDistance && !quickFlick) || horizontalDistance < verticalDistance * 1.20f) return null
    val targetIndex = (currentIndex + if (horizontalTravel < 0f) 1 else -1).coerceIn(MainSwipeRoutes.indices)
    return MainSwipeRoutes.getOrNull(targetIndex)?.takeIf { targetIndex != currentIndex }
}

private fun NavHostController.replaceGraphStart(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private val AuthMode.route: String
    get() = when (this) {
        AuthMode.LOGIN -> Route.Login.value
        AuthMode.REGISTER -> Route.Register.value
        AuthMode.FORGOT -> Route.ForgotPassword.value
    }
