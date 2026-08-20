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
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
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
import com.finlux.app.presentation.home.HomeScreen
import com.finlux.app.presentation.expense.ExpenseScreen
import com.finlux.app.presentation.income.IncomeScreen
import com.finlux.app.presentation.goal.GoalsScreen
import com.finlux.app.presentation.notifications.NotificationsScreen
import com.finlux.app.presentation.reminders.RemindersScreen
import com.finlux.app.presentation.reports.ReportsScreen
import com.finlux.app.presentation.settings.SettingsScreen
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(Route.Home.value) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
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
                    onSelectTransaction = { viewingTransaction = it },
                    onActionTransaction = { actionTransaction = it },
                    onEditTransaction = { editingTransaction = it },
                )
            }
            composable(Route.Transactions.value) {
                TransactionsScreen(
                    onNavigate = navigateMain,
                    onAdd = { showQuickAdd = true },
                    onBack = navController::popBackStack,
                    onEditTransaction = { editingTransaction = it },
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
                    onSelectTransaction = { viewingTransaction = it },
                    onActionTransaction = { actionTransaction = it },
                    onEditTransaction = { editingTransaction = it },
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
                    onEditTransaction = { editingTransaction = it },
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
                onDismiss = { viewingTransaction = null },
                onEdit = {
                    viewingTransaction = null
                    editingTransaction = it
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
                onDismiss = { actionTransaction = null },
                onViewDetails = {
                    actionTransaction = null
                    viewingTransaction = it
                },
                onEdit = {
                    actionTransaction = null
                    editingTransaction = it
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
