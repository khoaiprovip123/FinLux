package com.finlux.app.data.remote.firebase

/** Fields required by Firestore Rules for an atomic wallet balance + ledger mutation. */
internal fun walletLedgerUpdate(balance: Long, transactionId: String): Map<String, Any> {
    require(transactionId.isNotBlank()) { "Transaction ID must not be blank" }
    return mapOf(
        "balance" to balance,
        "lastTransactionId" to transactionId,
    )
}
