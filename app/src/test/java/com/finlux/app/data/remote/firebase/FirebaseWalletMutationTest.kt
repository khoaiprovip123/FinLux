package com.finlux.app.data.remote.firebase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirebaseWalletMutationTest {
    @Test
    fun `ledger balance update always carries its transaction id`() {
        assertEquals(
            mapOf("balance" to 750_000L, "lastTransactionId" to "tx_123"),
            walletLedgerUpdate(balance = 750_000L, transactionId = "tx_123"),
        )
    }

    @Test
    fun `ledger balance update rejects a blank transaction id`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            walletLedgerUpdate(balance = 750_000L, transactionId = " ")
        }
    }
}
