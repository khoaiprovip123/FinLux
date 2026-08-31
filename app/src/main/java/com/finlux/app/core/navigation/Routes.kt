package com.finlux.app.core.navigation

sealed class Route(val value: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Register : Route("register")
    data object ForgotPassword : Route("forgot-password")
    data object Home : Route("home")
    data object Transactions : Route("transactions")
    data object Reports : Route("reports")
    data object Budget : Route("budget")
    data object Settings : Route("settings")
    data object Categories : Route("categories")
    data object Wallets : Route("wallets")
    data object Income : Route("income")
    data object Expense : Route("expense")
    data object Notifications : Route("notifications")
    data object Reminders : Route("reminders")
    data object Goals : Route("goals")
    data object Debt : Route("debt")
    data object Deals : Route("deals")
}
