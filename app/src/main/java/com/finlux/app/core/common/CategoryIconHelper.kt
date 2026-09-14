package com.finlux.app.core.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.finlux.app.core.designsystem.categoryIcon
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.FinanceTransaction
import com.finlux.app.domain.model.SystemCategories
import com.finlux.app.domain.model.TransactionType

object CategoryIconHelper {
    /**
     * Resolves the appropriate icon for a transaction based on its type and category.
     * Guaranteed Single Source of Truth for system category IDs (using SystemCategories constants).
     */
    fun resolveTransactionIcon(transaction: FinanceTransaction, category: Category?): ImageVector = when {
        transaction.type == TransactionType.TRANSFER_IN || transaction.type == TransactionType.TRANSFER_OUT ->
            Icons.Default.SwapHoriz
        category != null -> categoryIcon(category.icon)
        transaction.type == TransactionType.INCOME -> Icons.Default.Payments
        else -> when (category?.id) {
            SystemCategories.FOOD -> Icons.Default.Restaurant
            SystemCategories.TRANSPORT -> Icons.Default.DirectionsCar
            SystemCategories.SHOPPING -> Icons.Default.ShoppingBag
            SystemCategories.BILLS -> Icons.Default.Receipt
            SystemCategories.HOME -> Icons.Default.Home
            SystemCategories.HEALTH -> Icons.Default.LocalHospital
            SystemCategories.SAVINGS -> Icons.Default.Savings
            else -> Icons.Default.AccountBalanceWallet
        }
    }
}
