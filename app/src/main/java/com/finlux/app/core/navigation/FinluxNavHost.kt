package com.finlux.app.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finlux.app.domain.model.AppUiStyle
import com.finlux.app.domain.model.ThemePreference
import com.finlux.app.domain.model.UiPreferences
import com.finlux.app.presentation.auth.AuthMode
import com.finlux.app.presentation.auth.AuthScreen
import com.finlux.app.presentation.auth.SplashScreen
import com.finlux.app.presentation.budget.BudgetScreen
import com.finlux.app.presentation.category.CategoriesScreen
import com.finlux.app.presentation.components.MainBottomBar
import com.finlux.app.presentation.home.HomeScreen
import com.finlux.app.presentation.expense.ExpenseScreen
import com.finlux.app.presentation.income.IncomeScreen
import com.finlux.app.presentation.debt.DebtDashboardScreen
import com.finlux.app.presentation.goal.GoalsScreen
import com.finlux.app.presentation.notifications.NotificationsScreen
import com.finlux.app.presentation.reminders.RemindersScreen
import com.finlux.app.presentation.reports.ReportsScreen
import com.finlux.app.presentation.settings.SettingsScreen
import com.finlux.app.presentation.savingspin.settings.SavingSpinSettingsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finlux.app.presentation.transaction.TransactionDetailSheet
import com.finlux.app.presentation.transaction.TransactionActionDialog
import com.finlux.app.presentation.transaction.DeleteTransactionConfirmDialog
import com.finlux.app.presentation.transaction.TransactionsViewModel
import com.finlux.app.presentation.transaction.AddTransactionSheet
import com.finlux.app.presentation.transaction.TransactionsScreen
import com.finlux.app.presentation.wallet.WalletsScreen
import com.finlux.app.domain.model.TransactionType
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.presentation.components.QuickAddSheet
import com.finlux.app.presentation.receipt.ReceiptCaptureScreen
import com.finlux.app.presentation.updater.AppUpdateDialog
import com.finlux.app.presentation.updater.AppUpdateViewModel
import android.annotation.SuppressLint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FinluxNavHost(
    selectedTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit,
    selectedUiStyle: AppUiStyle = AppUiStyle.CLASSIC_LIQUID,
    onUiStyleSelected: (AppUiStyle) -> Unit = {},
    uiPreferences: UiPreferences,
    onUiPreferencesChanged: (UiPreferences) -> Unit,
    navController: NavHostController = rememberNavController(),
    destinationFlow: MutableStateFlow<String?>? = null,
    payNotificationIdFlow: MutableStateFlow<String?>? = null,
) {
    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
    val updateViewModel: AppUpdateViewModel = hiltViewModel()
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val allCategories = transactionsViewModel.categories.collectAsStateWithLifecycle().value
    val allWallets = transactionsViewModel.wallets.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdates(silent = true)
    }

    var showAddTransaction by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var viewingTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var actionTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var pendingDeleteTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showTransferMoney by remember { mutableStateOf(false) }
    var walletTransferRequest by remember { mutableStateOf(0) }
    var initialTransactionType by remember { mutableStateOf<TransactionType?>(null) }
    var pendingReceiptUri by remember { mutableStateOf<String?>(null) }
    var showReceiptCapture by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var swipeDragOffset by remember { mutableFloatStateOf(0f) }
    var isSwipeDragging by remember { mutableStateOf(false) }
    var snapResetSwipe by remember { mutableStateOf(false) }
    val renderedSwipeOffset by animateFloatAsState(
        targetValue = swipeDragOffset,
        animationSpec = if (isSwipeDragging || snapResetSwipe) snap() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "main-tab-swipe-offset",
    )

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
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(Route.Home.value) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val mainSwipeModifier = if (currentRoute in MainSwipeRoutes) {
        Modifier.pointerInput(currentRoute, uiPreferences.animationsEnabled) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = true, pass = PointerEventPass.Main)
                var horizontalTravel = 0f
                var verticalTravel = 0f
                var gestureLocked = false
                var lastUptimeMillis = down.uptimeMillis

                do {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.isConsumed) {
                        // Cử chỉ đang được xử lý bởi component con (như HorizontalPager, Slider, Chart...)
                        break
                    }
                    val delta = change.positionChange()
                    horizontalTravel += delta.x
                    verticalTravel += delta.y
                    lastUptimeMillis = change.uptimeMillis

                    if (!gestureLocked &&
                        abs(horizontalTravel) >= viewConfiguration.touchSlop &&
                        abs(horizontalTravel) >= abs(verticalTravel) * 1.50f
                    ) {
                        gestureLocked = true
                    }

                    if (gestureLocked) {
                        change.consume()
                        if (uiPreferences.animationsEnabled) {
                            val routeIndex = MainSwipeRoutes.indexOf(currentRoute)
                            val isBlockedAtEdge =
                                (routeIndex == 0 && horizontalTravel > 0f) ||
                                    (routeIndex == MainSwipeRoutes.lastIndex && horizontalTravel < 0f)
                            isSwipeDragging = true
                            swipeDragOffset = horizontalTravel * if (isBlockedAtEdge) 0.22f else 1f
                        }
                    }
                } while (change.pressed)

                if (gestureLocked) {
                    val target = mainRouteAfterSwipe(
                        currentRoute = currentRoute,
                        horizontalTravel = horizontalTravel,
                        verticalTravel = verticalTravel,
                        threshold = swipeThresholdPx,
                        elapsedMillis = lastUptimeMillis - down.uptimeMillis,
                    )
                    if (target != null) {
                        snapResetSwipe = true
                        swipeDragOffset = 0f
                        isSwipeDragging = false
                        navigateMain(target)
                        snapResetSwipe = false
                    } else {
                        isSwipeDragging = false
                        swipeDragOffset = 0f
                    }
                }
            }
        }
    } else Modifier
    val showRootBottomBar = currentRoute in MainSwipeRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showRootBottomBar && currentRoute != null,
                enter = fadeIn(tween(260)) + slideInVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetY = { it },
                ),
                exit = fadeOut(tween(140)) + slideOutVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    targetOffsetY = { it },
                ),
            ) {
                MainBottomBar(
                    selectedRoute = currentRoute ?: Route.Home.value,
                    onNavigate = navigateMain,
                    onAdd = { showQuickAdd = true },
                )
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .then(mainSwipeModifier),
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = renderedSwipeOffset },
            enterTransition = {
                val from = MainSwipeRoutes.indexOf(initialState.destination.route)
                val to = MainSwipeRoutes.indexOf(targetState.destination.route)
                if (from >= 0 && to >= 0) {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    ) { width -> if (to > from) width else -width }
                } else fadeIn(tween(180))
            },
            exitTransition = {
                val from = MainSwipeRoutes.indexOf(initialState.destination.route)
                val to = MainSwipeRoutes.indexOf(targetState.destination.route)
                if (from >= 0 && to >= 0) {
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    ) { width -> if (to > from) -width else width }
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
                    onSelectTransaction = { viewingTransaction = it },
                    onActionTransaction = { actionTransaction = it },
                    onEditTransaction = { if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) editingTransaction = it },
                )
            }
            composable(Route.Transactions.value) {
                TransactionsScreen(
                    onNavigate = navigateMain,
                    onAdd = { showQuickAdd = true },
                    onBack = navController::popBackStack,
                    onEditTransaction = { if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) editingTransaction = it },
                )
            }
            composable(Route.Reports.value) {
                ReportsScreen(onNavigate = navigateMain, onAdd = { showQuickAdd = true }, onBack = null)
            }
            composable(Route.Budget.value) {
                BudgetScreen(onNavigate = navigateMain, onAdd = { showQuickAdd = true }, onBack = navController::popBackStack)
            }
            composable(Route.Settings.value) {
                SettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    selectedUiStyle = selectedUiStyle,
                    onUiStyleSelected = onUiStyleSelected,
                    uiPreferences = uiPreferences,
                    onUiPreferencesChanged = onUiPreferencesChanged,
                    onNavigate = { route ->
                        if (route in MainSwipeRoutes) navigateMain(route)
                        else navController.navigate(route)
                    },
                    onAdd = { showQuickAdd = true },
                    onSignedOut = { navController.replaceGraphStart(Route.Login.value) },
                    onCheckUpdate = { updateViewModel.checkForUpdates(silent = false) },
                )
            }
            composable(Route.Categories.value) { CategoriesScreen(onBack = navController::popBackStack) }
            composable(Route.Wallets.value) {
                WalletsScreen(
                    onBack = navController::popBackStack,
                    onNavigate = { route ->
                        if (route in MainSwipeRoutes) navigateMain(route)
                        else navController.navigate(route)
                    },
                    onSelectTransaction = { viewingTransaction = it },
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
                    onSelectTransaction = { viewingTransaction = it },
                    onActionTransaction = { actionTransaction = it },
                    onEditTransaction = { if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) editingTransaction = it },
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
                    onSelectTransaction = { viewingTransaction = it },
                    onActionTransaction = { actionTransaction = it },
                    onEditTransaction = { if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) editingTransaction = it },
                )
            }
            composable(Route.Notifications.value) {
                NotificationsScreen(
                    onBack = navController::popBackStack,
                    onNavigate = navController::navigate,
                    payNotificationIdFlow = payNotificationIdFlow,
                )
            }
            composable(Route.Reminders.value) { RemindersScreen(onBack = navController::popBackStack) }
            composable(Route.Goals.value) { GoalsScreen(onBack = navController::popBackStack) }
            composable(Route.Debt.value) { DebtDashboardScreen(onBack = navController::popBackStack) }
            composable(Route.Deals.value) { com.finlux.app.presentation.deal.DealsScreen(onNavigateBack = navController::popBackStack) }
            composable(Route.SavingSpinSettings.value) {
                SavingSpinSettingsScreen(
                    onBack = navController::popBackStack,
                    onManageDestinations = { navController.navigate(Route.SavingSpinSettings.value) },
                )
            }
            composable(Route.SavingSpinReport.value) {
                com.finlux.app.presentation.savingspin.report.SavingSpinReportScreen(
                    onBack = navController::popBackStack,
                )
            }
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
        if (showTransferMoney) {
            com.finlux.app.presentation.wallet.TransferMoneyScreen(
                onDismiss = { showTransferMoney = false },
            )
        }
        if (editingTransaction != null) {
            AddTransactionSheet(
                initialTransaction = editingTransaction,
                onDismiss = {
                    editingTransaction = null
                },
            )
        }
        viewingTransaction?.let { tx ->
            TransactionDetailSheet(
                transaction = tx,
                category = allCategories[tx.categoryId],
                wallet = allWallets[tx.walletId],
                relatedWallet = allWallets[tx.relatedWalletId],
                onDismiss = { viewingTransaction = null },
                onEdit = {
                    viewingTransaction = null
                    if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) {
                        editingTransaction = it
                    }
                },
                onDelete = {
                    viewingTransaction = null
                    pendingDeleteTransaction = it
                },
            )
        }
        actionTransaction?.let { tx ->
            TransactionActionDialog(
                transaction = tx,
                category = allCategories[tx.categoryId],
                wallet = allWallets[tx.walletId],
                relatedWallet = allWallets[tx.relatedWalletId],
                onDismiss = { actionTransaction = null },
                onViewDetails = {
                    actionTransaction = null
                    viewingTransaction = it
                },
                onEdit = {
                    actionTransaction = null
                    if (it.type != TransactionType.TRANSFER_OUT && it.type != TransactionType.TRANSFER_IN) {
                        editingTransaction = it
                    }
                },
                onDelete = {
                    actionTransaction = null
                    pendingDeleteTransaction = it
                },
            )
        }
        pendingDeleteTransaction?.let { tx ->
            DeleteTransactionConfirmDialog(
                transaction = tx,
                relatedWallet = allWallets[tx.relatedWalletId],
                onDismiss = { pendingDeleteTransaction = null },
                onConfirm = {
                    transactionsViewModel.delete(it)
                    pendingDeleteTransaction = null
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
                    showTransferMoney = true
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
        AppUpdateDialog(
            uiState = updateUiState,
            onDownloadAndInstall = { updateViewModel.downloadAndInstall(it) },
            onInstallDownloaded = { updateViewModel.installDownloadedApk(it) },
            onDismiss = { updateViewModel.dismissUpdate() },
        )
    }
}

private val MainSwipeRoutes = listOf(
    Route.Home.value,
    Route.Transactions.value,
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
