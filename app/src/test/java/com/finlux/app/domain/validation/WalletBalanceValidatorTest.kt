package com.finlux.app.domain.validation

import com.finlux.app.domain.model.Money
import com.finlux.app.domain.model.Wallet
import com.finlux.app.domain.model.WalletType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class WalletBalanceValidatorTest {

    private fun createWallet(
        id: String = "test_wallet",
        name: String = "Ví Tiền mặt",
        type: WalletType = WalletType.CASH,
        balance: Long = 0L,
    ) = Wallet(
        id = id,
        name = name,
        type = type,
        balance = Money(balance),
        colorHex = "#1F6FBF",
        isDefault = true,
        createdAt = Instant.now(),
    )

    @Test
    fun `validate rejects expense when standard cash wallet has 0 balance`() {
        val wallet = createWallet(name = "Ví Tiền mặt", type = WalletType.CASH, balance = 0L)

        val result = wallet.validateSufficientFunds(amount = 50_000L, isExpense = true)

        assertInstanceOf(WalletValidationResult.InsufficientFunds::class.java, result)
        val error = result as WalletValidationResult.InsufficientFunds
        assertEquals(0L, error.available)
        assertEquals(50_000L, error.required)
        assertEquals(50_000L, error.shortage)
        assertEquals("Ví Tiền mặt", error.walletName)
    }

    @Test
    fun `validate rejects expense when standard bank wallet has insufficient balance`() {
        val wallet = createWallet(name = "Ngân hàng Techcombank", type = WalletType.BANK, balance = 150_000L)

        val result = wallet.validateSufficientFunds(amount = 200_000L, isExpense = true)

        assertInstanceOf(WalletValidationResult.InsufficientFunds::class.java, result)
        val error = result as WalletValidationResult.InsufficientFunds
        assertEquals(150_000L, error.available)
        assertEquals(200_000L, error.required)
        assertEquals(50_000L, error.shortage)
        assertEquals("Ngân hàng Techcombank", error.walletName)
    }

    @Test
    fun `validate allows expense when standard wallet has exactly enough balance`() {
        val wallet = createWallet(name = "Ví MoMo", type = WalletType.EWALLET, balance = 100_000L)

        val result = wallet.validateSufficientFunds(amount = 100_000L, isExpense = true)

        assertEquals(WalletValidationResult.Valid, result)
    }

    @Test
    fun `validate always allows income transaction regardless of wallet balance`() {
        val wallet = createWallet(name = "Ví Tiền mặt", type = WalletType.CASH, balance = -50_000L)

        val result = wallet.validateSufficientFunds(amount = 2_000_000L, isExpense = false)

        assertEquals(WalletValidationResult.Valid, result)
    }

    @Test
    fun `validate allows credit card expense within credit limit`() {
        val creditCard = createWallet(name = "Thẻ HSBC", type = WalletType.CARD, balance = -5_000_000L)

        val result = creditCard.validateSufficientFunds(
            amount = 3_000_000L,
            isExpense = true,
            creditLimit = 15_000_000L,
        )

        assertEquals(WalletValidationResult.Valid, result)
    }

    @Test
    fun `validate rejects credit card expense when exceeding credit limit`() {
        val creditCard = createWallet(name = "Thẻ VPBank", type = WalletType.CARD, balance = -18_000_000L)

        val result = creditCard.validateSufficientFunds(
            amount = 5_000_000L,
            isExpense = true,
            creditLimit = 20_000_000L,
        )

        assertInstanceOf(WalletValidationResult.CreditLimitExceeded::class.java, result)
        val error = result as WalletValidationResult.CreditLimitExceeded
        assertEquals(20_000_000L, error.creditLimit)
        assertEquals(18_000_000L, error.currentDebt)
        assertEquals(23_000_000L, error.newDebt)
        assertEquals(3_000_000L, error.excessAmount)
    }

    @Test
    fun `validate rollback preview allows expense when refunded amount makes balance sufficient`() {
        // Ví hiện có 50.000đ, giao dịch cũ đang sửa là 100.000đ, giao dịch mới là 120.000đ
        // Số dư khả dụng thực tế sau hoàn trả = 50.000 + 100.000 = 150.000đ >= 120.000đ -> Hợp lệ!
        val wallet = createWallet(name = "Ví Tiền mặt", type = WalletType.CASH, balance = 50_000L)

        val result = wallet.validateSufficientFunds(
            amount = 120_000L,
            isExpense = true,
            rollbackAmount = 100_000L,
        )

        assertEquals(WalletValidationResult.Valid, result)
    }
}
