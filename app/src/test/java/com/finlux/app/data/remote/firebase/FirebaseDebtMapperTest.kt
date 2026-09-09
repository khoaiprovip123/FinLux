package com.finlux.app.data.remote.firebase

import com.finlux.app.domain.model.DebtAccount
import com.finlux.app.domain.model.DebtType
import com.finlux.app.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FirebaseDebtMapperTest {

    @Test
    fun `toDebtMap sanitizes PERSONAL_LOAN with legacy dueDate 1 to null`() {
        val legacyDebt = DebtAccount(
            name = "Đen",
            type = DebtType.PERSONAL_LOAN,
            totalAmount = Money(1_000_000L),
            remainingBalance = Money(1_000_000L),
            interestRateApr = 0.0,
            minimumPayment = Money(30_000L),
            dueDate = 1, // Legacy rác
        )

        val map = legacyDebt.toDebtMap()
        assertNull(map["dueDate"])
    }

    @Test
    fun `toDebtMap retains valid dueDate for recurring CREDIT_CARD`() {
        val creditCard = DebtAccount(
            name = "Thẻ HSBC",
            type = DebtType.CREDIT_CARD,
            totalAmount = Money(20_000_000L),
            remainingBalance = Money(10_000_000L),
            interestRateApr = 24.0,
            minimumPayment = Money(1_000_000L),
            dueDate = 15,
        )

        val map = creditCard.toDebtMap()
        assertEquals(15, map["dueDate"])
    }
}
