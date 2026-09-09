package com.finlux.app.domain.model

/**
 * Converts Firestore's double-entry transfer records into one logical item for presentation.
 * The outgoing side is preferred because it carries the source wallet as [FinanceTransaction.walletId].
 * An orphan incoming row is retained so partially-synced data never disappears from the UI.
 */
fun List<FinanceTransaction>.collapseInternalTransferPairs(): List<FinanceTransaction> {
    if (none { it.type == TransactionType.TRANSFER_IN }) return this

    val ids = asSequence().map(FinanceTransaction::id).toHashSet()
    return filter { transaction ->
        if (transaction.type != TransactionType.TRANSFER_IN) return@filter true
        val outgoingId = transaction.id
            .takeIf { it.endsWith(TRANSFER_IN_SUFFIX) }
            ?.removeSuffix(TRANSFER_IN_SUFFIX)
            ?.plus(TRANSFER_OUT_SUFFIX)
        outgoingId == null || outgoingId !in ids
    }
}

/** Payment cards are liabilities/instruments, so they must not inflate gross assets. */
fun List<Wallet>.assetWallets(): List<Wallet> = filter {
    it.status == ACTIVE_WALLET_STATUS && it.type != WalletType.CARD
}

fun List<Wallet>.totalAssetBalance(): Long = assetWallets().sumOf { it.balance.value }

/** Goal deposits are expenses and withdrawals are income under BR-14. */
fun List<FinanceTransaction>.netGoalContribution(
    isSavingsCategory: (String?) -> Boolean,
): Long {
    val deposits = filter { isSavingsCategory(it.categoryId) && it.type == TransactionType.EXPENSE }
        .sumOf { it.amount.value }
    val withdrawals = filter { isSavingsCategory(it.categoryId) && it.type == TransactionType.INCOME }
        .sumOf { it.amount.value }
    return (deposits - withdrawals).coerceAtLeast(0L)
}

fun List<FinanceTransaction>.netGoalContribution(categoryId: String = SAVINGS_CATEGORY_ID): Long =
    netGoalContribution { it == categoryId }

/** Kiểm tra xem giao dịch có phải là trả gốc nợ (chuyển dịch vốn, không phải chi tiêu sinh hoạt) hay không */
fun FinanceTransaction.isDebtPrincipalSettlement(): Boolean =
    categoryId == DEBT_PRINCIPAL_CATEGORY_ID

/** Kiểm tra xem giao dịch có phải là tiền lãi vay (chi phí tài chính thực sự) hay không */
fun FinanceTransaction.isDebtInterest(): Boolean =
    categoryId == DEBT_INTEREST_CATEGORY_ID

const val DEBT_PAYMENT_CATEGORY_ID = "debt_payment"
const val DEBT_INTEREST_CATEGORY_ID = "debt_interest"
const val DEBT_PRINCIPAL_CATEGORY_ID = "debt_principal"

private const val TRANSFER_IN_SUFFIX = "_in"
private const val TRANSFER_OUT_SUFFIX = "_out"
private const val ACTIVE_WALLET_STATUS = "active"
private const val SAVINGS_CATEGORY_ID = "savings"
