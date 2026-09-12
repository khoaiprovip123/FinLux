package com.finlux.app.domain.model

/**
 * Single Source of Truth cho toàn bộ Category ID chuẩn của hệ thống FinLux.
 * Bắt buộc sử dụng các hằng số tại đây thay vì gõ literal string rải rác.
 */
object SystemCategories {
    // --- Chi tiêu (Expense) ---
    const val FOOD = "food"
    const val TRANSPORT = "transport"
    const val SHOPPING = "shopping"
    const val BILLS = "bills"
    const val HOME = "home"
    const val HEALTH = "health"
    const val TRAVEL = "travel"
    const val DEBT_PAYMENT = "debt_payment"
    const val SAVINGS = "savings"

    // --- Thu nhập (Income) ---
    const val SALARY = "salary"
    const val BONUS = "bonus"
    const val FREELANCE = "freelance"
    const val INTEREST = "interest"
    const val REFUND = "refund"
    const val INVESTMENT_INCOME = "investment-income"

    // --- Legacy / Backwards Compatibility ---
    const val LEGACY_DEBT_PRINCIPAL = "debt_principal"
    const val LEGACY_DEBT_INTEREST = "debt_interest"

    val ALL_SYSTEM_EXPENSE_IDS: Set<String> = setOf(
        FOOD,
        TRANSPORT,
        SHOPPING,
        BILLS,
        HOME,
        HEALTH,
        TRAVEL,
        DEBT_PAYMENT,
        SAVINGS,
    )

    val ALL_SYSTEM_INCOME_IDS: Set<String> = setOf(
        SALARY,
        BONUS,
        FREELANCE,
        INTEREST,
        REFUND,
        INVESTMENT_INCOME,
    )

    val ALL_SYSTEM_IDS: Set<String> = ALL_SYSTEM_EXPENSE_IDS + ALL_SYSTEM_INCOME_IDS
}
