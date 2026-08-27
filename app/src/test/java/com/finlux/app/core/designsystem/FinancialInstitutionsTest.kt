package com.finlux.app.core.designsystem

import com.finlux.app.domain.model.WalletType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FinancialInstitutionsTest {

    @Test
    fun `institutions catalog contains major Vietnamese banks and e-wallets`() {
        val list = VietnameseFinancialInstitutions
        assertTrue(list.size >= 75, "Expected at least 75 financial institutions, found ${list.size}")
        assertEquals(65, VietQrFinancialInstitutions.size)

        assertEquals(list.size, list.map { it.id }.distinct().size, "Institution ids must be unique")
        assertEquals(
            list.size,
            list.map { it.code.uppercase() }.distinct().size,
            "Institution codes must be unique",
        )
        assertTrue(
            list.filter { it.category == InstitutionCategory.BANK || it.category == InstitutionCategory.EWALLET }
                .all { it.iconRes != null },
            "Every bank and e-wallet must have a bundled logo resource",
        )

        val vcb = list.firstOrNull { it.code == "VCB" }
        assertNotNull(vcb)
        assertEquals("Vietcombank", vcb?.shortName)
        assertEquals(WalletType.BANK, vcb?.type)

        val momo = list.firstOrNull { it.code == "MOMO" }
        assertNotNull(momo)
        assertEquals("MoMo", momo?.shortName)
        assertEquals(WalletType.EWALLET, momo?.type)

        val mb = list.firstOrNull { it.code == "MB" }
        assertNotNull(mb)
        assertEquals("MBBank", mb?.shortName)

        assertNotNull(list.firstOrNull { it.shortName == "Agribank" })
        assertNotNull(list.firstOrNull { it.shortName == "VNPT Money" })
        assertNotNull(list.firstOrNull { it.shortName == "Payoo" })
        assertNotNull(list.firstOrNull { it.shortName == "9Pay" })
    }

    @Test
    fun `findInstitutionForWallet matches keywords and abbreviations accurately`() {
        assertEquals("VCB", findInstitutionForWallet("Vietcombank")?.code)
        assertEquals("VCB", findInstitutionForWallet("vcb")?.code)
        assertEquals("TCB", findInstitutionForWallet("Techcombank")?.code)
        assertEquals("TCB", findInstitutionForWallet("tcb")?.code)
        assertEquals("MB", findInstitutionForWallet("MB Bank")?.code)
        assertEquals("MB", findInstitutionForWallet("mbbank")?.code)
        assertEquals("MOMO", findInstitutionForWallet("Ví MoMo")?.code)
        assertEquals("ZALOPAY", findInstitutionForWallet("ZaloPay")?.code)
        assertEquals("VTLMONEY", findInstitutionForWallet("Viettel Money")?.code)
        assertEquals("VNPTMONEY", findInstitutionForWallet("VNPT Money")?.code)
        assertEquals("SHOPEEPAY", findInstitutionForWallet("ShopeePay")?.code)
        assertEquals("PAYOO", findInstitutionForWallet("Ví Payoo")?.code)
        assertEquals("9PAY", findInstitutionForWallet("9Pay")?.code)
        assertEquals("VBA", findInstitutionForWallet("Agribank")?.code)
        assertEquals("MB", findInstitutionForWallet("970422")?.code)
        assertEquals("CASH", findInstitutionForWallet("Tiền mặt")?.code)
        assertEquals("SAVE", findInstitutionForWallet("Sổ tiết kiệm")?.code)
    }
}
