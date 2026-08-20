package com.finlux.app.domain.model

import java.time.Instant
import java.time.YearMonth

enum class TransactionType { INCOME, EXPENSE, TRANSFER_OUT, TRANSFER_IN }
enum class CategoryType { INCOME, EXPENSE }
enum class WalletType { CASH, BANK, EWALLET, CARD, INVESTMENT, OTHER }
enum class ThemePreference { LIGHT, DARK, SYSTEM }
enum class AppUiStyle { CLASSIC_LIQUID, MODERN_LUXURY, PRISM }
enum class ReminderRecurrence { DAILY, WEEKLY, MONTHLY }
enum class GlassIntensity { SOFT, BALANCED, VIVID }
enum class CardDensity { COMFORTABLE, COMPACT }
enum class VisualStyle { MODERN_DARK, GLASSMORPHISM, DYNAMIC_GRADIENT }

/**
 * A VND amount is stored as [Long] because VND has no fractional minor unit and business rule
 * BR-05 permits up to 15 digits. Using floating point here would corrupt wallet balances.
 */
@JvmInline
value class Money(val value: Long)

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
)

data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val balance: Money,
    val colorHex: String,
    val isDefault: Boolean,
    val createdAt: Instant,
)

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean,
    val createdAt: Instant,
)

data class FinanceTransaction(
    val id: String = "",
    val type: TransactionType,
    val amount: Money,
    val categoryId: String?,
    val walletId: String,
    val relatedWalletId: String? = null,
    val note: String = "",
    val receiptImageUrl: String? = null,
    val date: Instant,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class Budget(
    val id: String,
    val categoryId: String,
    val month: YearMonth,
    val limitAmount: Money,
    val spentAmount: Money,
    val notified80: Boolean,
    val notified100: Boolean,
)

data class Reminder(
    val id: String,
    val title: String,
    val amount: Money,
    val categoryId: String,
    val walletId: String,
    val recurrence: ReminderRecurrence,
    val startDate: Instant,
    val enabled: Boolean,
    val nextTriggerDate: Instant,
)

/** A user-defined saving target. Goal balances are independent from wallet balances. */
data class FinancialGoal(
    val id: String = "",
    val name: String,
    val targetAmount: Money,
    val savedAmount: Money = Money(0),
    val deadline: Instant,
    val category: String,
    val monthlyContribution: Money,
    val imageUri: String? = null,
    val createdAt: Instant = Instant.now(),
)

/** Local-only visual preferences. Financial data remains unaffected. */
data class UiPreferences(
    val visualStyle: VisualStyle = VisualStyle.DYNAMIC_GRADIENT,
    val glassIntensity: GlassIntensity = GlassIntensity.BALANCED,
    val cardDensity: CardDensity = CardDensity.COMFORTABLE,
    val animationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
)

data class DashboardSummary(
    val income: Money = Money(0),
    val expense: Money = Money(0),
    val net: Long = income.value - expense.value,
)
