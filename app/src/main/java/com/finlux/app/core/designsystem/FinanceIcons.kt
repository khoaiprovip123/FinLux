package com.finlux.app.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import com.finlux.app.domain.model.WalletType

data class FinanceIconOption(val key: String, val label: String, val icon: ImageVector)

val FinanceCategoryIcons = listOf(
    FinanceIconOption("restaurant", "Ăn uống", Icons.Default.Restaurant),
    FinanceIconOption("local_cafe", "Cà phê", Icons.Default.LocalCafe),
    FinanceIconOption("directions_car", "Di chuyển", Icons.Default.DirectionsCar),
    FinanceIconOption("shopping_bag", "Mua sắm", Icons.Default.ShoppingBag),
    FinanceIconOption("receipt_long", "Hóa đơn", Icons.Default.ReceiptLong),
    FinanceIconOption("home", "Nhà ở", Icons.Default.Home),
    FinanceIconOption("health", "Sức khỏe", Icons.Default.HealthAndSafety),
    FinanceIconOption("school", "Học tập", Icons.Default.School),
    FinanceIconOption("flight", "Du lịch", Icons.Default.Flight),
    FinanceIconOption("games", "Giải trí", Icons.Default.SportsEsports),
    FinanceIconOption("pets", "Thú cưng", Icons.Default.Pets),
    FinanceIconOption("favorite", "Gia đình", Icons.Default.Favorite),
    FinanceIconOption("payments", "Thu nhập", Icons.Default.Payments),
    FinanceIconOption("savings", "Tiết kiệm", Icons.Default.Savings),
    FinanceIconOption("workspace_premium", "Thưởng", Icons.Default.WorkspacePremium),
    FinanceIconOption("work", "Công việc", Icons.Default.Work),
    FinanceIconOption("account_balance", "Ngân hàng", Icons.Default.AccountBalance),
    FinanceIconOption("show_chart", "Đầu tư", Icons.Default.ShowChart),
    FinanceIconOption("celebration", "Quà tặng", Icons.Default.Celebration),
    FinanceIconOption("auto_awesome", "Khác", Icons.Default.AutoAwesome),
)

fun categoryIcon(key: String): ImageVector =
    FinanceCategoryIcons.firstOrNull { it.key == key }?.icon ?: Icons.Default.AutoAwesome

fun walletIcon(type: WalletType): ImageVector = when (type) {
    WalletType.CASH -> Icons.Default.Payments
    WalletType.BANK -> Icons.Default.AccountBalance
    WalletType.EWALLET -> Icons.Default.Wallet
    WalletType.CARD -> Icons.Default.CreditCard
    WalletType.INVESTMENT -> Icons.Default.ShowChart
    WalletType.OTHER -> Icons.Default.AccountBalanceWallet
}

fun colorFromHex(value: String, fallback: Color = FinluxBlue): Color = runCatching {
    Color(value.toColorInt())
}.getOrDefault(fallback)

val FinanceAccentHexes = listOf(
    "#3478F6", "#7758F6", "#47C8FF", "#20B982", "#F05B68", "#F2A63B", "#EC4899", "#14B8A6",
)
